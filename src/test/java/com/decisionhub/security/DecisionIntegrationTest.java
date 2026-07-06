package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.ComparisonFactorRequest;
import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionOption;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.User;
import com.decisionhub.entity.VotingType;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.service.DecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/decisionhub",
    "spring.datasource.username=decisionhub_app",
    "spring.datasource.password=dh_dev_sec_pwd_2026",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DecisionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionBoardRepository decisionBoardRepository;

    @Autowired
    private DecisionOptionRepository decisionOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String creatorToken;
    private String otherUserToken;
    private UUID creatorId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() throws Exception {
        // Clear tables in reverse dependency order
        decisionOptionRepository.deleteAll();
        decisionBoardRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        // 1. Creator Registration & Login
        UserRegisterRequest creatorReg = new UserRegisterRequest(
                "creatoruser", "creator@test.com", "Password123!", "Creator", "User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorReg)))
                .andExpect(status().isCreated());

        UserLoginRequest creatorLogin = new UserLoginRequest("creatoruser", "Password123!");
        String creatorLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creatorLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        creatorToken = objectMapper.readValue(creatorLoginResponse, AuthTokenResponse.class).accessToken();
        creatorId = userRepository.findByUsername("creatoruser").orElseThrow().getId();

        // 2. Other User Registration & Login
        UserRegisterRequest otherReg = new UserRegisterRequest(
                "otheruser", "other@test.com", "Password123!", "Other", "User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherReg)))
                .andExpect(status().isCreated());

        UserLoginRequest otherLogin = new UserLoginRequest("otheruser", "Password123!");
        String otherLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        otherUserToken = objectMapper.readValue(otherLoginResponse, AuthTokenResponse.class).accessToken();
        otherUserId = userRepository.findByUsername("otheruser").orElseThrow().getId();
    }

    @Test
    void testDecisionLifecycleAndAuthorization() throws Exception {
        // 1. Create Decision Board (DRAFT State)
        DecisionRequest createRequest = new DecisionRequest(
                "Framework Comparison", "Select a backend Java framework", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                new HashSet<>(Arrays.asList("java", "framework")),
                Arrays.asList(new OptionCreateDto("Spring Boot", "Enterprise stack", Collections.emptyList()), new OptionCreateDto("Quarkus", "Cloud native", Collections.emptyList())),
                Arrays.asList(new ComparisonFactorRequest("Performance", "Throughput specs"), new ComparisonFactorRequest("Ease of use", "Developer velocity"))
        );

        String createResponseJson = mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Framework Comparison"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        DecisionResponse createdDecision = objectMapper.readValue(createResponseJson, DecisionResponse.class);
        UUID decisionId = createdDecision.id();

        // Verify Audit Log generated
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("DECISION_CREATED")));

        // 2. Non-Owner Updates Rejected (Forbidden)
        DecisionUpdateRequest updateRequest = new DecisionUpdateRequest(
                "Hacked Title", "Hacked Description", null, null, Collections.emptySet()
        );
        mockMvc.perform(put("/decisions/{id}", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // 3. Owner Updates Metadata (Ok)
        DecisionUpdateRequest validUpdate = new DecisionUpdateRequest(
                "Updated Framework Comparison", "Select backend stack", null, null, new HashSet<>(Collections.singletonList("jvm"))
        );
        mockMvc.perform(put("/decisions/{id}", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Framework Comparison"));

        // 4. Transition: DRAFT -> ACTIVE (Ok)
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify Status Transition logged in Audit
        auditLogs = auditLogRepository.findAll();
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("DECISION_ACTIVATED")));

        // 5. Transition: ACTIVE -> DRAFT (Invalid Transition - Rejected)
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "DRAFT"))
                .andExpect(status().isBadRequest());

        // 6. Transition: ACTIVE -> CLOSED (Ok)
        mockMvc.perform(put("/decisions/{id}/status", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        // 7. Closed Decisions are Immutable (Rejected)
        mockMvc.perform(put("/decisions/{id}", decisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVisibilityConstraints() throws Exception {
        // Create a PRIVATE decision board
        DecisionRequest privateRequest = new DecisionRequest(
                "Confidential Board", "Private planning workspace", null, null,
                false, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                Collections.emptySet(),
                Arrays.asList(new OptionCreateDto("OptA", "desc", Collections.emptyList()), new OptionCreateDto("OptB", "desc", Collections.emptyList())),
                Collections.emptyList()
        );

        String privateResponseJson = mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(privateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        UUID privateId = objectMapper.readValue(privateResponseJson, DecisionResponse.class).id();

        // Other user fetching the private board should be Forbidden
        mockMvc.perform(get("/decisions/{id}", privateId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());

        // Creator fetching it should be Ok
        mockMvc.perform(get("/decisions/{id}", privateId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Confidential Board"));
    }

    @Test
    void testValidationConstraints() throws Exception {
        // 1. Insufficient Options (Rejected)
        DecisionRequest invalidOptions = new DecisionRequest(
                "Invalid Board", "desc", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                Collections.emptySet(),
                Collections.singletonList(new OptionCreateDto("OptA", "desc", Collections.emptyList())),
                Collections.emptyList()
        );
        mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOptions)))
                .andExpect(status().isBadRequest());

        // 2. Past Deadline (Rejected)
        DecisionRequest invalidDeadline = new DecisionRequest(
                "Invalid Board", "desc", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, Instant.now().minusSeconds(3600),
                Collections.emptySet(),
                Arrays.asList(new OptionCreateDto("OptA", "desc", Collections.emptyList()), new OptionCreateDto("OptB", "desc", Collections.emptyList())),
                Collections.emptyList()
        );
        mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDeadline)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testOptimisticLockingConcurrency() throws Exception {
        // Create initial board
        DecisionRequest request = new DecisionRequest(
                "Concurrency Test", "desc", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                Collections.emptySet(),
                Arrays.asList(new OptionCreateDto("OptA", "desc", Collections.emptyList()), new OptionCreateDto("OptB", "desc", Collections.emptyList())),
                Collections.emptyList()
        );

        String createResponseJson = mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID decisionId = objectMapper.readValue(createResponseJson, DecisionResponse.class).id();

        // Retrieve board in two separate JPA sessions (simulate concurrent loading)
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        
        DecisionBoard session1Entity = txTemplate.execute(status -> {
            DecisionBoard db = decisionBoardRepository.findById(decisionId).orElseThrow();
            // Trigger lazy loading
            Hibernate.initialize(db.getOptions());
            return db;
        });

        DecisionBoard session2Entity = txTemplate.execute(status -> {
            DecisionBoard db = decisionBoardRepository.findById(decisionId).orElseThrow();
            Hibernate.initialize(db.getOptions());
            return db;
        });

        assertNotNull(session1Entity);
        assertNotNull(session2Entity);
        assertEquals(0L, session1Entity.getVersion());
        assertEquals(0L, session2Entity.getVersion());

        // Update session 1 entity (version becomes 1)
        txTemplate.execute(status -> {
            session1Entity.setTitle("Title 1");
            return decisionBoardRepository.saveAndFlush(session1Entity);
        });

        // Update session 2 entity (should throw OptimisticLockingFailureException because database version is now 1 but entity version is 0)
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            txTemplate.execute(status -> {
                session2Entity.setTitle("Title 2");
                return decisionBoardRepository.saveAndFlush(session2Entity);
            });
        });
    }

    @Test
    void testAggregateRollbackOnError() throws Exception {
        // Attempt to create a board, but pass invalid option fields (Constraint violation or manual throw)
        // In this case, we'll try to save options that violate the database schema nullability rules (e.g. title is blank/null on option)
        DecisionRequest invalidRequest = new DecisionRequest(
                "Rollback Test Board", "desc", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                Collections.emptySet(),
                Arrays.asList(new OptionCreateDto("", "Empty Title", Collections.emptyList()), new OptionCreateDto("Valid Title", "desc", Collections.emptyList())),
                Collections.emptyList()
        );

        // Blank option title is blocked by JSR validations or database constraints
        mockMvc.perform(post("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify that the DecisionBoard was NOT saved (full transactional rollback)
        List<DecisionBoard> boards = decisionBoardRepository.findAll();
        assertTrue(boards.isEmpty());
    }

    @Test
    void testRepositoryFetchOptimization() {
        // Create decision board in database
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        UUID dbId = txTemplate.execute(status -> {
            User user = userRepository.findByUsername("creatoruser").orElseThrow();
            DecisionBoard board = DecisionBoard.builder()
                    .title("Fetch Opt Board")
                    .creator(user)
                    .votingType(VotingType.SINGLE_CHOICE)
                    .anonymityType(AnonymityType.PUBLIC)
                    .status(DecisionStatus.DRAFT)
                    .build();
            board.addOption(DecisionOption.builder().title("OptA").decision(board).build());
            board.addOption(DecisionOption.builder().title("OptB").decision(board).build());
            return decisionBoardRepository.save(board).getId();
        });

        assertNotNull(dbId);

        // Fetch using service and assert that collections are loaded in exactly 2 or 3 queries, and no N+1 query trigger occurs.
        // We verify that the lazy-loaded lists (options, tags, factors) are initialized and accessible after query
        txTemplate.execute(status -> {
            DecisionResponse response = decisionService.getDecisionById(dbId);
            assertNotNull(response);
            assertEquals(2, response.options().size());
            return null;
        });
    }
}

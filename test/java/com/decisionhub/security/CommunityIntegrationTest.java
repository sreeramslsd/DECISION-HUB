package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.CommunityMemberRepository;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class CommunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMemberRepository communityMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String creatorToken;
    private String memberToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clear repositories to isolate test runs
        communityMemberRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        // 1. Create and Login Community Creator
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

        // 2. Create and Login Secondary User (regular member)
        UserRegisterRequest memberReg = new UserRegisterRequest(
                "memberuser", "member@test.com", "Password123!", "Member", "User"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReg)))
                .andExpect(status().isCreated());

        UserLoginRequest memberLogin = new UserLoginRequest("memberuser", "Password123!");
        String memberLoginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        memberToken = objectMapper.readValue(memberLoginResponse, AuthTokenResponse.class).accessToken();
    }

    @Test
    void testCommunityLifecycleWorkflows() throws Exception {
        // 1. Create a Community
        CommunityRequest createRequest = new CommunityRequest(
                "Software Engineering Workspace", 
                "A workspace for software engineering professionals and researchers.", 
                "software-eng", 
                null
        );

        String createResponseJson = mockMvc.perform(post("/communities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Software Engineering Workspace"))
                .andExpect(jsonPath("$.slug").value("software-eng"))
                .andReturn().getResponse().getContentAsString();

        CommunityResponse createdCommunity = objectMapper.readValue(createResponseJson, CommunityResponse.class);
        UUID communityId = createdCommunity.id();

        // Verify database state: Creator should be active Moderator
        List<CommunityMemberResponse> members = communityServiceGetMembers(communityId);
        assertEquals(1, members.size());
        assertEquals("creatoruser", members.get(0).username());
        assertEquals(CommunityRole.MODERATOR, members.get(0).role());
        assertEquals(CommunityStatus.ACTIVE, members.get(0).status());

        // Verify audit logs exist
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertFalse(auditLogs.isEmpty());
        assertTrue(auditLogs.stream().anyMatch(l -> l.getAction().equals("COMMUNITY_CREATED")));

        // 2. Enforce Slug Uniqueness (Conflict)
        mockMvc.perform(post("/communities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        // 3. Regular member Joins Community
        mockMvc.perform(post("/communities/{id}/join", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Join again (Conflict)
        mockMvc.perform(post("/communities/{id}/join", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isConflict());

        // Verify members list
        members = communityServiceGetMembers(communityId);
        assertEquals(2, members.size());

        // 4. Update Community Metadata (Authorized)
        CommunityRequest updateRequest = new CommunityRequest(
                "Updated Software Engineering", 
                "Updated workspace description.", 
                "software-eng-updated", 
                null
        );

        mockMvc.perform(put("/communities/{id}", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Software Engineering"))
                .andExpect(jsonPath("$.slug").value("software-eng-updated"));

        // Update Community Metadata (Unauthorized - Forbidden)
        mockMvc.perform(put("/communities/{id}", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // 5. Member management (Role & Status Promotion/Demotion)
        UUID targetUserId = userRepository.findByUsername("memberuser").orElseThrow().getId();

        // Promotes member to MODERATOR
        mockMvc.perform(put("/communities/{id}/members/{userId}/role", communityId, targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("role", "MODERATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));

        // Demotes member back to MEMBER
        mockMvc.perform(put("/communities/{id}/members/{userId}/role", communityId, targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("role", "MEMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));

        // Blocks member
        mockMvc.perform(put("/communities/{id}/members/{userId}/status", communityId, targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        // 6. Leave Community Restrictions (Last Moderator cannot leave)
        mockMvc.perform(post("/communities/{id}/leave", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isBadRequest()); // Blocked: Creator is last active moderator

        // Member Leaves Community
        mockMvc.perform(post("/communities/{id}/leave", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + memberToken))
                .andExpect(status().isNoContent());

        // 7. Soft Delete Workspace
        mockMvc.perform(delete("/communities/{id}", communityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isNoContent());

        // Exclude soft-deleted communities from GET list/slug/search
        mockMvc.perform(get("/communities/{id}", communityId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/communities/slug/{slug}", "software-eng-updated"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPostgresqlFullTextSearchQueries() throws Exception {
        // Create search targets
        CommunityRequest community1 = new CommunityRequest("Java Developers", "Learn Java 21, Spring Boot, and Hibernate.", "java-dev", null);
        CommunityRequest community2 = new CommunityRequest("PostgreSQL Engines", "Advanced database engine architectures.", "postgres-dev", null);

        mockMvc.perform(post("/communities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(community1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/communities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(community2)))
                .andExpect(status().isCreated());

        // Perform FTS search queries
        mockMvc.perform(get("/communities/search")
                        .param("query", "Spring")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Java Developers"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/communities/search")
                        .param("query", "Database")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("PostgreSQL Engines"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // Test non-matching FTS query
        mockMvc.perform(get("/communities/search")
                        .param("query", "NonExistentWord")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private List<CommunityMemberResponse> communityServiceGetMembers(UUID communityId) throws Exception {
        String responseJson = mockMvc.perform(get("/communities/{id}/members", communityId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(responseJson, objectMapper.getTypeFactory().constructCollectionType(List.class, CommunityMemberResponse.class));
    }
}

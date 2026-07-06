package com.decisionhub.controller;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionSummaryDto;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.OptionCreateDto;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.AnonymityType;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.entity.VotingType;
import com.decisionhub.service.DecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DecisionController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DecisionService decisionService;

    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void createDecision_withValidPayload_returnsCreated() throws Exception {
        UUID decisionId = UUID.randomUUID();
        DecisionRequest request = new DecisionRequest(
                "Design Architecture", "Select database stack", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                new HashSet<>(Arrays.asList("tech", "db")),
                Arrays.asList(new OptionCreateDto("Postgres", "Relational DB", Collections.emptyList()), new OptionCreateDto("Mongo", "NoSQL", Collections.emptyList())),
                Collections.emptyList()
        );

        DecisionResponse response = new DecisionResponse(
                decisionId, "Design Architecture", "Select database stack",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.DRAFT,
                null, 0L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                new HashSet<>(Arrays.asList("tech", "db")), Instant.now()
        );

        when(decisionService.createDecision(any(DecisionRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(decisionId.toString()))
                .andExpect(jsonPath("$.title").value("Design Architecture"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createDecision_withInsufficientOptions_returnsBadRequest() throws Exception {
        DecisionRequest request = new DecisionRequest(
                "Design Architecture", "Select database stack", null, null,
                true, VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, null,
                Collections.emptySet(),
                Collections.singletonList(new OptionCreateDto("Postgres", "Relational DB", Collections.emptyList())),
                Collections.emptyList()
        );

        mockMvc.perform(post("/decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDecisionById_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DecisionResponse response = new DecisionResponse(
                id, "Design Architecture", "Select database stack",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.DRAFT,
                null, 0L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptySet(), Instant.now()
        );

        when(decisionService.getDecisionById(eq(id))).thenReturn(response);

        mockMvc.perform(get("/decisions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getDecisions_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DecisionResponse response = new DecisionResponse(
                id, "Design Architecture", "Select database stack",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.DRAFT,
                null, 0L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptySet(), Instant.now()
        );

        PageResponse<DecisionResponse> pageResponse = new PageResponse<>(
                Collections.singletonList(response), 0, 20, 1L, 1, true
        );

        when(decisionService.getDecisions(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/decisions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void updateDecision_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DecisionUpdateRequest request = new DecisionUpdateRequest(
                "Updated Title", "Updated Desc", null, null, Collections.emptySet()
        );

        DecisionResponse response = new DecisionResponse(
                id, "Updated Title", "Updated Desc",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.DRAFT,
                null, 1L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptySet(), Instant.now()
        );

        when(decisionService.updateDecision(eq(id), any(), any(), any())).thenReturn(response);

        mockMvc.perform(put("/decisions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteDecision_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(decisionService).deleteDecision(eq(id), any(), any());

        mockMvc.perform(delete("/decisions/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void transitionStatus_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DecisionResponse response = new DecisionResponse(
                id, "Design Architecture", "Select database stack",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.ACTIVE,
                null, 1L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptySet(), Instant.now()
        );

        when(decisionService.transitionStatus(eq(id), eq(DecisionStatus.ACTIVE), any(), any())).thenReturn(response);

        mockMvc.perform(put("/decisions/{id}/status", id)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void searchDecisions_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        DecisionResponse response = new DecisionResponse(
                id, "Design Architecture", "Select database stack",
                new UserSummaryDto(UUID.randomUUID(), "creator", null), null, null,
                VotingType.SINGLE_CHOICE, AnonymityType.PUBLIC, DecisionStatus.DRAFT,
                null, 0L, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptySet(), Instant.now()
        );

        PageResponse<DecisionResponse> pageResponse = new PageResponse<>(
                Collections.singletonList(response), 0, 20, 1L, 1, true
        );

        when(decisionService.searchDecisions(eq("Architecture"), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/decisions/search")
                        .param("query", "Architecture")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Design Architecture"));
    }
}

package com.decisionhub.controller;

import com.decisionhub.dto.CategoryResponse;
import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.dto.UserSummaryDto;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.service.CommunityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = CommunityController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    // Prevent JPA Auditing context load failures during WebMvcTest
    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void createCommunity_withValidPayload_returnsCreated() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityRequest request = new CommunityRequest("Tech Workspace", "A tech community", "tech-workspace", null);
        CommunityResponse response = new CommunityResponse(
            communityId, "Tech Workspace", "A tech community", "tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );

        when(communityService.createCommunity(any(CommunityRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(communityId.toString()))
                .andExpect(jsonPath("$.name").value("Tech Workspace"))
                .andExpect(jsonPath("$.slug").value("tech-workspace"));
    }

    @Test
    void createCommunity_withBlankName_returnsBadRequest() throws Exception {
        CommunityRequest request = new CommunityRequest("", "A tech community", "tech-workspace", null);

        mockMvc.perform(post("/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void getCommunities_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityResponse response = new CommunityResponse(
            communityId, "Tech Workspace", "A tech community", "tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );
        PageResponse<CommunityResponse> pageResponse = new PageResponse<>(
            Collections.singletonList(response), 0, 20, 1L, 1, true
        );

        when(communityService.getCommunities(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/communities")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(communityId.toString()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getCommunityById_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityResponse response = new CommunityResponse(
            communityId, "Tech Workspace", "A tech community", "tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );

        when(communityService.getCommunityById(eq(communityId))).thenReturn(response);

        mockMvc.perform(get("/communities/{id}", communityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(communityId.toString()));
    }

    @Test
    void getCommunityBySlug_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityResponse response = new CommunityResponse(
            communityId, "Tech Workspace", "A tech community", "tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );

        when(communityService.getCommunityBySlug(eq("tech-workspace"))).thenReturn(response);

        mockMvc.perform(get("/communities/slug/{slug}", "tech-workspace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("tech-workspace"));
    }

    @Test
    void updateCommunity_withValidPayload_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityRequest request = new CommunityRequest("New Tech Workspace", "Updated tech community", "new-tech-workspace", null);
        CommunityResponse response = new CommunityResponse(
            communityId, "New Tech Workspace", "Updated tech community", "new-tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );

        when(communityService.updateCommunity(eq(communityId), any(CommunityRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(put("/communities/{id}", communityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Tech Workspace"))
                .andExpect(jsonPath("$.slug").value("new-tech-workspace"));
    }

    @Test
    void deleteCommunity_returnsNoContent() throws Exception {
        UUID communityId = UUID.randomUUID();
        doNothing().when(communityService).deleteCommunity(eq(communityId), any(), any());

        mockMvc.perform(delete("/communities/{id}", communityId))
                .andExpect(status().isNoContent());
    }

    @Test
    void joinCommunity_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommunityMemberResponse memberResponse = new CommunityMemberResponse(
            userId, "user1", null, CommunityRole.MEMBER, CommunityStatus.ACTIVE, Instant.now()
        );

        when(communityService.joinCommunity(eq(communityId), any(), any())).thenReturn(memberResponse);

        mockMvc.perform(post("/communities/{id}/join", communityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void leaveCommunity_returnsNoContent() throws Exception {
        UUID communityId = UUID.randomUUID();
        doNothing().when(communityService).leaveCommunity(eq(communityId), any(), any());

        mockMvc.perform(post("/communities/{id}/leave", communityId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMembers_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommunityMemberResponse memberResponse = new CommunityMemberResponse(
            userId, "user1", null, CommunityRole.MEMBER, CommunityStatus.ACTIVE, Instant.now()
        );

        when(communityService.getMembers(eq(communityId))).thenReturn(Collections.singletonList(memberResponse));

        mockMvc.perform(get("/communities/{id}/members", communityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void updateMemberRole_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommunityMemberResponse memberResponse = new CommunityMemberResponse(
            userId, "user1", null, CommunityRole.MODERATOR, CommunityStatus.ACTIVE, Instant.now()
        );

        when(communityService.updateMemberRole(eq(communityId), eq(userId), eq(CommunityRole.MODERATOR), any(), any())).thenReturn(memberResponse);

        mockMvc.perform(put("/communities/{id}/members/{userId}/role", communityId, userId)
                        .param("role", "MODERATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));
    }

    @Test
    void updateMemberStatus_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommunityMemberResponse memberResponse = new CommunityMemberResponse(
            userId, "user1", null, CommunityRole.MEMBER, CommunityStatus.BLOCKED, Instant.now()
        );

        when(communityService.updateMemberStatus(eq(communityId), eq(userId), eq(CommunityStatus.BLOCKED), any(), any())).thenReturn(memberResponse);

        mockMvc.perform(put("/communities/{id}/members/{userId}/status", communityId, userId)
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void searchCommunities_returnsOk() throws Exception {
        UUID communityId = UUID.randomUUID();
        CommunityResponse response = new CommunityResponse(
            communityId, "Tech Workspace", "A tech community", "tech-workspace",
            null, new UserSummaryDto(UUID.randomUUID(), "creator", null), Instant.now()
        );
        PageResponse<CommunityResponse> pageResponse = new PageResponse<>(
            Collections.singletonList(response), 0, 20, 1L, 1, true
        );

        when(communityService.searchCommunities(eq("Tech"), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/communities/search")
                        .param("query", "Tech")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Tech Workspace"));
    }
}

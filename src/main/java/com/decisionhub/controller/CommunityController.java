package com.decisionhub.controller;

import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing community workspace administration and membership APIs.
 */
@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
@Tag(name = "Community", description = "Community Workspace Management & Membership Endpoints")
@Slf4j
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @Operation(summary = "Create a new community workspace", description = "Creates a community and assigns the creator as MODERATOR")
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to create community: {}", request.name());
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        CommunityResponse response = communityService.createCommunity(request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get list of active communities", description = "Retrieves a paginated list of all active communities")
    public ResponseEntity<PageResponse<CommunityResponse>> getCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        log.debug("REST request to get active communities list");
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<CommunityResponse> response = communityService.getCommunities(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve community details", description = "Gets active community by its unique ID")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable UUID id) {
        log.debug("REST request to get community by ID: {}", id);
        CommunityResponse response = communityService.getCommunityById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Retrieve community by slug name", description = "Gets active community by its unique URL slug")
    public ResponseEntity<CommunityResponse> getCommunityBySlug(@PathVariable String slug) {
        log.debug("REST request to get community by slug: {}", slug);
        CommunityResponse response = communityService.getCommunityBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update community metadata", description = "Updates an existing community's details (requires MODERATOR role)")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable UUID id,
            @Valid @RequestBody CommunityRequest request,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update community: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        CommunityResponse response = communityService.updateCommunity(id, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete community workspace", description = "Soft deletes a community workspace (requires MODERATOR role)")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable UUID id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to delete community: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        communityService.deleteCommunity(id, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Join community workspace", description = "Registers the authenticated user as an active MEMBER of the community")
    public ResponseEntity<CommunityMemberResponse> joinCommunity(
            @PathVariable UUID id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to join community: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        CommunityMemberResponse response = communityService.joinCommunity(id, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave community workspace", description = "Removes the user membership from the community (restricted if last active MODERATOR)")
    public ResponseEntity<Void> leaveCommunity(
            @PathVariable UUID id,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to leave community: {}", id);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        communityService.leaveCommunity(id, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get list of community members", description = "Retrieves all user memberships inside the community")
    public ResponseEntity<List<CommunityMemberResponse>> getMembers(@PathVariable UUID id) {
        log.debug("REST request to get community members list for ID: {}", id);
        List<CommunityMemberResponse> response = communityService.getMembers(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/members/{userId}/role")
    @Operation(summary = "Update community member role", description = "Promotes or demotes a community member (requires MODERATOR role)")
    public ResponseEntity<CommunityMemberResponse> updateMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestParam CommunityRole role,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update member role: community={}, user={}, role={}", id, userId, role);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        CommunityMemberResponse response = communityService.updateMemberRole(id, userId, role, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/members/{userId}/status")
    @Operation(summary = "Update community member status", description = "Changes membership access state (requires MODERATOR role)")
    public ResponseEntity<CommunityMemberResponse> updateMemberStatus(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestParam CommunityStatus status,
            HttpServletRequest servletRequest
    ) {
        log.info("REST request to update member status: community={}, user={}, status={}", id, userId, status);
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        CommunityMemberResponse response = communityService.updateMemberStatus(id, userId, status, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search communities", description = "Performs full-text indexing queries matching query string against name and description fields")
    public ResponseEntity<PageResponse<CommunityResponse>> searchCommunities(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("REST request to search communities with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CommunityResponse> response = communityService.searchCommunities(query, pageable);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

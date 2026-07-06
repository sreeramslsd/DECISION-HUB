package com.decisionhub.service;

import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing communities and community memberships.
 */
public interface CommunityService {

    /**
     * Creates a new community.
     */
    CommunityResponse createCommunity(CommunityRequest request, String ipAddress, String userAgent);

    /**
     * Retrieves a paginated list of all active communities.
     */
    PageResponse<CommunityResponse> getCommunities(Pageable pageable);

    /**
     * Retrieves a community by its ID.
     */
    CommunityResponse getCommunityById(UUID id);

    /**
     * Retrieves a community by its unique slug.
     */
    CommunityResponse getCommunityBySlug(String slug);

    /**
     * Updates an existing community.
     */
    CommunityResponse updateCommunity(UUID id, CommunityRequest request, String ipAddress, String userAgent);

    /**
     * Soft-deletes a community by its ID.
     */
    void deleteCommunity(UUID id, String ipAddress, String userAgent);

    /**
     * Adds the currently authenticated user as a member of a community.
     */
    CommunityMemberResponse joinCommunity(UUID communityId, String ipAddress, String userAgent);

    /**
     * Removes the currently authenticated user from a community's membership.
     */
    void leaveCommunity(UUID communityId, String ipAddress, String userAgent);

    /**
     * Retrieves a list of all active memberships in a community.
     */
    List<CommunityMemberResponse> getMembers(UUID communityId);

    /**
     * Updates the role of a community member.
     */
    CommunityMemberResponse updateMemberRole(UUID communityId, UUID userId, CommunityRole role, String ipAddress, String userAgent);

    /**
     * Updates the status of a community member.
     */
    CommunityMemberResponse updateMemberStatus(UUID communityId, UUID userId, CommunityStatus status, String ipAddress, String userAgent);

    /**
     * Performs a full-text search on communities by name or description.
     */
    PageResponse<CommunityResponse> searchCommunities(String query, Pageable pageable);
}

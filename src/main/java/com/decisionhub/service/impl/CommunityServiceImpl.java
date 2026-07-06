package com.decisionhub.service.impl;

import com.decisionhub.dto.CommunityMemberResponse;
import com.decisionhub.dto.CommunityRequest;
import com.decisionhub.dto.CommunityResponse;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.Category;
import com.decisionhub.entity.Community;
import com.decisionhub.entity.CommunityMember;
import com.decisionhub.entity.CommunityMemberId;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.entity.User;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.CommunityMapper;
import com.decisionhub.mapper.CommunityMemberMapper;
import com.decisionhub.repository.CategoryRepository;
import com.decisionhub.repository.CommunityMemberRepository;
import com.decisionhub.repository.CommunityRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.security.AuthenticationFacade;
import com.decisionhub.service.AuditService;
import com.decisionhub.service.CommunityService;
import com.decisionhub.validator.CommunityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing communities, memberships, search, and auditing logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommunityMapper communityMapper;
    private final CommunityMemberMapper communityMemberMapper;
    private final CommunityValidator communityValidator;
    private final AuditService auditService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @Transactional
    public CommunityResponse createCommunity(CommunityRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to create community: {}", request.name());
        
        communityValidator.validateSlugUniqueness(request.slug(), null);

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.categoryId()));
        }

        User creator = getCurrentUser();

        Community community = communityMapper.toEntity(request);
        community.setCategory(category);
        community.setCreator(creator);

        Community savedCommunity = communityRepository.save(community);

        // Add creator as Moderator
        CommunityMember member = CommunityMember.builder()
            .community(savedCommunity)
            .user(creator)
            .role(CommunityRole.MODERATOR)
            .status(CommunityStatus.ACTIVE)
            .build();
        communityMemberRepository.save(member);

        // Log events
        auditService.log(creator, "COMMUNITY_CREATED", "communities", savedCommunity.getId(), null, null, ipAddress, userAgent);
        auditService.log(creator, "MEMBER_JOINED", "community_members", savedCommunity.getId(), null, null, ipAddress, userAgent);

        log.info("Community '{}' created successfully", savedCommunity.getName());
        return communityMapper.toResponse(savedCommunity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityResponse> getCommunities(Pageable pageable) {
        log.debug("Fetching paginated list of communities");
        Page<Community> page = communityRepository.findAll(pageable);
        Page<CommunityResponse> mappedPage = page.map(communityMapper::toResponse);
        return PageResponse.from(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityResponse getCommunityById(UUID id) {
        log.debug("Fetching community by ID: {}", id);
        Community community = getActiveCommunity(id);
        return communityMapper.toResponse(community);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityResponse getCommunityBySlug(String slug) {
        log.debug("Fetching community by slug: {}", slug);
        Community community = getActiveCommunityBySlug(slug);
        return communityMapper.toResponse(community);
    }

    @Override
    @Transactional
    public CommunityResponse updateCommunity(UUID id, CommunityRequest request, String ipAddress, String userAgent) {
        log.info("Attempting to update community: {}", id);

        Community community = getActiveCommunity(id);

        User currentUser = getCurrentUser();
        communityValidator.validateModeratorPermission(id, currentUser.getId());
        communityValidator.validateSlugUniqueness(request.slug(), id);

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.categoryId()));
        }

        communityMapper.updateEntity(request, community);
        community.setCategory(category);

        Community savedCommunity = communityRepository.save(community);

        String oldValue = "{\"name\":\"" + community.getName() + "\",\"slug\":\"" + community.getSlug() + "\"}";
        String newValue = "{\"name\":\"" + request.name() + "\",\"slug\":\"" + request.slug() + "\"}";
        auditService.log(currentUser, "COMMUNITY_UPDATED", "communities", id, oldValue, newValue, ipAddress, userAgent);

        log.info("Community '{}' updated successfully", savedCommunity.getName());
        return communityMapper.toResponse(savedCommunity);
    }

    @Override
    @Transactional
    public void deleteCommunity(UUID id, String ipAddress, String userAgent) {
        log.info("Attempting to delete community: {}", id);

        Community community = getActiveCommunity(id);

        User currentUser = getCurrentUser();
        communityValidator.validateModeratorPermission(id, currentUser.getId());

        community.setDeletedAt(java.time.Instant.now());
        communityRepository.saveAndFlush(community);
        communityRepository.delete(community);

        auditService.log(currentUser, "COMMUNITY_DELETED", "communities", id, null, null, ipAddress, userAgent);

        log.info("Community with ID '{}' soft deleted successfully", id);
    }

    @Override
    @Transactional
    public CommunityMemberResponse joinCommunity(UUID communityId, String ipAddress, String userAgent) {
        log.info("User requesting to join community: {}", communityId);

        Community community = getActiveCommunity(communityId);

        User currentUser = getCurrentUser();
        communityValidator.validateMemberJoin(communityId, currentUser.getId());

        CommunityMember member = CommunityMember.builder()
            .community(community)
            .user(currentUser)
            .role(CommunityRole.MEMBER)
            .status(CommunityStatus.ACTIVE)
            .build();

        CommunityMember savedMember = communityMemberRepository.save(member);

        auditService.log(currentUser, "MEMBER_JOINED", "community_members", communityId, null, null, ipAddress, userAgent);

        log.info("User '{}' joined community '{}'", currentUser.getUsername(), community.getName());
        return communityMemberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    public void leaveCommunity(UUID communityId, String ipAddress, String userAgent) {
        log.info("User requesting to leave community: {}", communityId);

        Community community = getActiveCommunity(communityId);

        User currentUser = getCurrentUser();
        communityValidator.validateLeaveAllowed(communityId, currentUser.getId());

        CommunityMemberId memberId = new CommunityMemberId(communityId, currentUser.getId());
        CommunityMember member = communityMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this community"));

        communityMemberRepository.delete(member);

        auditService.log(currentUser, "MEMBER_LEFT", "community_members", communityId, null, null, ipAddress, userAgent);

        log.info("User '{}' left community '{}'", currentUser.getUsername(), community.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityMemberResponse> getMembers(UUID communityId) {
        log.debug("Fetching members list for community: {}", communityId);
        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with ID: " + communityId);
        }

        List<CommunityMember> members = communityMemberRepository.findByCommunityId(communityId);
        return members.stream()
            .map(communityMemberMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommunityMemberResponse updateMemberRole(UUID communityId, UUID userId, CommunityRole role, String ipAddress, String userAgent) {
        log.info("Updating member role: community={}, user={}, role={}", communityId, userId, role);

        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with ID: " + communityId);
        }

        User currentUser = getCurrentUser();
        communityValidator.validateModeratorPermission(communityId, currentUser.getId());
        communityValidator.validateRoleUpdateAllowed(communityId, userId, role);

        CommunityMemberId memberId = new CommunityMemberId(communityId, userId);
        CommunityMember member = communityMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this community"));

        member.setRole(role);
        CommunityMember savedMember = communityMemberRepository.save(member);

        String oldValue = "{\"role\":\"" + member.getRole().name() + "\"}";
        String newValue = "{\"role\":\"" + role.name() + "\"}";
        auditService.log(currentUser, "ROLE_CHANGED", "community_members", communityId, oldValue, newValue, ipAddress, userAgent);

        log.info("Role updated for user ID: {} to role: {}", userId, role);
        return communityMemberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional
    public CommunityMemberResponse updateMemberStatus(UUID communityId, UUID userId, CommunityStatus status, String ipAddress, String userAgent) {
        log.info("Updating member status: community={}, user={}, status={}", communityId, userId, status);

        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with ID: " + communityId);
        }

        User currentUser = getCurrentUser();
        communityValidator.validateModeratorPermission(communityId, currentUser.getId());
        communityValidator.validateStatusUpdateAllowed(communityId, userId, status);

        CommunityMemberId memberId = new CommunityMemberId(communityId, userId);
        CommunityMember member = communityMemberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this community"));

        member.setStatus(status);
        CommunityMember savedMember = communityMemberRepository.save(member);

        String oldValue = "{\"status\":\"" + member.getStatus().name() + "\"}";
        String newValue = "{\"status\":\"" + status.name() + "\"}";
        auditService.log(currentUser, "STATUS_CHANGED", "community_members", communityId, oldValue, newValue, ipAddress, userAgent);

        log.info("Status updated for user ID: {} to status: {}", userId, status);
        return communityMemberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommunityResponse> searchCommunities(String query, Pageable pageable) {
        log.debug("Performing full-text search on communities with query: {}", query);
        Page<Community> page = communityRepository.searchFullText(query, pageable);
        Page<CommunityResponse> mappedPage = page.map(communityMapper::toResponse);
        return PageResponse.from(mappedPage);
    }

    private User getCurrentUser() {
        UUID userId = authenticationFacade.getCurrentUserId()
            .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Community getActiveCommunity(UUID id) {
        Community community = communityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Community not found with ID: " + id));
        if (community.isDeleted()) {
            throw new ResourceNotFoundException("Community not found with ID: " + id);
        }
        return community;
    }

    private Community getActiveCommunityBySlug(String slug) {
        Community community = communityRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Community not found with slug: " + slug));
        if (community.isDeleted()) {
            throw new ResourceNotFoundException("Community not found with slug: " + slug);
        }
        return community;
    }
}

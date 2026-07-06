package com.decisionhub.validator;

import com.decisionhub.entity.Community;
import com.decisionhub.entity.CommunityMember;
import com.decisionhub.entity.CommunityMemberId;
import com.decisionhub.entity.CommunityRole;
import com.decisionhub.entity.CommunityStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ConflictException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.repository.CommunityMemberRepository;
import com.decisionhub.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Validator class containing business validation rules for the Community module.
 */
@Component
@RequiredArgsConstructor
public class CommunityValidator {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;

    /**
     * Asserts that a slug is unique within active communities.
     */
    public void validateSlugUniqueness(String slug, UUID currentCommunityId) {
        Optional<Community> existing = communityRepository.findBySlug(slug);
        if (existing.isPresent()) {
            if (currentCommunityId == null || !existing.get().getId().equals(currentCommunityId)) {
                throw new ConflictException("Slug '" + slug + "' is already in use by another community");
            }
        }
    }

    /**
     * Asserts that a user is not already a member of a community.
     */
    public void validateMemberJoin(UUID communityId, UUID userId) {
        CommunityMemberId memberId = new CommunityMemberId(communityId, userId);
        if (communityMemberRepository.existsById(memberId)) {
            throw new ConflictException("User is already a member of this community");
        }
    }

    /**
     * Asserts that a user is allowed to leave the community (not the last active moderator).
     */
    public void validateLeaveAllowed(UUID communityId, UUID userId) {
        CommunityMemberId memberId = new CommunityMemberId(communityId, userId);
        Optional<CommunityMember> memberOpt = communityMemberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new BadRequestException("User is not a member of this community");
        }

        CommunityMember member = memberOpt.get();
        if (member.getRole() == CommunityRole.MODERATOR && member.getStatus() == CommunityStatus.ACTIVE) {
            assertNotLastActiveModerator(communityId, userId, "leave");
        }
    }

    /**
     * Asserts that the requestor is an active moderator of the community.
     */
    public void validateModeratorPermission(UUID communityId, UUID moderatorId) {
        CommunityMemberId memberId = new CommunityMemberId(communityId, moderatorId);
        CommunityMember member = communityMemberRepository.findById(memberId)
            .orElseThrow(() -> new ForbiddenException("Only active community moderators can perform this action"));

        if (member.getRole() != CommunityRole.MODERATOR || member.getStatus() != CommunityStatus.ACTIVE) {
            throw new ForbiddenException("Only active community moderators can perform this action");
        }
    }

    /**
     * Asserts that demoting the target user is allowed (not demoting the last active moderator).
     */
    public void validateRoleUpdateAllowed(UUID communityId, UUID targetUserId, CommunityRole newRole) {
        if (newRole == CommunityRole.MEMBER) {
            CommunityMemberId memberId = new CommunityMemberId(communityId, targetUserId);
            Optional<CommunityMember> memberOpt = communityMemberRepository.findById(memberId);
            if (memberOpt.isPresent() && memberOpt.get().getRole() == CommunityRole.MODERATOR 
                    && memberOpt.get().getStatus() == CommunityStatus.ACTIVE) {
                assertNotLastActiveModerator(communityId, targetUserId, "demote");
            }
        }
    }

    /**
     * Asserts that deactivating the target user is allowed (not deactivating the last active moderator).
     */
    public void validateStatusUpdateAllowed(UUID communityId, UUID targetUserId, CommunityStatus newStatus) {
        if (newStatus != CommunityStatus.ACTIVE) {
            CommunityMemberId memberId = new CommunityMemberId(communityId, targetUserId);
            Optional<CommunityMember> memberOpt = communityMemberRepository.findById(memberId);
            if (memberOpt.isPresent() && memberOpt.get().getRole() == CommunityRole.MODERATOR 
                    && memberOpt.get().getStatus() == CommunityStatus.ACTIVE) {
                assertNotLastActiveModerator(communityId, targetUserId, "deactivate");
            }
        }
    }

    private void assertNotLastActiveModerator(UUID communityId, UUID userId, String actionType) {
        List<CommunityMember> members = communityMemberRepository.findByCommunityId(communityId);
        long activeModsCount = members.stream()
            .filter(m -> m.getRole() == CommunityRole.MODERATOR && m.getStatus() == CommunityStatus.ACTIVE)
            .count();

        if (activeModsCount <= 1) {
            throw new BadRequestException("Cannot " + actionType + " moderator as they are the last active moderator in the community");
        }
    }
}

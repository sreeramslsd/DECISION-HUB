package com.decisionhub.repository;

import com.decisionhub.entity.CommunityMember;
import com.decisionhub.entity.CommunityMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, CommunityMemberId> {
    List<CommunityMember> findByCommunityId(UUID communityId);
    List<CommunityMember> findByUserId(UUID userId);
}

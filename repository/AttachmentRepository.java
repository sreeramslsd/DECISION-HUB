package com.decisionhub.repository;

import com.decisionhub.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByDecisionId(UUID decisionId);
    List<Attachment> findByCommentId(UUID commentId);
    List<Attachment> findByCommunityId(UUID communityId);
}

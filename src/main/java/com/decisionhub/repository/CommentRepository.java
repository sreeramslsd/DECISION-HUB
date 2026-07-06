package com.decisionhub.repository;

import com.decisionhub.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByDecisionIdOrderByCreatedAtAsc(UUID decisionId);

    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
}

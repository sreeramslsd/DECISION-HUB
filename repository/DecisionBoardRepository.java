package com.decisionhub.repository;

import com.decisionhub.entity.DecisionBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionBoardRepository extends JpaRepository<DecisionBoard, UUID> {

    @EntityGraph(attributePaths = {"creator", "category", "community", "tags"})
    Optional<DecisionBoard> findById(UUID id);

    @EntityGraph(attributePaths = {"creator", "category", "community", "tags"})
    @Query("SELECT d FROM DecisionBoard d WHERE d.deletedAt IS NULL AND d.isPublic = true")
    Page<DecisionBoard> findPublicDecisions(Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "category", "community", "tags"})
    @Query("SELECT d FROM DecisionBoard d WHERE d.deletedAt IS NULL AND (" +
           "d.isPublic = true " +
           "OR d.creator.id = :userId " +
           "OR (d.community.id IS NOT NULL AND d.community.id IN (" +
           "  SELECT cm.community.id FROM CommunityMember cm " +
           "  WHERE cm.user.id = :userId AND cm.status = com.decisionhub.entity.CommunityStatus.ACTIVE" +
           "))" +
           ")")
    Page<DecisionBoard> findVisibleDecisions(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "category", "community", "tags"})
    Page<DecisionBoard> findByCreatorId(UUID creatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "category", "community", "tags"})
    Page<DecisionBoard> findByCommunityId(UUID communityId, Pageable pageable);

    @Query(value = "SELECT distinct db.* FROM decision_boards db " +
            "LEFT JOIN decision_tags dt ON db.id = dt.decision_id " +
            "LEFT JOIN tags t ON dt.tag_id = t.id " +
            "WHERE db.deleted_at IS NULL AND db.is_public = true AND (" +
            "to_tsvector('english', coalesce(db.title, '') || ' ' || coalesce(db.description, '')) @@ plainto_tsquery('english', :query) " +
            "OR to_tsvector('english', coalesce(t.name, '')) @@ plainto_tsquery('english', :query)" +
            ")",
            countQuery = "SELECT count(distinct db.id) FROM decision_boards db " +
            "LEFT JOIN decision_tags dt ON db.id = dt.decision_id " +
            "LEFT JOIN tags t ON dt.tag_id = t.id " +
            "WHERE db.deleted_at IS NULL AND db.is_public = true AND (" +
            "to_tsvector('english', coalesce(db.title, '') || ' ' || coalesce(db.description, '')) @@ plainto_tsquery('english', :query) " +
            "OR to_tsvector('english', coalesce(t.name, '')) @@ plainto_tsquery('english', :query)" +
            ")",
            nativeQuery = true)
    Page<DecisionBoard> searchPublicFullText(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT distinct db.* FROM decision_boards db " +
            "LEFT JOIN decision_tags dt ON db.id = dt.decision_id " +
            "LEFT JOIN tags t ON dt.tag_id = t.id " +
            "WHERE db.deleted_at IS NULL AND (" +
            "db.is_public = true " +
            "OR db.creator_id = :userId " +
            "OR (db.community_id IS NOT NULL AND db.community_id IN (" +
            "  SELECT community_id FROM community_members " +
            "  WHERE user_id = :userId AND status = 'ACTIVE'" +
            "))" +
            ") AND (" +
            "to_tsvector('english', coalesce(db.title, '') || ' ' || coalesce(db.description, '')) @@ plainto_tsquery('english', :query) " +
            "OR to_tsvector('english', coalesce(t.name, '')) @@ plainto_tsquery('english', :query)" +
            ")",
            countQuery = "SELECT count(distinct db.id) FROM decision_boards db " +
            "LEFT JOIN decision_tags dt ON db.id = dt.decision_id " +
            "LEFT JOIN tags t ON dt.tag_id = t.id " +
            "WHERE db.deleted_at IS NULL AND (" +
            "db.is_public = true " +
            "OR db.creator_id = :userId " +
            "OR (db.community_id IS NOT NULL AND db.community_id IN (" +
            "  SELECT community_id FROM community_members " +
            "  WHERE user_id = :userId AND status = 'ACTIVE'" +
            "))" +
            ") AND (" +
            "to_tsvector('english', coalesce(db.title, '') || ' ' || coalesce(db.description, '')) @@ plainto_tsquery('english', :query) " +
            "OR to_tsvector('english', coalesce(t.name, '')) @@ plainto_tsquery('english', :query)" +
            ")",
            nativeQuery = true)
    Page<DecisionBoard> searchVisibleFullText(@Param("query") String query, @Param("userId") UUID userId, Pageable pageable);
}

package com.decisionhub.repository;

import com.decisionhub.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {
    Optional<Community> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query(value = "SELECT * FROM communities WHERE deleted_at IS NULL AND " +
            "to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '')) @@ plainto_tsquery('english', :query)",
            countQuery = "SELECT count(*) FROM communities WHERE deleted_at IS NULL AND " +
            "to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, '')) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    Page<Community> searchFullText(@Param("query") String query, Pageable pageable);
}

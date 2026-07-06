package com.decisionhub.service;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionResponse;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.dto.PageResponse;
import com.decisionhub.entity.DecisionStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing Decision Boards, their visibility, lifecycle, and search.
 */
public interface DecisionService {

    /**
     * Creates a new decision board as a single transactional aggregate.
     */
    DecisionResponse createDecision(DecisionRequest request, String ipAddress, String userAgent);

    /**
     * Retrieves an active decision board by its ID with optimized fetches.
     */
    DecisionResponse getDecisionById(UUID id);

    /**
     * Retrieves a paginated list of all active decisions the current user is authorized to view.
     */
    PageResponse<DecisionResponse> getDecisions(Pageable pageable);

    /**
     * Updates metadata of an existing decision board.
     */
    DecisionResponse updateDecision(UUID id, DecisionUpdateRequest request, String ipAddress, String userAgent);

    /**
     * Soft-deletes a decision board.
     */
    void deleteDecision(UUID id, String ipAddress, String userAgent);

    /**
     * Transitions a decision board's lifecycle status.
     */
    DecisionResponse transitionStatus(UUID id, DecisionStatus status, String ipAddress, String userAgent);

    /**
     * Performs a full-text search on decision boards by title, description, or tags.
     */
    PageResponse<DecisionResponse> searchDecisions(String query, Pageable pageable);
}

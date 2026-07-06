package com.decisionhub.validator;

import com.decisionhub.dto.DecisionRequest;
import com.decisionhub.dto.DecisionUpdateRequest;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.DecisionStatus;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Validator class containing business validation rules for the Decision module.
 */
@Component
@RequiredArgsConstructor
public class DecisionValidator {

    private final CategoryRepository categoryRepository;

    /**
     * Validates a decision creation request.
     */
    public void validateCreate(DecisionRequest request) {
        if (request.options() == null || request.options().size() < 2) {
            throw new BadRequestException("At least two options are required to create a decision board");
        }

        if (request.deadline() != null && request.deadline().isBefore(Instant.now())) {
            throw new BadRequestException("Decision deadline must be in the future");
        }

        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new BadRequestException("Category not found with ID: " + request.categoryId());
        }
    }

    /**
     * Validates a decision update request against the existing board state.
     */
    public void validateUpdate(DecisionBoard existingBoard, DecisionUpdateRequest request) {
        if (existingBoard.getStatus() == DecisionStatus.CLOSED) {
            throw new BadRequestException("Cannot update a closed decision board");
        }

        if (request.deadline() != null && request.deadline().isBefore(Instant.now())) {
            // Only validate deadline if it has actually changed
            if (existingBoard.getDeadline() == null || !existingBoard.getDeadline().equals(request.deadline())) {
                throw new BadRequestException("Decision deadline must be in the future");
            }
        }

        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new BadRequestException("Category not found with ID: " + request.categoryId());
        }
    }

    /**
     * Validates status transitions of a decision board.
     */
    public void validateStatusTransition(DecisionBoard existingBoard, DecisionStatus newStatus) {
        DecisionStatus currentStatus = existingBoard.getStatus();
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == DecisionStatus.DRAFT && newStatus == DecisionStatus.ACTIVE) {
            // Valid transition
            return;
        }

        if (currentStatus == DecisionStatus.ACTIVE && newStatus == DecisionStatus.CLOSED) {
            // Valid transition
            return;
        }

        throw new BadRequestException(
            String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
        );
    }
}

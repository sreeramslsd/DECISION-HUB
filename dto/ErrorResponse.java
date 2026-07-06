package com.decisionhub.dto;

import java.time.Instant;
import java.util.List;

/**
 * Unified response wrapper for all API errors.
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    List<ValidationError> validationErrors
) {
    public record ValidationError(String field, String message) {}
}

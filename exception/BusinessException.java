package com.decisionhub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base abstract exception class for all domain-specific business logic errors.
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    
    private final HttpStatus status;

    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

package com.decisionhub.entity;

/**
 * Processing status of transactional outbox events.
 */
public enum EventStatus {
    PENDING,
    PROCESSED,
    FAILED
}

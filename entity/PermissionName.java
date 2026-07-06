package com.decisionhub.entity;

/**
 * Fine-grained application permissions for access control evaluation.
 */
public enum PermissionName {
    DECISION_CREATE,
    DECISION_READ,
    DECISION_UPDATE,
    DECISION_DELETE,
    DECISION_VOTE,
    DECISION_CLOSE,
    COMMUNITY_CREATE,
    COMMUNITY_UPDATE,
    COMMUNITY_DELETE,
    COMMUNITY_MODERATE,
    COMMENT_CREATE,
    COMMENT_UPDATE,
    COMMENT_DELETE,
    USER_MANAGE,
    CATEGORY_MANAGE,
    ANALYTICS_VIEW
}

package com.decisionhub.service;

import com.decisionhub.entity.User;
import java.util.UUID;

/**
 * Service interface responsible for logging system audit events.
 */
public interface AuditService {

    /**
     * Records a new audit log entry in the database.
     */
    void log(
        User user,
        String action,
        String targetTable,
        UUID targetId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent
    );
}

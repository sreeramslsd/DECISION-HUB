package com.decisionhub.service.impl;

import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.User;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for system audit logging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
        User user,
        String action,
        String targetTable,
        UUID targetId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent
    ) {
        log.debug("Auditing action: {} on target table: {} by user: {}", action, targetTable, user != null ? user.getUsername() : "SYSTEM");

        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action(action)
            .targetTable(targetTable)
            .targetId(targetId)
            .oldValue(oldValue)
            .newValue(newValue)
            .ipAddress(ipAddress != null ? ipAddress : "UNKNOWN")
            .userAgent(userAgent)
            .build();

        auditLogRepository.save(auditLog);
    }
}

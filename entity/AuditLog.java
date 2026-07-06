package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing system audit logs.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_logs_target", columnList = "target_table, target_id"),
        @Index(name = "idx_audit_logs_user_created", columnList = "user_id, created_at DESC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable (system/unauthenticated action)

    @NotBlank
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @NotBlank
    @Column(name = "target_table", nullable = false, length = 50)
    private String targetTable;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value")
    private String newValue;

    @NotBlank
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

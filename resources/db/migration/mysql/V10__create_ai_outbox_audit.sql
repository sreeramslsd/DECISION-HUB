-- V10__create_ai_outbox_audit.sql
CREATE TABLE ai_recommendations (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    decision_id BINARY(16) UNIQUE NOT NULL,
    pros TEXT NOT NULL,
    cons TEXT NOT NULL,
    risks TEXT NOT NULL,
    suggestions TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    reasoning TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ai_recommendations_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE events (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    event_type VARCHAR(100) NOT NULL,
    payload_json JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_events_status_created ON events(status, created_at);

CREATE TABLE audit_logs (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NULL,
    action VARCHAR(100) NOT NULL,
    target_table VARCHAR(50) NOT NULL,
    target_id BINARY(16) NOT NULL,
    old_value JSON NULL,
    new_value JSON NULL,
    ip_address VARCHAR(45) NOT NULL,
    request_id VARCHAR(36) NULL,
    user_agent VARCHAR(512) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_audit_logs_target ON audit_logs(target_table, target_id);
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at DESC);

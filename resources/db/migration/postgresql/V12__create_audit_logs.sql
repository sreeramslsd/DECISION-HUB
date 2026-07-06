-- V12__create_audit_logs.sql
-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NULL,
    action VARCHAR(100) NOT NULL,
    target_table VARCHAR(50) NOT NULL,
    target_id UUID NOT NULL,
    old_value JSONB NULL,
    new_value JSONB NULL,
    ip_address VARCHAR(45) NOT NULL,
    request_id VARCHAR(36) NULL,
    user_agent VARCHAR(512) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- PostgreSQL-specific indexes
CREATE INDEX idx_audit_logs_target ON audit_logs(target_table, target_id);
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at DESC);

-- JSONB index optimizations for old_value and new_value
CREATE INDEX idx_audit_logs_old_value_gin ON audit_logs USING GIN (old_value);
CREATE INDEX idx_audit_logs_new_value_gin ON audit_logs USING GIN (new_value);

-- V11__create_events_outbox.sql
-- Create events table for Transactional Outbox Pattern
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    payload_json JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE NULL
);

-- PostgreSQL-specific indexes
CREATE INDEX idx_events_status_created ON events(status, created_at);
CREATE INDEX idx_events_payload_json_gin ON events USING GIN (payload_json);

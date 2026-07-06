-- V8__create_comments_attachments.sql
-- Create comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID NOT NULL,
    user_id UUID NULL,
    parent_comment_id UUID NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_comments_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_comments_updated_at
BEFORE UPDATE ON comments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- PostgreSQL-specific indexes
CREATE INDEX idx_comments_decision_created ON comments(decision_id, created_at);
CREATE INDEX idx_comments_parent ON comments(parent_comment_id);

-- Expression-based GIN Index for Full Text Search on comment content
CREATE INDEX idx_comments_search_gin ON comments USING GIN (
    to_tsvector('english', coalesce(content, ''))
);

-- Create attachments table
CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    url VARCHAR(512) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploader_id UUID NOT NULL,
    decision_id UUID NULL,
    comment_id UUID NULL,
    community_id UUID NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_attachments_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_attachments_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT chk_attachment_entity CHECK (
        (decision_id IS NOT NULL AND comment_id IS NULL AND community_id IS NULL) OR
        (decision_id IS NULL AND comment_id IS NOT NULL AND community_id IS NULL) OR
        (decision_id IS NULL AND comment_id IS NULL AND community_id IS NOT NULL)
    )
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_attachments_updated_at
BEFORE UPDATE ON attachments
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

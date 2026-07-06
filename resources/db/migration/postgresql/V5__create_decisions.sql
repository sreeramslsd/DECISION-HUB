-- V5__create_decisions.sql
-- Create decision_boards table
CREATE TABLE decision_boards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creator_id UUID NOT NULL,
    community_id UUID NULL,
    category_id UUID NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    voting_type VARCHAR(20) NOT NULL,
    anonymity_type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deadline TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_decision_boards_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_decision_boards_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL,
    CONSTRAINT fk_decision_boards_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_decision_boards_updated_at
BEFORE UPDATE ON decision_boards
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- PostgreSQL-specific indexes
CREATE INDEX idx_decision_boards_status_deadline ON decision_boards(status, deadline);
CREATE INDEX idx_decision_boards_community_status ON decision_boards(community_id, status);
CREATE INDEX idx_decision_boards_creator_created ON decision_boards(creator_id, created_at);

-- Expression-based GIN Index for Full Text Search on title and description
CREATE INDEX idx_decision_boards_search_gin ON decision_boards USING GIN (
    to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, ''))
);

-- Create decision_history table
CREATE TABLE decision_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID NOT NULL,
    editor_id UUID NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    changes_json JSONB NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_decision_history_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_decision_history_editor FOREIGN KEY (editor_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- PostgreSQL-specific indexes for history lookup and JSONB search
CREATE INDEX idx_decision_history_lookup ON decision_history(decision_id, version);
CREATE INDEX idx_decision_history_changes_json_gin ON decision_history USING GIN (changes_json);

-- Create tags table
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create decision_tags table
CREATE TABLE decision_tags (
    decision_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (decision_id, tag_id),
    CONSTRAINT fk_decision_tags_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_decision_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- V4__create_communities.sql
-- Create communities table
CREATE TABLE communities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) NOT NULL,
    category_id UUID NULL,
    creator_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_communities_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_communities_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_communities_updated_at
BEFORE UPDATE ON communities
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- PostgreSQL-specific optimized indexes
CREATE INDEX idx_communities_slug_active ON communities(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_communities_created_at ON communities(created_at);

-- Expression-based GIN Index for Full Text Search on name and description
CREATE INDEX idx_communities_search_gin ON communities USING GIN (
    to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, ''))
);

-- Create community_members table
CREATE TABLE community_members (
    community_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (community_id, user_id),
    CONSTRAINT fk_community_members_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_community_members_updated_at
BEFORE UPDATE ON community_members
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_community_members_lookup ON community_members(community_id, status);

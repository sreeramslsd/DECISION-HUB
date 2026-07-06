-- V7__create_polls_and_votes.sql
-- Create polls table
CREATE TABLE polls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID NOT NULL,
    question VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_polls_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_polls_updated_at
BEFORE UPDATE ON polls
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create poll_voters table
CREATE TABLE poll_voters (
    poll_id UUID NOT NULL,
    user_id UUID NOT NULL,
    voted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (poll_id, user_id),
    CONSTRAINT fk_poll_voters_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    CONSTRAINT fk_poll_voters_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create votes table
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    poll_id UUID NOT NULL,
    option_id UUID NOT NULL,
    user_id UUID NULL,
    rating INT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_votes_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT fk_votes_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_option FOREIGN KEY (option_id) REFERENCES decision_options(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for votes
CREATE INDEX idx_votes_poll_created ON votes(poll_id, created_at);
CREATE INDEX idx_votes_option_lookup ON votes(option_id);

-- Create saved_decisions table
CREATE TABLE saved_decisions (
    user_id UUID NOT NULL,
    decision_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, decision_id),
    CONSTRAINT fk_saved_decisions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_decisions_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
);

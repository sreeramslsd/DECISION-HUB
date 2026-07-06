-- V7__create_voting_bookmarks.sql
CREATE TABLE decision_voters (
    decision_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    voted_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (decision_id, user_id),
    CONSTRAINT fk_decision_voters_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_decision_voters_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE votes (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    decision_id BINARY(16) NOT NULL,
    option_id BINARY(16) NOT NULL,
    user_id BINARY(16) NULL,
    rating INT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_votes_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT fk_votes_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_option FOREIGN KEY (option_id) REFERENCES decision_options(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_votes_decision_created ON votes(decision_id, created_at);
CREATE INDEX idx_votes_option_lookup ON votes(option_id);

CREATE TABLE saved_decisions (
    user_id BINARY(16) NOT NULL,
    decision_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, decision_id),
    CONSTRAINT fk_saved_decisions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_decisions_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Rename roles to platform_roles
RENAME TABLE roles TO platform_roles;

-- Update foreign key constraint in users table (it was fk_users_role pointing to roles)
ALTER TABLE users DROP FOREIGN KEY fk_users_role;
ALTER TABLE users ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES platform_roles(id) ON DELETE RESTRICT;

-- 2. Create oauth_accounts table
CREATE TABLE oauth_accounts (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_oauth_provider_user (provider, provider_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    user_id BINARY(16) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Create polls table
CREATE TABLE polls (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    decision_id BINARY(16) NOT NULL,
    question VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_polls_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. Create comparison_factors table
CREATE TABLE comparison_factors (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    decision_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_comparison_factors_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    UNIQUE KEY uq_decision_factor_name (decision_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. Create comparison_scores table
CREATE TABLE comparison_scores (
    option_id BINARY(16) NOT NULL,
    factor_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    score INT NOT NULL,
    remarks TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (option_id, factor_id, user_id),
    CONSTRAINT chk_comparison_scores_score CHECK (score >= 0 AND score <= 100),
    CONSTRAINT fk_comparison_scores_option FOREIGN KEY (option_id) REFERENCES decision_options(id) ON DELETE CASCADE,
    CONSTRAINT fk_comparison_scores_factor FOREIGN KEY (factor_id) REFERENCES comparison_factors(id) ON DELETE CASCADE,
    CONSTRAINT fk_comparison_scores_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. Refactor votes table
-- Drop foreign key pointing to decision_boards
ALTER TABLE votes DROP FOREIGN KEY fk_votes_decision;
-- Drop decision_id column and add poll_id column
ALTER TABLE votes DROP COLUMN decision_id;
ALTER TABLE votes ADD COLUMN poll_id BINARY(16) NOT NULL AFTER id;
-- Add new foreign key constraint
ALTER TABLE votes ADD CONSTRAINT fk_votes_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE;

-- 8. Refactor decision_voters to poll_voters
RENAME TABLE decision_voters TO poll_voters;
ALTER TABLE poll_voters DROP FOREIGN KEY fk_decision_voters_decision;
ALTER TABLE poll_voters RENAME COLUMN decision_id TO poll_id;
ALTER TABLE poll_voters ADD CONSTRAINT fk_poll_voters_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

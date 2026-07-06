-- V10__create_ai_recommendations.sql
-- Create ai_recommendations table
CREATE TABLE ai_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID UNIQUE NOT NULL,
    pros TEXT NOT NULL,
    cons TEXT NOT NULL,
    risks TEXT NOT NULL,
    suggestions TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    reasoning TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ai_recommendations_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_ai_recommendations_updated_at
BEFORE UPDATE ON ai_recommendations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create comparison_factors table (Sync to approved ER)
CREATE TABLE comparison_factors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    decision_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_comparison_factors_decision FOREIGN KEY (decision_id) REFERENCES decision_boards(id) ON DELETE CASCADE,
    CONSTRAINT uq_decision_factor_name UNIQUE (decision_id, name)
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_comparison_factors_updated_at
BEFORE UPDATE ON comparison_factors
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Create comparison_scores table (Sync to approved ER)
CREATE TABLE comparison_scores (
    option_id UUID NOT NULL,
    factor_id UUID NOT NULL,
    user_id UUID NOT NULL,
    score INT NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (option_id, factor_id, user_id),
    CONSTRAINT chk_comparison_scores_score CHECK (score >= 0 AND score <= 100),
    CONSTRAINT fk_comparison_scores_option FOREIGN KEY (option_id) REFERENCES decision_options(id) ON DELETE CASCADE,
    CONSTRAINT fk_comparison_scores_factor FOREIGN KEY (factor_id) REFERENCES comparison_factors(id) ON DELETE CASCADE,
    CONSTRAINT fk_comparison_scores_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Trigger for updated_at
CREATE TRIGGER trigger_update_comparison_scores_updated_at
BEFORE UPDATE ON comparison_scores
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity representing AI-generated recommendation cache for a decision board.
 */
@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "decision")
public class AIRecommendation extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false, unique = true)
    private DecisionBoard decision;

    @NotBlank
    @Column(name = "pros", columnDefinition = "TEXT", nullable = false)
    private String pros;

    @NotBlank
    @Column(name = "cons", columnDefinition = "TEXT", nullable = false)
    private String cons;

    @NotBlank
    @Column(name = "risks", columnDefinition = "TEXT", nullable = false)
    private String risks;

    @NotBlank
    @Column(name = "suggestions", columnDefinition = "TEXT", nullable = false)
    private String suggestions;

    @NotBlank
    @Column(name = "recommendation", columnDefinition = "TEXT", nullable = false)
    private String recommendation;

    @NotBlank
    @Column(name = "reasoning", columnDefinition = "TEXT", nullable = false)
    private String reasoning;
}

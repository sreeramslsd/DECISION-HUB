package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity mapping rating criteria scores for decision options.
 */
@Entity
@Table(
    name = "option_criteria",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_option_criteria_name", columnNames = {"option_id", "criterion_name"})
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "option")
public class OptionCriteria extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private DecisionOption option;

    @NotBlank
    @Column(name = "criterion_name", nullable = false, length = 100)
    private String criterionName;

    @Min(0)
    @Max(100)
    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}

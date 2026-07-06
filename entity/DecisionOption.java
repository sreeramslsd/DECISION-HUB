package com.decisionhub.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an option within a decision board.
 */
@Entity
@Table(name = "decision_options")
@SQLDelete(sql = "UPDATE decision_options SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"decision", "criteria", "comparisonScores"})
public class DecisionOption extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private DecisionBoard decision;

    @NotBlank
    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Deprecated
    private List<OptionCriteria> criteria = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ComparisonScore> comparisonScores = new ArrayList<>();

    /**
     * Helper to add criteria.
     *
     * @param crit Criteria item to append.
     */
    public void addCriteria(OptionCriteria crit) {
        this.criteria.add(crit);
        crit.setOption(this);
    }

    /**
     * Helper to remove criteria.
     *
     * @param crit Criteria item to detach.
     */
    public void removeCriteria(OptionCriteria crit) {
        this.criteria.remove(crit);
        crit.setOption(null);
    }
}

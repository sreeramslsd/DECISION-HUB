package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity tracking history modifications of decision boards.
 */
@Entity
@Table(
    name = "decision_history",
    indexes = {
        @Index(name = "idx_decision_history_lookup", columnList = "decision_id, version")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"decision", "editor"})
public class DecisionHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private DecisionBoard decision;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    private User editor;

    @NotBlank
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // 'CREATE', 'UPDATE', 'CLOSE'

    @NotBlank
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes_json", nullable = false)
    private String changesJson;
}

package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity representing hierarchical categories for decision boards and communities.
 */
@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_categories_name_del", columnList = "name, deleted_at")
    }
)
@SQLDelete(sql = "UPDATE categories SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Category extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;
}

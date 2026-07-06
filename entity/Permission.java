package com.decisionhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing fine-grained permissions for RBAC control.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "roles")
public class Permission extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private PermissionName name;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}

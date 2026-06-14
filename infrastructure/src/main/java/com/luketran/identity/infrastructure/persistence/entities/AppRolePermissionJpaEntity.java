package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_role_permission", uniqueConstraints = {
        @UniqueConstraint(name = "uq_app_role_permission_role_perm", columnNames = {"role_id", "permission_id"})
})
public class AppRolePermissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "role_id", nullable = false, insertable = false, updatable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false, insertable = false, updatable = false)
    private UUID permissionId;

    // === Relationships ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private AppRoleJpaEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private AppPermissionJpaEntity permission;
}

package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_role")
public class AppRoleJpaEntity extends BaseJpaEntity {

    @Column(name = "app_id", nullable = false, insertable = false, updatable = false)
    private UUID appId;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // === Relationships ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private AppJpaEntity app;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<AppRolePermissionJpaEntity> permissionItems;
}

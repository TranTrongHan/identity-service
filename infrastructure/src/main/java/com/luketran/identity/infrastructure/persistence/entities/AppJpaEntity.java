package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "app")
public class AppJpaEntity extends BaseJpaEntity {

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "signing_key", nullable = false, length = 128)
    private String signingKey;

    @Column(name = "token_lifetime_minutes", nullable = false)
    private int tokenLifetimeMinutes;

    @Column(name = "session_lifetime_minutes", nullable = false)
    private int sessionLifetimeMinutes;

    // === Relationships (inverse side, optional) ===
    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<AppRoleJpaEntity> roles;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<AppPermissionJpaEntity> permissions;

    @OneToMany(mappedBy = "app", fetch = FetchType.LAZY)
    private List<AppAccessJpaEntity> accesses;
}

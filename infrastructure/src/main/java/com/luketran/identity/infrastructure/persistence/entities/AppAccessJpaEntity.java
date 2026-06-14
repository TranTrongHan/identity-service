package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_access", uniqueConstraints = {
        @UniqueConstraint(name = "uq_app_access_account_app", columnNames = {"account_id", "app_id"})
})
public class AppAccessJpaEntity extends BaseJpaEntity {

    @Column(name = "account_id", nullable = false, insertable = false, updatable = false)
    private UUID accountId;

    @Column(name = "app_id", nullable = false, insertable = false, updatable = false)
    private UUID appId;

    @Column(name = "role_id", insertable = false, updatable = false)
    private UUID roleId;

    @Column(name = "scope", nullable = false, columnDefinition = "TEXT DEFAULT ''")
    private String scope;

    // === Relationships ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountJpaEntity account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private AppJpaEntity app;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private AppRoleJpaEntity role;
}

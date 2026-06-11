package com.luketran.identity.identity_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "app_access")
@Getter
@Setter
@NoArgsConstructor
public class AppAccess extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "app_id", nullable = false)
    private UUID appId;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "scope", nullable = false)
    private String scope = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", insertable = false, updatable = false)
    private App app;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private AppRole role;
}

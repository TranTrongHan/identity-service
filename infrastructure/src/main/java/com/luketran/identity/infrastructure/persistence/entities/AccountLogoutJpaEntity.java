package com.luketran.identity.infrastructure.persistence.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "account_logout")
@Getter
@Setter
public class AccountLogoutJpaEntity extends BaseJpaEntity {

    @Column(name = "account_id", nullable = false, updatable = false, insertable = false)
    private UUID accountId;

    // === Relationship ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountJpaEntity account;
}

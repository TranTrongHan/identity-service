package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "account")
public class AccountJpaEntity extends BaseJpaEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "secret_key", nullable = false, length = 100)
    private String secretKey;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "wrong_login_count", nullable = false)
    private int wrongLoginCount;

    @Column(name = "access_denied_until")
    private LocalDateTime accessDeniedUntil;
}

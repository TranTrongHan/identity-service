package com.luketran.identity.identity_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
public class Account extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "secret_key", nullable = false, length = 100)
    private String secretKey;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "wrong_login_count", nullable = false)
    private int wrongLoginCount = 0;

    @Column(name = "access_denied_until")
    private LocalDateTime accessDeniedUntil;
}

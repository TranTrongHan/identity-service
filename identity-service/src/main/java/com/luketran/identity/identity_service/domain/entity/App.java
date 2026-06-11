package com.luketran.identity.identity_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app")
@Getter
@Setter
@NoArgsConstructor
public class App extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "signing_key", nullable = false, length = 128)
    private String signingKey;

    @Column(name = "token_lifetime_minutes", nullable = false)
    private int tokenLifetimeMinutes = 15;

    @Column(name = "session_lifetime_minutes", nullable = false)
    private int sessionLifetimeMinutes = 1440;
}

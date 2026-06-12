package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_role")
public class AppRoleJpaEntity extends BaseJpaEntity {

    @Column(name = "app_id", nullable = false)
    private UUID appId;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;
}

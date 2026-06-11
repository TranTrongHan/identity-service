package com.luketran.identity.identity_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "app_role")
@Getter
@Setter
@NoArgsConstructor
public class AppRole extends BaseEntity {

    @Column(name = "app_id", nullable = false)
    private UUID appId;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", insertable = false, updatable = false)
    private App app;
}

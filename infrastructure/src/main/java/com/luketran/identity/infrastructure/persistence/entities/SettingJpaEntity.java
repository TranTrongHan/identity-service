package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "setting")
public class SettingJpaEntity {

    @Id
    @Column(name = "code", length = 100)
    private String code;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "description", length = 500)
    private String description;
}

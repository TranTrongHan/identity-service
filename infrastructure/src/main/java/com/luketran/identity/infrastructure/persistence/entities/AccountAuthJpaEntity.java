package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "account_auth", uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_auth_field_type_value", columnNames = {"field_type", "field_value"})
})
public class AccountAuthJpaEntity extends BaseJpaEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "field_type", nullable = false)
    private int fieldType;

    @Column(name = "field_value", nullable = false, length = 200)
    private String fieldValue;
}

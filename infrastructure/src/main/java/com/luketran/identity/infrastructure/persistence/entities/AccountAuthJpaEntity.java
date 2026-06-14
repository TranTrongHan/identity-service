package com.luketran.identity.infrastructure.persistence.entities;

import com.luketran.identity.domain.enums.AuthFieldType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "account_auth", uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_auth_field_type_value", columnNames = {"field_type", "field_value"})
})
public class AccountAuthJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "account_id", nullable = false, insertable = false, updatable = false)
    private UUID accountId;

    @Column(name = "field_type", nullable = false)
    private AuthFieldType fieldType;

    @Column(name = "field_value", nullable = false, length = 200)
    private String fieldValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // === Relationship ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountJpaEntity account;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

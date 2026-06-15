package com.luketran.identity.infrastructure.persistence.jpa;


import com.luketran.identity.infrastructure.persistence.entities.AccountSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountSessionJpaRepository extends JpaRepository<AccountSessionJpaEntity, UUID> {
    AccountSessionJpaEntity findByAccountIdAndAppId(UUID accountId, UUID appId);
    void deleteAllByAccountId(UUID accountId);
}

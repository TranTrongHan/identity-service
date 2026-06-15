package com.luketran.identity.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luketran.identity.infrastructure.persistence.entities.AccountLogoutJpaEntity;

public interface AccountLogoutJpaRepository extends JpaRepository<AccountLogoutJpaEntity, UUID> {

    Optional<AccountLogoutJpaEntity> findByAccountId(UUID accountId);

    void deleteByAccountId(UUID accountId);
}

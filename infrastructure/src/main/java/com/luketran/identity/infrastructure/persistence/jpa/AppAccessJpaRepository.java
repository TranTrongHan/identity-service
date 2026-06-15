package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppAccessJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppAccessJpaRepository extends JpaRepository<AppAccessJpaEntity, UUID> {
    Optional<AppAccessJpaEntity> findByAccountIdAndAppIdAndDeletedAtIsNull(UUID accountId, UUID appId);
}

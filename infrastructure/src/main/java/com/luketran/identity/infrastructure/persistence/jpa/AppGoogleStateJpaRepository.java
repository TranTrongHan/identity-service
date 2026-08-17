package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppGoogleStateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppGoogleStateJpaRepository extends JpaRepository<AppGoogleStateJpaEntity, UUID> {

    Optional<AppGoogleStateJpaEntity> findByAppIdAndState(UUID appId, UUID state);
}

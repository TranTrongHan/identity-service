package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppPermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppPermissionJpaRepository extends JpaRepository<AppPermissionJpaEntity, UUID> {
    List<AppPermissionJpaEntity> findAllByAppIdAndDeletedAtIsNull(UUID appId);

    List<AppPermissionJpaEntity> findAllByAppId(UUID appId);
}

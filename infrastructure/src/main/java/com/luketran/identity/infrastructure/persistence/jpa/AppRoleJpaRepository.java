package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRoleJpaRepository extends JpaRepository<AppRoleJpaEntity, UUID> {
    Optional<AppRoleJpaEntity> findByCodeAndAppIdAndDeletedAtIsNull(String code, UUID appId);
    List<AppRoleJpaEntity> findAllByAppIdAndDeletedAtIsNull(UUID appId);
    List<AppRoleJpaEntity> findAllByDeletedAtIsNull();
}

package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppRolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppRolePermissionJpaRepository extends JpaRepository<AppRolePermissionJpaEntity, UUID> {
    List<AppRolePermissionJpaEntity> findAllByRoleId(UUID roleId);
    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
    void deleteAllByRoleId(UUID roleId);
}

package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AppRolePermission;

import java.util.List;
import java.util.UUID;

public interface AppRolePermissionRepository extends BaseRepository<AppRolePermission> {

    List<AppRolePermission> findAllByRoleId(UUID roleId);

    AppRolePermission save(AppRolePermission appRolePermission);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    void deleteAllByRoleId(UUID roleId);
}

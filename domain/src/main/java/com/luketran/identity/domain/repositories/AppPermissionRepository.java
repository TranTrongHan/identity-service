package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AppPermission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppPermissionRepository extends BaseRepository<AppPermission> {

    Optional<AppPermission> findById(UUID id);

    List<AppPermission> findAllActiveByAppId(UUID appId);

    List<AppPermission> findAllByAppId(UUID appId);

    AppPermission save(AppPermission appPermission);

    void deleteById(UUID id);

    void softDelete(UUID id);
}

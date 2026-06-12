package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AppRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRoleRepository extends BaseRepository<AppRole> {

    Optional<AppRole> findById(UUID id);

    Optional<AppRole> findByCodeAndAppId(String code, UUID appId);

    List<AppRole> findAllActiveByAppId(UUID appId);

    AppRole save(AppRole appRole);

    void deleteById(UUID id);
}

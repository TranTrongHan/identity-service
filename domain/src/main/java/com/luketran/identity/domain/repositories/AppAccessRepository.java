package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AppAccess;

import java.util.Optional;
import java.util.UUID;

public interface AppAccessRepository extends BaseRepository<AppAccess> {

    Optional<AppAccess> findByAccountIdAndAppId(UUID accountId, UUID appId);

    AppAccess save(AppAccess appAccess);

    void deleteById(UUID id);
}

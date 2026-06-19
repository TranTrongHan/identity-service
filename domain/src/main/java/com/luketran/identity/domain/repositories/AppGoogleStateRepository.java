package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AppGoogleState;

import java.util.Optional;
import java.util.UUID;

public interface AppGoogleStateRepository {

    Optional<AppGoogleState> findByAppIdAndState(UUID appId, UUID state);

    AppGoogleState save(AppGoogleState entity);

    void deleteById(UUID id);
}

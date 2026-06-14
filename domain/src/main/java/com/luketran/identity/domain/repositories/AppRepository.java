package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.App;

import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends BaseRepository<App> {

    Optional<App> findById(UUID id);

    Optional<App> findByCode(String code);

    App save(App app);

    void deleteById(UUID id);
}

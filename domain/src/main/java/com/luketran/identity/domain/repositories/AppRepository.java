package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.App;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends BaseRepository<App> {

    Optional<App> findById(UUID id);

    Optional<App> findByCode(String code);

    List<App> findAllActive();

    App save(App app);

    void deleteById(UUID id);

    void softDelete(UUID id);
}

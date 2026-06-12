package com.luketran.identity.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseRepository<T> {

    Optional<T> findById(UUID id);

    List<T> findAll();

    T save(T entity);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}

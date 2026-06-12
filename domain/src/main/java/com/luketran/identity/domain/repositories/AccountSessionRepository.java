package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AccountSession;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountSessionRepository extends BaseRepository<AccountSession> {

    Optional<AccountSession> findActiveById(UUID id);

    void deleteAllExpiredBefore(LocalDateTime before);
}

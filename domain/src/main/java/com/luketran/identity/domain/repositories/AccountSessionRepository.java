package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AccountSession;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountSessionRepository extends BaseRepository<AccountSession> {

    Optional<AccountSession> findById(UUID id);

    Optional<AccountSession> findActiveById(UUID id);

    AccountSession save(AccountSession session);

    void deleteAllExpiredBefore(LocalDateTime before);

    void deleteAllByAccountId(UUID accountId);

    Optional<AccountSession> findByAccountIdAndAppId(UUID accountId, UUID appId);
}

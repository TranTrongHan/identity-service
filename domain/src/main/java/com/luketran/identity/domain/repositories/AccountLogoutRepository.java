package com.luketran.identity.domain.repositories;

import java.util.Optional;
import java.util.UUID;

import com.luketran.identity.domain.entities.AccountLogout;

public interface AccountLogoutRepository extends BaseRepository<AccountLogout> {
    Optional<AccountLogout> findByAccountId(UUID accountId);

    void deleteByAccountId(UUID accountId);
}

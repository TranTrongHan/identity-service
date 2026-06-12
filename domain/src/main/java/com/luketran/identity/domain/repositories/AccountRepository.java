package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends BaseRepository<Account> {

    Optional<Account> findById(UUID id);

    Optional<Account> findActiveById(UUID id);

    Account save(Account account);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}

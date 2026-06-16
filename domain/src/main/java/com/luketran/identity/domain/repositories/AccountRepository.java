package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends BaseRepository<Account> {

    Optional<Account> findById(UUID id);

    Optional<Account> findActiveById(UUID id);

    /**
     * Load Account kèm relations: authMethods, appAccesses (+ app, role), sessions (+ app).
     */
    Optional<Account> findWithDetails(UUID id);

    Account save(Account account);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    Account create(Account account);

    /**
     * Paginated list with optional filters.
     */
    List<Account> findPage(String nameFilter, String statusFilter, int offset, int size);

    long count(String nameFilter, String statusFilter);

    void softDelete(UUID id);
}

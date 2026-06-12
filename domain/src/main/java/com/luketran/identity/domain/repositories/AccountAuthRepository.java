package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.AccountAuth;
import com.luketran.identity.domain.enums.AuthFieldType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountAuthRepository extends BaseRepository<AccountAuth> {

    Optional<AccountAuth> findByFieldTypeAndFieldValue(AuthFieldType fieldType, String fieldValue);

    List<AccountAuth> findAllByAccountId(UUID accountId);

    AccountAuth save(AccountAuth accountAuth);

    void deleteById(UUID id);
}

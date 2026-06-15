package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AccountAuth;
import com.luketran.identity.domain.enums.AuthFieldType;
import com.luketran.identity.domain.repositories.AccountAuthRepository;
import com.luketran.identity.infrastructure.persistence.entities.AccountAuthJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AccountAuthJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AccountJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AccountAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountAuthRepositoryAdapter implements AccountAuthRepository {
    private final AccountAuthJpaRepository jpaRepository;
    private final AccountJpaRepository accountJpaRepository;
    private final AccountAuthMapper mapper;

    /**
     * @param fieldType
     * @param fieldValue
     * @return
     */
    @Override
    public Optional<AccountAuth> findByFieldTypeAndFieldValue(AuthFieldType fieldType, String fieldValue) {
        return Optional.ofNullable(mapper.toDomain(jpaRepository.findByFieldTypeAndFieldValue(fieldType, fieldValue)));
    }

    /**
     * @param fieldType
     * @param fieldValue
     * @return
     */
    @Override
    public Optional<AccountAuth> findWithAccount(AuthFieldType fieldType, String fieldValue) {
        return Optional.ofNullable(mapper.toDomain(jpaRepository.findWithAccount(fieldType, fieldValue)));
    }

    @Override
    public List<AccountAuth> findAllByAccountId(UUID accountId) {
        return jpaRepository.findAllByAccountId(accountId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<AccountAuth> findById(UUID id) {
        return Optional.empty();
    }

    /**
     * @return
     */
    @Override
    public List<AccountAuth> findAll() {
        return List.of();
    }

    /**
     * @param accountAuth
     * @return
     */
    @Override
    public AccountAuth save(AccountAuth accountAuth) {
        AccountAuthJpaEntity jpaEntity = mapper.toEntity(accountAuth);
        if (accountAuth.getAccountId() != null) {
            jpaEntity.setAccount(accountJpaRepository.getReferenceById(accountAuth.getAccountId()));
        }
        AccountAuthJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    /**
     * @param id
     */
    @Override
    public void deleteById(UUID id) {

    }

    /**
     * @param id
     * @return
     */
    @Override
    public boolean existsById(UUID id) {
        return false;
    }
}

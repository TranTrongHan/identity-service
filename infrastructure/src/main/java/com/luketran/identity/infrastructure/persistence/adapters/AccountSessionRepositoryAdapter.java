package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.entities.AccountSession;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.AccountSessionRepository;
import com.luketran.identity.infrastructure.persistence.entities.AccountSessionJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AccountSessionJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AccountSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class AccountSessionRepositoryAdapter implements AccountSessionRepository {
    private final AccountSessionJpaRepository jpaRepository;
    private final AccountSessionMapper mapper;
    /**
     * @param id
     * @return
     */
    @Override
    public Optional<AccountSession> findActiveById(UUID id) {
        return Optional.empty();
    }

    /**
     * @param before
     */
    @Override
    public void deleteAllExpiredBefore(LocalDateTime before) {

    }

    /**
     * @param accountId
     * @param appId
     * @return
     */
    @Override
    public Optional<AccountSession> findByAccountIdAndAppId(UUID accountId, UUID appId) {
        return Optional.ofNullable(mapper.toDomain(jpaRepository.findByAccountIdAndAppId(accountId, appId)));
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Optional<AccountSession> findById(UUID id) {
        return Optional.empty();
    }

    /**
     * @return
     */
    @Override
    public List<AccountSession> findAll() {
        return List.of();
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public AccountSession save(AccountSession entity) {
        AccountSessionJpaEntity accountSessionJpaEntity = jpaRepository.save(mapper.toEntity(entity));
        return mapper.toDomain(accountSessionJpaEntity);
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

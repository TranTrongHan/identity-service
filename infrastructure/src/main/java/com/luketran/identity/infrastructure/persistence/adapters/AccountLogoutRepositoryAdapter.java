package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AccountLogout;
import com.luketran.identity.infrastructure.persistence.entities.AccountLogoutJpaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.luketran.identity.domain.repositories.AccountLogoutRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AccountJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AccountLogoutJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AccountLogoutMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountLogoutRepositoryAdapter implements AccountLogoutRepository {
    private final AccountLogoutJpaRepository jpaRepository;
    private final AccountJpaRepository accountJpaRepository;
    private final AccountLogoutMapper mapper;

    @Override
    public Optional<AccountLogout> findByAccountId(UUID accountId) {
        return jpaRepository.findByAccountId(accountId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByAccountId(UUID accountId) {
        jpaRepository.deleteByAccountId(accountId);
    }

    @Override
    public Optional<AccountLogout> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AccountLogout> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public AccountLogout save(AccountLogout entity) {
        AccountLogoutJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        if (entity.getAccountId() != null) {
            jpaEntity.setAccount(accountJpaRepository.getReferenceById(entity.getAccountId()));
        }
        AccountLogoutJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}

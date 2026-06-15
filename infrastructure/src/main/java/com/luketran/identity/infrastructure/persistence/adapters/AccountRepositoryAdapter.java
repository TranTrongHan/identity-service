package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.repositories.AccountRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AccountJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findActiveById(UUID id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Account save(Account account) {
        var jpaEntity = mapper.toJpaEntity(account);
        var saved = jpaRepository.save(jpaEntity);
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

    /**
     * @param account
     * @return
     */
    @Override
    public Account create(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(account)));
    }
}

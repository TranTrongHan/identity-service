package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.repositories.AccountRepository;
import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AccountJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    public Optional<Account> findWithDetails(UUID id) {
        return jpaRepository.findWithDetailsById(id)
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

    @Override
    public Account create(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpaEntity(account)));
    }

    @Override
    public List<Account> findPage(String nameFilter, String statusFilter, int offset, int size) {
        Boolean active = resolveActiveFilter(statusFilter);
        PageRequest pageable = PageRequest.of(offset / Math.max(size, 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AccountJpaEntity> page = jpaRepository.findFiltered(nameFilter, active, pageable);
        return page.getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(String nameFilter, String statusFilter) {
        Boolean active = resolveActiveFilter(statusFilter);
        PageRequest pageable = PageRequest.of(0, 1);
        return jpaRepository.findFiltered(nameFilter, active, pageable).getTotalElements();
    }

    @Override
    public void softDelete(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            jpaRepository.save(entity);
        });
    }

    private Boolean resolveActiveFilter(String statusFilter) {
        if (statusFilter == null) return null;
        return switch (statusFilter.toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> null; // ALL
        };
    }
}

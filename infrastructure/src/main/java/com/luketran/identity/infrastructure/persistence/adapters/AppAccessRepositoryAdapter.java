package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.domain.repositories.AppAccessRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppAccessJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AccountJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppAccessJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppRoleJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppAccessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AppAccessRepositoryAdapter implements AppAccessRepository {

    private final AppAccessJpaRepository jpaRepository;
    private final AccountJpaRepository accountJpaRepository;
    private final AppJpaRepository appJpaRepository;
    private final AppRoleJpaRepository appRoleJpaRepository;
    private final AppAccessMapper mapper;

    @Override
    public Optional<AppAccess> findByAccountIdAndAppId(UUID accountId, UUID appId) {
        return jpaRepository.findByAccountIdAndAppIdAndDeletedAtIsNull(accountId, appId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AppAccess> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AppAccess> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public AppAccess save(AppAccess domain) {
        AppAccessJpaEntity jpaEntity = mapper.toJpaEntity(domain);
        
        if (domain.getAccountId() != null) {
            jpaEntity.setAccount(accountJpaRepository.getReferenceById(domain.getAccountId()));
        }
        if (domain.getAppId() != null) {
            jpaEntity.setApp(appJpaRepository.getReferenceById(domain.getAppId()));
        }
        if (domain.getRoleId() != null) {
            jpaEntity.setRole(appRoleJpaRepository.getReferenceById(domain.getRoleId()));
        }
        
        AppAccessJpaEntity saved = jpaRepository.save(jpaEntity);
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

package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AppRole;
import com.luketran.identity.domain.repositories.AppRoleRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppRoleJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AppJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppRoleJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AppRoleRepositoryAdapter implements AppRoleRepository {

    private final AppRoleJpaRepository jpaRepository;
    private final AppJpaRepository appJpaRepository;
    private final AppRoleMapper mapper;

    @Override
    public Optional<AppRole> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AppRole> findByCodeAndAppId(String code, UUID appId) {
        return jpaRepository.findByCodeAndAppIdAndDeletedAtIsNull(code, appId)
                .map(mapper::toDomain);
    }

    @Override
    public List<AppRole> findAllActiveByAppId(UUID appId) {
        return jpaRepository.findAllByAppIdAndDeletedAtIsNull(appId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AppRole> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public AppRole save(AppRole domain) {
        AppRoleJpaEntity jpaEntity = mapper.toJpaEntity(domain);
        
        if (domain.getAppId() != null) {
            jpaEntity.setApp(appJpaRepository.getReferenceById(domain.getAppId()));
        }
        
        AppRoleJpaEntity saved = jpaRepository.save(jpaEntity);
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

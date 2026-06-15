package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AppPermission;
import com.luketran.identity.domain.repositories.AppPermissionRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppPermissionJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AppJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppPermissionJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AppPermissionRepositoryAdapter implements AppPermissionRepository {

    private final AppPermissionJpaRepository jpaRepository;
    private final AppJpaRepository appJpaRepository;
    private final AppPermissionMapper mapper;

    @Override
    public Optional<AppPermission> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AppPermission> findAllActiveByAppId(UUID appId) {
        return jpaRepository.findAllByAppIdAndDeletedAtIsNull(appId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AppPermission> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public AppPermission save(AppPermission domain) {
        AppPermissionJpaEntity jpaEntity = mapper.toJpaEntity(domain);
        
        if (domain.getAppId() != null) {
            jpaEntity.setApp(appJpaRepository.getReferenceById(domain.getAppId()));
        }
        
        AppPermissionJpaEntity saved = jpaRepository.save(jpaEntity);
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

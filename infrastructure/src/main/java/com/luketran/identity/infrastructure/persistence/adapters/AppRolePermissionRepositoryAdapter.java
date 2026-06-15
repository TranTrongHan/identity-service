package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AppRolePermission;
import com.luketran.identity.domain.repositories.AppRolePermissionRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppRolePermissionJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AppPermissionJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppRoleJpaRepository;
import com.luketran.identity.infrastructure.persistence.jpa.AppRolePermissionJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class AppRolePermissionRepositoryAdapter implements AppRolePermissionRepository {

    private final AppRolePermissionJpaRepository jpaRepository;
    private final AppRoleJpaRepository appRoleJpaRepository;
    private final AppPermissionJpaRepository appPermissionJpaRepository;
    private final AppRolePermissionMapper mapper;

    @Override
    public List<AppRolePermission> findAllByRoleId(UUID roleId) {
        return jpaRepository.findAllByRoleId(roleId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AppRolePermission> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AppRolePermission> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public AppRolePermission save(AppRolePermission domain) {
        AppRolePermissionJpaEntity jpaEntity = mapper.toJpaEntity(domain);
        
        if (domain.getRoleId() != null) {
            jpaEntity.setRole(appRoleJpaRepository.getReferenceById(domain.getRoleId()));
        }
        if (domain.getPermissionId() != null) {
            jpaEntity.setPermission(appPermissionJpaRepository.getReferenceById(domain.getPermissionId()));
        }
        
        AppRolePermissionJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
        jpaRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public void deleteAllByRoleId(UUID roleId) {
        jpaRepository.deleteAllByRoleId(roleId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}

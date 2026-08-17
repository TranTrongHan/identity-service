package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.AppGoogleState;
import com.luketran.identity.domain.repositories.AppGoogleStateRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppGoogleStateJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AppGoogleStateJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppGoogleStateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AppGoogleStateRepositoryAdapter implements AppGoogleStateRepository {

    private final AppGoogleStateJpaRepository jpaRepository;
    private final AppGoogleStateMapper mapper;

    @Override
    public Optional<AppGoogleState> findByAppIdAndState(UUID appId, UUID state) {
        return jpaRepository.findByAppIdAndState(appId, state).map(mapper::toDomain);
    }

    @Override
    public AppGoogleState save(AppGoogleState entity) {
        AppGoogleStateJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        AppGoogleStateJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

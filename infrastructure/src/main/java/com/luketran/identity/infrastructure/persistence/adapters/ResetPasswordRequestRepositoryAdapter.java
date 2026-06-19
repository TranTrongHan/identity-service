package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.ResetPasswordRequest;
import com.luketran.identity.domain.repositories.ResetPasswordRequestRepository;
import com.luketran.identity.infrastructure.persistence.entities.ResetPasswordRequestJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.ResetPasswordRequestJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.ResetPasswordRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResetPasswordRequestRepositoryAdapter implements ResetPasswordRequestRepository {

    private final ResetPasswordRequestJpaRepository jpaRepository;
    private final ResetPasswordRequestMapper mapper;

    @Override
    public Optional<ResetPasswordRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public ResetPasswordRequest save(ResetPasswordRequest entity) {
        ResetPasswordRequestJpaEntity jpaEntity = mapper.toJpaEntity(entity);
        ResetPasswordRequestJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

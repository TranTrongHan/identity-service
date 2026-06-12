package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByIdAndDeletedAtIsNull(UUID id);
}

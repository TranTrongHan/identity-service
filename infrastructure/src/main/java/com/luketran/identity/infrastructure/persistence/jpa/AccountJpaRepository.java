package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;


public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByIdAndDeletedAtIsNull(UUID id);
}

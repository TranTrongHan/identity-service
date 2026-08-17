package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.ResetPasswordRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResetPasswordRequestJpaRepository extends JpaRepository<ResetPasswordRequestJpaEntity, UUID> {
}

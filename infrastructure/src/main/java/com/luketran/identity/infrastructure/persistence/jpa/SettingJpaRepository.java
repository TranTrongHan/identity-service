package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.SettingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingJpaRepository extends JpaRepository<SettingJpaEntity, String> {

    Optional<SettingJpaEntity> findByCode(String code);
}

package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AppJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppJpaRepository extends JpaRepository<AppJpaEntity, UUID> {

    AppJpaEntity findAppByCode(String codeApp);

    List<AppJpaEntity> findAllByDeletedAtIsNull();
}

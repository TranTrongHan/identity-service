package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.Setting;
import com.luketran.identity.domain.enums.SettingCode;
import com.luketran.identity.domain.repositories.SettingRepository;
import com.luketran.identity.infrastructure.persistence.entities.SettingJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.SettingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettingRepositoryAdapter implements SettingRepository {

    private final SettingJpaRepository jpaRepository;

    @Override
    public Optional<Setting> findByCode(SettingCode code) {
        return jpaRepository.findByCode(code.name())
                .map(this::toDomain);
    }

    private Setting toDomain(SettingJpaEntity entity) {
        Setting setting = new Setting();
        setting.setCode(entity.getCode());
        setting.setValue(entity.getValue());
        setting.setDescription(entity.getDescription());
        return setting;
    }
}

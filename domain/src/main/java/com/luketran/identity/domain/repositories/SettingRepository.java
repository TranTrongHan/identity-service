package com.luketran.identity.domain.repositories;

import com.luketran.identity.domain.entities.Setting;
import com.luketran.identity.domain.enums.SettingCode;

import java.util.Optional;

/**
 * Repository cho Setting.
 * Không extends BaseRepository vì Setting dùng String code làm PK, không phải UUID.
 */
public interface SettingRepository {

    Optional<Setting> findByCode(SettingCode code);

    /**
     * Lấy giá trị setting, fallback về defaultValue nếu không tìm thấy trong DB.
     */
    default String getValueOrDefault(SettingCode code) {
        return findByCode(code)
                .map(Setting::getValue)
                .orElse(code.getDefaultValue());
    }

    /**
     * Lấy giá trị setting dạng int.
     */
    default int getIntValueOrDefault(SettingCode code) {
        return Integer.parseInt(getValueOrDefault(code));
    }
}

package com.luketran.identity.infrastructure.persistence.converters;

import com.luketran.identity.domain.enums.AuthFieldType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter: AuthFieldType enum ↔ Integer column trong DB.
 *
 * autoApply = true → tự động áp dụng cho MỌI field kiểu AuthFieldType
 * trong tất cả JPA entities, không cần ghi @Convert trên từng field.
 */
@Converter(autoApply = true)
public class AuthFieldTypeConverter implements AttributeConverter<AuthFieldType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AuthFieldType type) {
        if (type == null) return null;
        return type.getValue();  // USERNAME → 1, EMAIL → 2, PHONE → 3
    }

    @Override
    public AuthFieldType convertToEntityAttribute(Integer value) {
        if (value == null) return null;
        return AuthFieldType.fromValue(value);  // 1 → USERNAME, 2 → EMAIL, 3 → PHONE
    }
}

package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.ResetPasswordRequest;
import com.luketran.identity.infrastructure.persistence.entities.ResetPasswordRequestJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResetPasswordRequestMapper {

    ResetPasswordRequest toDomain(ResetPasswordRequestJpaEntity entity);

    ResetPasswordRequestJpaEntity toJpaEntity(ResetPasswordRequest domain);
}

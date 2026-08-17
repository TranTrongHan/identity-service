package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppGoogleState;
import com.luketran.identity.infrastructure.persistence.entities.AppGoogleStateJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppGoogleStateMapper {

    AppGoogleState toDomain(AppGoogleStateJpaEntity entity);

    AppGoogleStateJpaEntity toJpaEntity(AppGoogleState domain);
}

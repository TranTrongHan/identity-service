package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.infrastructure.persistence.entities.AppAccessJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppAccessMapper {
    AppAccess toDomain(AppAccessJpaEntity entity);
    AppAccessJpaEntity toJpaEntity(AppAccess domain);
}

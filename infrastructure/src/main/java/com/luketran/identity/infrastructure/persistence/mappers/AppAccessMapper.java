package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.infrastructure.persistence.entities.AppAccessJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AppMapper.class, AppRoleMapper.class})
public interface AppAccessMapper {
    AppAccess toDomain(AppAccessJpaEntity entity);

    @Mapping(target = "app", ignore = true)
    @Mapping(target = "role", ignore = true)
    AppAccessJpaEntity toJpaEntity(AppAccess domain);
}

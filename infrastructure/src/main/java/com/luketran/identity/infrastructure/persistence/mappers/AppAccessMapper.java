package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppAccess;
import com.luketran.identity.infrastructure.persistence.entities.AppAccessJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {AppMapper.class, AppRoleMapper.class})
public interface AppAccessMapper {
    @Mapping(target = "app", ignore = true)
    @Mapping(target = "role", ignore = true)
    AppAccess toDomain(AppAccessJpaEntity entity);

    @Named("toDomainWithDetails")
    AppAccess toDomainWithDetails(AppAccessJpaEntity entity);

    @Mapping(target = "app", ignore = true)
    @Mapping(target = "role", ignore = true)
    AppAccessJpaEntity toJpaEntity(AppAccess domain);
}


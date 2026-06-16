package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppRole;
import com.luketran.identity.infrastructure.persistence.entities.AppRoleJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppRoleMapper {
    AppRole toDomain(AppRoleJpaEntity entity);

    @Mapping(target = "permissionItems", ignore = true)
    @Mapping(target = "app", ignore = true)
    AppRoleJpaEntity toJpaEntity(AppRole domain);
}

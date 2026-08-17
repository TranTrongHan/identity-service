package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppRolePermission;
import com.luketran.identity.infrastructure.persistence.entities.AppRolePermissionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppRolePermissionMapper {
    AppRolePermission toDomain(AppRolePermissionJpaEntity entity);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "permission", ignore = true)
    AppRolePermissionJpaEntity toJpaEntity(AppRolePermission domain);
}

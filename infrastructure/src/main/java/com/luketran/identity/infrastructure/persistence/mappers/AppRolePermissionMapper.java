package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppRolePermission;
import com.luketran.identity.infrastructure.persistence.entities.AppRolePermissionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppRolePermissionMapper {
    AppRolePermission toDomain(AppRolePermissionJpaEntity entity);
    AppRolePermissionJpaEntity toJpaEntity(AppRolePermission domain);
}

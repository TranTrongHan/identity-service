package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppPermission;
import com.luketran.identity.infrastructure.persistence.entities.AppPermissionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppPermissionMapper {
    AppPermission toDomain(AppPermissionJpaEntity entity);
    AppPermissionJpaEntity toJpaEntity(AppPermission domain);
}

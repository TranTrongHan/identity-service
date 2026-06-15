package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AppRole;
import com.luketran.identity.infrastructure.persistence.entities.AppRoleJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppRoleMapper {
    AppRole toDomain(AppRoleJpaEntity entity);
    AppRoleJpaEntity toJpaEntity(AppRole domain);
}

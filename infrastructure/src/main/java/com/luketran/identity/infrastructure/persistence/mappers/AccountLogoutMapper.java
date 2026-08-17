package com.luketran.identity.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.luketran.identity.domain.entities.AccountLogout;
import com.luketran.identity.infrastructure.persistence.entities.AccountLogoutJpaEntity;

@Mapper(componentModel = "spring")
public interface AccountLogoutMapper {
    AccountLogout toDomain(AccountLogoutJpaEntity jpaEntity);

    @Mapping(target = "account", ignore = true)
    AccountLogoutJpaEntity toJpaEntity(AccountLogout domain);
}


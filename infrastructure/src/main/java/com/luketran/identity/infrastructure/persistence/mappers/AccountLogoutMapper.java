package com.luketran.identity.infrastructure.persistence.mappers;

import org.mapstruct.Mapper;

import com.luketran.identity.domain.entities.AccountLogout;
import com.luketran.identity.infrastructure.persistence.entities.AccountLogoutJpaEntity;

@Mapper(componentModel = "spring")
public interface AccountLogoutMapper {
    AccountLogout toDomain(AccountLogoutJpaEntity jpaEntity);

    AccountLogoutJpaEntity toJpaEntity(AccountLogout domain);
}

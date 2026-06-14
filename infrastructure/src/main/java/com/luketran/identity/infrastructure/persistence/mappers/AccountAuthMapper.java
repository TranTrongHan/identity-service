package com.luketran.identity.infrastructure.persistence.mappers;


import com.luketran.identity.domain.entities.AccountAuth;
import com.luketran.identity.infrastructure.persistence.entities.AccountAuthJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountAuthMapper {
    AccountAuth toDomain(AccountAuthJpaEntity accountAuthJpaEntity);

}

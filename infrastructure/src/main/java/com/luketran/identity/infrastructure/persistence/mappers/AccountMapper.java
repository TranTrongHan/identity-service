package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toDomain(AccountJpaEntity entity);

    AccountJpaEntity toJpaEntity(Account domain);
}

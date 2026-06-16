package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AccountAuthMapper.class, AppAccessMapper.class, AccountSessionMapper.class})
public interface AccountMapper {

    Account toDomain(AccountJpaEntity entity);

    @Mapping(target = "authMethods", ignore = true)
    @Mapping(target = "appAccesses", ignore = true)
    @Mapping(target = "sessions", ignore = true)
    AccountJpaEntity toJpaEntity(Account domain);
}

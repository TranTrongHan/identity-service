package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AccountSession;
import com.luketran.identity.infrastructure.persistence.entities.AccountSessionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AppMapper.class})
public interface AccountSessionMapper {
    AccountSession toDomain(AccountSessionJpaEntity jpaEntity);

    @Mapping(target = "app", ignore = true)
    @Mapping(target = "account", ignore = true)
    AccountSessionJpaEntity toEntity(AccountSession entity);
}

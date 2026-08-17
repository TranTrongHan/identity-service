package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AccountSession;
import com.luketran.identity.infrastructure.persistence.entities.AccountSessionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {AppMapper.class})
public interface AccountSessionMapper {
    @Mapping(target = "app", ignore = true)
    AccountSession toDomain(AccountSessionJpaEntity jpaEntity);

    @Named("toDomainWithDetails")
    AccountSession toDomainWithDetails(AccountSessionJpaEntity jpaEntity);

    @Mapping(target = "app", ignore = true)
    @Mapping(target = "account", ignore = true)
    AccountSessionJpaEntity toEntity(AccountSession entity);
}


package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.AccountSession;
import com.luketran.identity.infrastructure.persistence.entities.AccountSessionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountSessionMapper {
    AccountSession toDomain(AccountSessionJpaEntity jpaEntity);

    AccountSessionJpaEntity toEntity(AccountSession entity);
}

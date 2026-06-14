package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.infrastructure.persistence.entities.AppJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppMapper {
    App toDomain(AppJpaEntity appJpaEntity);
}

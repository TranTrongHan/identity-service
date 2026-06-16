package com.luketran.identity.infrastructure.persistence.mappers;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.infrastructure.persistence.entities.AppJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppMapper {
    App toDomain(AppJpaEntity appJpaEntity);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "accesses", ignore = true)
    AppJpaEntity toJpaEntity(App app);
}

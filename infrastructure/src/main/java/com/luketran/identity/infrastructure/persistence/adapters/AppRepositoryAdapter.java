package com.luketran.identity.infrastructure.persistence.adapters;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.repositories.AppRepository;
import com.luketran.identity.infrastructure.persistence.entities.AppJpaEntity;
import com.luketran.identity.infrastructure.persistence.jpa.AppJpaRepository;
import com.luketran.identity.infrastructure.persistence.mappers.AppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AppRepositoryAdapter implements AppRepository {
    private final AppJpaRepository jpaRepository;
    private final AppMapper mapper;
    /**
     * @param id
     * @return
     */
    @Override
    public Optional<App> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    /**
     * @return
     */
    @Override
    public List<App> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    /**
     * @param code
     * @return
     */
    @Override
    public Optional<App> findByCode(String code) {
        AppJpaEntity entity = jpaRepository.findAppByCode(code);
        return entity == null ? Optional.empty() : Optional.of(mapper.toDomain(entity));
    }


    /**
     * @param app
     * @return
     */
    @Override
    public App save(App app) {
        AppJpaEntity jpaEntity = mapper.toJpaEntity(app);
        AppJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    /**
     * @param id
     */
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}

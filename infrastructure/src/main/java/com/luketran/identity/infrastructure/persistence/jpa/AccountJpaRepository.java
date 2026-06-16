package com.luketran.identity.infrastructure.persistence.jpa;

import com.luketran.identity.infrastructure.persistence.entities.AccountJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT a FROM AccountJpaEntity a " +
           "LEFT JOIN FETCH a.authMethods " +
           "LEFT JOIN FETCH a.appAccesses aa LEFT JOIN FETCH aa.app LEFT JOIN FETCH aa.role " +
           "LEFT JOIN FETCH a.sessions s LEFT JOIN FETCH s.app " +
           "WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<AccountJpaEntity> findWithDetailsById(@Param("id") UUID id);

    @Query("SELECT a FROM AccountJpaEntity a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:active IS NULL OR (:active = true AND a.deletedAt IS NULL) OR (:active = false AND a.deletedAt IS NOT NULL))")
    Page<AccountJpaEntity> findFiltered(@Param("name") String name,
                                        @Param("active") Boolean active,
                                        Pageable pageable);
}

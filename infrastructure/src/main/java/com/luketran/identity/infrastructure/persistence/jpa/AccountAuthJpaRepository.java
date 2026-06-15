package com.luketran.identity.infrastructure.persistence.jpa;


import com.luketran.identity.domain.enums.AuthFieldType;
import com.luketran.identity.infrastructure.persistence.entities.AccountAuthJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountAuthJpaRepository extends JpaRepository<AccountAuthJpaEntity, UUID> {
    AccountAuthJpaEntity findByFieldTypeAndFieldValue(AuthFieldType fieldType, String fieldValue);

    @Query("SELECT a FROM AccountAuthJpaEntity a JOIN FETCH a.account " +
           "WHERE a.fieldType = :fieldType AND a.fieldValue = :fieldValue")
    AccountAuthJpaEntity findWithAccount(@Param("fieldType") AuthFieldType fieldType, @Param("fieldValue") String fieldValue);
}

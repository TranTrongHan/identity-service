package com.luketran.identity.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "app_permission")
public class AppPermissionJpaEntity extends BaseJpaEntity {

    @Column(name = "app_id", nullable = false)
    private UUID appId;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "group_name", length = 500)
    private String groupName;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "include_permission_codes", columnDefinition = "jsonb")
    private List<String> includePermissionCodes;
}

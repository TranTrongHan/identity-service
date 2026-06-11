package com.luketran.identity.identity_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_permission")
@Getter
@Setter
@NoArgsConstructor
public class AppPermission extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", insertable = false, updatable = false)
    private App app;
}

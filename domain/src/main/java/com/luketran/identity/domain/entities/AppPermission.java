package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AppPermission extends BaseEntity {

    private UUID appId;

    private String code;

    private String name;

    private String groupName;

    private String description;

    private List<String> includePermissionCodes;
}

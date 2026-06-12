package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AppRolePermission {

    private UUID id;

    private UUID roleId;

    private UUID permissionId;
}

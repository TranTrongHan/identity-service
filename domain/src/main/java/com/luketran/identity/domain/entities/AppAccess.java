package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AppAccess extends BaseEntity {

    private UUID accountId;

    private UUID appId;

    private UUID roleId;

    private String scope;

    // === Relations (populated via JOIN FETCH when needed) ===
    private App app;

    private AppRole role;
}

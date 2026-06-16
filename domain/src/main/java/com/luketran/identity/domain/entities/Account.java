package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Account extends BaseEntity {

    private String name;

    private String avatarUrl;

    private String secretKey;

    private String password;

    private int wrongLoginCount;

    private LocalDateTime accessDeniedUntil;

    // === Relations (populated only when loaded via findWithDetails) ===
    private List<AccountAuth> authMethods;

    private List<AppAccess> appAccesses;

    private List<AccountSession> sessions;
}

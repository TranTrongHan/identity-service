package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class App extends BaseEntity {

    private String code;

    private String name;

    private String description;

    private String signingKey;

    private int tokenLifetimeMinutes;

    private int sessionLifetimeMinutes;

    // === Google OAuth ===
    private String googleClientId;

    private String googleClientSecret;

    private String googleAllowedDomain;

    // === Reset Password ===
    private String resetPasswordUrlTemplate;

    // === Relations ===
    private List<AppRole> roles;

    private List<AppPermission> permissions;

    private List<AppAccess> accesses;
}

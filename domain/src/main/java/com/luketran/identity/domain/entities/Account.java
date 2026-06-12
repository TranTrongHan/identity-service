package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Account extends BaseEntity {

    private String name;

    private String avatarUrl;

    private String secretKey;

    private String password;

    private int wrongLoginCount;

    private LocalDateTime accessDeniedUntil;
}

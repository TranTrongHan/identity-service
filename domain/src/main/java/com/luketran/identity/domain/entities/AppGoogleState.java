package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AppGoogleState extends BaseEntity {

    private UUID appId;

    private UUID state;

    private String redirectUri;

    private LocalDateTime expiredAt;
}

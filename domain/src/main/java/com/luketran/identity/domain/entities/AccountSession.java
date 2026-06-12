package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AccountSession {

    private UUID id;

    private UUID accountId;

    private UUID appId;

    private LocalDateTime expiredAt;

    private LocalDateTime createdAt;
}

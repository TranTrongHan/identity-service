package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ResetPasswordRequest extends BaseEntity {

    private UUID appId;

    private UUID accountId;

    private LocalDateTime expiredAt;
}

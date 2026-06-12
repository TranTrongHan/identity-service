package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseEntity {

    private UUID id;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;
}

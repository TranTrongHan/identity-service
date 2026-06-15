package com.luketran.identity.domain.entities;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountLogout extends BaseEntity {
    private UUID accountId;
}

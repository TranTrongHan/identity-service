package com.luketran.identity.domain.entities;

import com.luketran.identity.domain.enums.AuthFieldType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountAuth extends BaseEntity {

    private UUID accountId;

    private AuthFieldType fieldType;

    private String fieldValue;
}

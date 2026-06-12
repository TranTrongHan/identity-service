package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AppRole extends BaseEntity {

    private UUID appId;

    private String code;

    private String name;
}

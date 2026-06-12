package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class App extends BaseEntity {

    private String code;

    private String name;

    private String description;

    private String signingKey;

    private int tokenLifetimeMinutes;

    private int sessionLifetimeMinutes;
}

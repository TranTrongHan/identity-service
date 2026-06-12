package com.luketran.identity.domain.enums;

public enum AuthFieldType {

    USERNAME(1),
    EMAIL(2),
    PHONE(3);

    private final int value;

    AuthFieldType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthFieldType fromValue(int value) {
        for (AuthFieldType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AuthFieldType value: " + value);
    }
}

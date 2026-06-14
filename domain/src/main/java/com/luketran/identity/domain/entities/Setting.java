package com.luketran.identity.domain.entities;

import lombok.Getter;
import lombok.Setter;

/**
 * Setting entity — cấu hình hệ thống dạng key-value.
 * Primary key là code (String), không extends BaseEntity (không cần UUID/timestamps).
 */
@Getter
@Setter
public class Setting {

    private String code;

    private String value;

    private String description;
}

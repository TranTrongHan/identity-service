package com.luketran.identity.domain.enums;

/**
 * Mã định danh cho các setting trong hệ thống.
 * Tương ứng 1:1 với column `code` trong table `setting`.
 */
public enum SettingCode {

    /**
     * Số lần login sai cho phép trước khi bị khóa tạm.
     * Default: 3
     */
    MAX_WRONG_LOGIN_ALLOWED("3"),

    /**
     * Số phút phải chờ mỗi lần login sai (progressive lockout).
     * Formula: (wrongCount - maxAllowed + 1) * waitMinute
     * Default: 5
     */
    WAIT_MINUTE_PER_WRONG_LOGIN("5");

    private final String defaultValue;

    SettingCode(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

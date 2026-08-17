package com.luketran.identity.webapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình Identity tương tự IdentityAppsetting bên .NET.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "identity")
public class IdentityProperties {

    /**
     * Danh sách User ID được phép bypass kiểm tra thời hạn token (ValidateLifetime = false).
     */
    private List<String> bypassUserIds = new ArrayList<>();
}

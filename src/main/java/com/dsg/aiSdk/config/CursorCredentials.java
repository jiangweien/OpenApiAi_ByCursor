package com.dsg.aiSdk.config;

import java.util.Objects;

/**
 * Cursor Cloud API 凭证：在 Cursor Dashboard 创建 API Key，使用 Basic 认证（用户名=Key，密码为空）。
 */
public final class CursorCredentials {

    private final String apiKey;
    private final String baseUrl;

    public CursorCredentials(String apiKey) {
        this(apiKey, "https://api.cursor.com");
    }

    public CursorCredentials(String apiKey, String baseUrl) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey").trim();
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl").replaceAll("/+$", "");
        if (this.apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey is empty");
        }
    }

    public String apiKey() {
        return apiKey;
    }

    public String baseUrl() {
        return baseUrl;
    }
}

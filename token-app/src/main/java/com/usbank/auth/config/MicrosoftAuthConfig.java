package com.usbank.auth.config;

import java.util.Optional;

/**
 * Configuration utility for Microsoft Authentication credentials.
 */
public class MicrosoftAuthConfig {
    private static final String MS_LOGIN_URL_ENV = "MS_LOGIN_URL";
    private static final String CLIENT_ID_ENV = "MS_CLIENT_ID";
    private static final String CLIENT_SECRET_ENV = "MS_CLIENT_SECRET";
    private static final String TENANT_ID_ENV = "MS_TENANT_ID";

    /**
     * Retrieves Microsoft login URL from environment variable.
     *
     * @return Microsoft login URL
     * @throws IllegalStateException if URL is not configured
     */
    public static String getMicrosoftLoginUrl() {
        return Optional.ofNullable(System.getenv(MS_LOGIN_URL_ENV))
                .orElseThrow(() -> new IllegalStateException("Microsoft login URL not configured"));
    }

    /**
     * Retrieves client ID from environment variable.
     *
     * @return Microsoft client ID
     * @throws IllegalStateException if client ID is not configured
     */
    public static String getClientId() {
        return Optional.ofNullable(System.getenv(CLIENT_ID_ENV))
                .orElseThrow(() -> new IllegalStateException("Microsoft client ID not configured"));
    }

    /**
     * Retrieves client secret from environment variable.
     *
     * @return Microsoft client secret
     * @throws IllegalStateException if client secret is not configured
     */
    public static String getClientSecret() {
        return Optional.ofNullable(System.getenv(CLIENT_SECRET_ENV))
                .orElseThrow(() -> new IllegalStateException("Microsoft client secret not configured"));
    }

    /**
     * Retrieves tenant ID from environment variable.
     *
     * @return Microsoft tenant ID
     * @throws IllegalStateException if tenant ID is not configured
     */
    public static String getTenantId() {
        return Optional.ofNullable(System.getenv(TENANT_ID_ENV))
                .orElseThrow(() -> new IllegalStateException("Microsoft tenant ID not configured"));
    }
}
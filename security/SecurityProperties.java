package com.decisionhub.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class mapping security limits and JWT parameters.
 */
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class SecurityProperties {

    /**
     * Secret key for signing access tokens. Must be minimum 256 bits for HS256.
     */
    private String secret;

    /**
     * Expiration duration of access tokens (milliseconds). Default is 15 minutes.
     */
    private long expirationMs = 900000;

    /**
     * Expiration duration of refresh tokens (milliseconds). Default is 7 days.
     */
    private long refreshExpirationMs = 604800000;
}

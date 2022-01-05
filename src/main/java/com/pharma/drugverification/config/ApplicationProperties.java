package com.pharma.drugverification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperties {

    private JwtProperties jwt = new JwtProperties();
    private SecurityProperties security = new SecurityProperties();
    private HsmProperties hsm = new HsmProperties();
    private VerificationProperties verification = new VerificationProperties();

    @Data
    public static class JwtProperties {
        private String secret;
        private long expiration;
        private long refreshExpiration;
    }

    @Data
    public static class SecurityProperties {
        private int maxFailedAttempts;
        private int lockoutDurationMinutes;
    }

    @Data
    public static class HsmProperties {
        private String pkcs11Library;
        private int slotIndex;
        private String pin;
    }

    @Data
    public static class VerificationProperties {
        private int cacheTtlSeconds;
        private double maxDistanceMeters;
        private int minTimeBetweenScansSeconds;
    }
}

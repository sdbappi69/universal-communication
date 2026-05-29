package com.planet0088.universalCommunications.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "uacp.jwt")
public class JwtConfig {
    private String secret;
    private long expirationMs;
}

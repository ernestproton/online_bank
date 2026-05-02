package com.example.online_bank.config;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import java.time.Duration;

@Slf4j
@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class JwtConfig {
    private Duration accessTokenLifetime;
    private Duration refreshTokenLifetime;
    private Duration idTokenLifetime;
    private Duration notBefore;
    private String fileName;
    private String audience;
    private String issuer;
    private SecretKey key;
}

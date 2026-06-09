package com.example.online_bank.security.jwt.factory.impl;

import com.example.online_bank.config.JwtConfig;
import com.example.online_bank.domain.dto.UserContainer;
import com.example.online_bank.enums.TokenType;
import com.example.online_bank.security.jwt.factory.TokenProvider;
import com.example.online_bank.service.JwtService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdTokenProvider implements TokenProvider {
    private final JwtConfig config;
    private final JwtService jwtService;

    /**
     * @param userContainer - refreshToken - Информация о пользователе
     * @return Токен Id
     * имя
     */
    //TODO: добавить фотографию профиля пользователю и подгружать через Amazon S3
    @Override
    public String create(TokenType type, UserContainer userContainer) {
        log.info("Creating IdToken");

        Date issuedDate = new Date();
        Date notBeforeDate = issuedDate;
        Date expiredDate = new Date(issuedDate.getTime() + config.getIdTokenLifetime().toMillis());

        String subject = userContainer.uuid();

        Map<String, Object> claims = jwtService.createClaims();
        claims.put("token_type", type);

        String id = jwtService.createUuid();

        String token = Jwts.builder()
                .subject(subject)
                .issuer(config.getIssuer())
                .id(id)
                .notBefore(notBeforeDate)
                .expiration(expiredDate)
                .signWith(config.getKey())
                .audience().add(config.getAudience())
                .and()
                .claims(claims)
                .issuedAt(issuedDate)
                .compact();

        log.info("id refreshToken created {}", token);
        return token;
    }

    /**
     * @param supported
     * @return
     */
    @Override
    public boolean supports(TokenType supported) {
        return supported == TokenType.ID;
    }
}

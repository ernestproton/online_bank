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
import java.util.List;
import java.util.Map;

import static com.example.online_bank.enums.TokenType.ACCESS;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccessTokenProvider implements TokenProvider {
    private final JwtConfig config;
    private final JwtService jwtService;

    @Override
    public String create(TokenType type, UserContainer userContainer) {

        log.trace("Создание дат");
        Date issuedDate = new Date();
        Date notBeforeDate = issuedDate;
        Date expiredDate = new Date(issuedDate.getTime() + config.getAccessTokenLifetime().toMillis());

        log.trace("Получение uuid пользователя");
        String subject = userContainer.uuid();
        log.trace("Получение ролей пользователя");
        List<String> subjectRoles = userContainer.roles();

        Map<String, Object> claims = jwtService.createClaims();

        log.trace("Помещаем значения в клаймы");
        claims.put("roles", subjectRoles);
        claims.put("token_type", type);
        claims.put("name", userContainer.name());

        String id = jwtService.createUuid();

        log.info("Собираем токен");
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

        //todo сделать тестовые логи
        log.info("access refreshToken created {}", token);
        return token;
    }

    @Override
    public boolean supports(TokenType supported) {
        return supported == ACCESS;
    }
}
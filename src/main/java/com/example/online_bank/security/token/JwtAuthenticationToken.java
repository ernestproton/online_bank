package com.example.online_bank.security.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    /**
     * Мы храним jwt refreshToken, в объекте JwtAuthenticationToken потому что JwtAuthenticationToken имеет два состояния:
     * до аутентификации и после аутентификации
     */
    @Getter
    private String token;
    private final Object principal;

    /**
     * Этот Конструктор для пользователя, который еще не был аутентифицирован
     */
    public JwtAuthenticationToken(String token) {
        super(null);
        setAuthenticated(false);
        this.token = token;
        this.principal = null;
    }

    /**
     * Этот конструктор может использоваться только, когда пользователь был аутентифицирован
     */
    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal) {
        super(authorities);
        setAuthenticated(true);
        this.principal = principal;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return super.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isAuthenticated() {
        return super.isAuthenticated();
    }
}

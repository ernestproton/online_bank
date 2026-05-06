package com.example.online_bank.security.config;

import com.example.online_bank.security.filter.JwtRequestFilter;
import com.example.online_bank.security.provider.JwtRequestProvider;
import com.example.online_bank.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String corsUrl;

    //    Реализация фильтра для настройки конечных точек протокола
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRequestFilter jwtRequestFilter) throws
            Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authRequestManager ->
                        authRequestManager
                                .requestMatchers(
                                        "/api/verification-code/update",
                                        "/api/sign-up",
                                        "/api/sign-up/admin",
                                        "/api/first-verify/email",
                                        "/api/default-verify/email",
                                        "/api/token/get-access-token",
                                        "/api/login",
                                        "/api/silent"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-resources/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .requestMatchers(
                                        "/api/currency/find-rate",
                                        "/api/currency/convert"
                                ).permitAll()
                                .requestMatchers(
                                        "/api/test/pure",
                                        "/api/test/send-email"
                                ).permitAll()
                                .requestMatchers(
                                        "/api/quest"
                                ).permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(
                                        "/api/bank-partner",
                                        "api/bank-partner/find-by-containing-name"
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )

                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(STATELESS))

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                                .accessDeniedHandler(((request, response, accessDeniedException) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN)))
                )
                .addFilterBefore(jwtRequestFilter, BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true); // Для JWT/Cookies
        configuration.setAllowedOrigins(List.of(
                corsUrl
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, UserService userService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userService);
        return daoAuthenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider,
            JwtRequestProvider jwtRequestProvider) {
        return new ProviderManager(Arrays.asList(daoAuthenticationProvider, jwtRequestProvider));
    }
}

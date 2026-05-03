package com.example.online_bank.security.config;

import com.example.online_bank.security.filter.JwtRequestFilter;
import com.example.online_bank.security.provider.JwtRequestProvider;
import com.example.online_bank.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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

    //    @Value("${app.cors.allowed-origins}")
//    private String corsUrl;
    private static final String url = "https://online-bank-hyper-revolution-computer-systems-8zcoa3c4f.vercel.app/";

    //    Реализация фильтра для настройки конечных точек протокола
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRequestFilter jwtRequestFilter) throws
            Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(dirtyCorsConfigurationSource()))
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
                                        "/api/bank-partner"
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
    // @Profile("dirty-config")
    public CorsConfigurationSource dirtyCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // РЕЖИМ "ПУСКАТЬ ВСЕХ":
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedOrigins(List.of(
                "https://online-bank-hyper-revolution-comput.vercel.app/",
                "https://online-bank-hyper-revolution-git-cf49f5-amirgilmanovs-projects.vercel.app/",
                "https://online-bank-hyper-revolution-computer-systems-iillnjp9z.vercel.app/",
                "http://localhost:3000/"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Для JWT/Cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

//    @Bean
//    //@Profile("!dirty-config")
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Указываем разрешенные источники
//        configuration.setAllowedOrigins(List.of(corsUrl));
//
//        // Разрешаем основные HTTP-методы
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//
//        // Разрешаем заголовки (важно для JWT и Content-Type)
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
//
//        // Если используешь куки или передаешь Authorization Header
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // Применяем настройки ко всем путям
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

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
    public AuthenticationManager authenticationManager
            (
                    DaoAuthenticationProvider daoAuthenticationProvider,
                    JwtRequestProvider jwtRequestProvider) {
        return new ProviderManager(Arrays.asList(daoAuthenticationProvider, jwtRequestProvider));
    }
}

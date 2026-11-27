package com.sebastian.spring.mas.aliado.aliado.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.web.ApiError;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionAuthFilter sessionAuthFilter(SesionPort sesionService, ObjectMapper objectMapper) {
        return new SessionAuthFilter(sesionService, objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionAuthFilter sessionAuthFilter,
                                           ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/prestadores/*/confianza").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/perfil/*/publico").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())

                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(req, res, objectMapper, HttpStatus.UNAUTHORIZED,
                                        "NO_AUTENTICADO", "Se requiere autenticación."))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(req, res, objectMapper, HttpStatus.FORBIDDEN,
                                        "ACCESO_DENEGADO", "No tienes permiso para realizar esta acción.")))
                .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void escribirError(HttpServletRequest req, HttpServletResponse res, ObjectMapper objectMapper,
                                      HttpStatus status, String code, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        ApiError error = new ApiError(OffsetDateTime.now(), status.value(), code, message,
                req.getRequestURI(), UUID.randomUUID().toString());
        res.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

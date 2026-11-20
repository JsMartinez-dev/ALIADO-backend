package com.sebastian.spring.mas.aliado.aliado.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.web.ApiError;
import com.sebastian.spring.mas.aliado.aliado.common.web.CurrentUser;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CuentaBloqueadaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autenticación por sesión opaca (plan general, sección 6.2; SPEC-01).
 * El cliente envía Authorization: Bearer <token>; en BD solo vive el hash SHA-256.
 * Las rutas públicas y la documentación quedan fuera del filtro.
 */
public class SessionAuthFilter extends OncePerRequestFilter {
    private final SesionPort sesionService;
    private final ObjectMapper objectMapper;

    public SessionAuthFilter(SesionPort sesionService, ObjectMapper objectMapper) {
        this.sesionService = sesionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        return ruta.startsWith("/api/v1/auth/")
                || ruta.startsWith("/swagger-ui/")
                || ruta.startsWith("/v3/api-docs/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring("Bearer ".length()).trim();
            try {
                sesionService.validar(token).ifPresent(usuario -> {
                    CurrentUser principal = new CurrentUser(usuario.id(), usuario.email(), usuario.rol().name());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } catch (CuentaBloqueadaException ex) {
                escribirError(request, response, "CUENTA_BLOQUEADA", ex.getMessage());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void escribirError(HttpServletRequest request, HttpServletResponse response, String code, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        ApiError error = new ApiError(OffsetDateTime.now(), HttpServletResponse.SC_FORBIDDEN, code, message,
                request.getRequestURI(), UUID.randomUUID().toString());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

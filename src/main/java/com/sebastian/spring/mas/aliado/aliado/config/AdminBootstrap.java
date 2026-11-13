package com.sebastian.spring.mas.aliado.aliado.config;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.AuthService;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Crea la cuenta ADMINISTRADOR solo si se definen ALIADO_ADMIN_EMAIL y
 * ALIADO_ADMIN_PASSWORD en el entorno. El rol ADMINISTRADOR no es
 * registrable por la API pública (SPEC-01/SPEC-12). Sin estas variables no se crea nada.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final AuthService authService;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(AuthService authService,
                          @Value("${aliado.admin.email:}") String adminEmail,
                          @Value("${aliado.admin.password:}") String adminPassword) {
        this.authService = authService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Sin ALIADO_ADMIN_EMAIL/PASSWORD: no se crea cuenta administradora.");
            return;
        }
        try {
            authService.crearAdministrador(adminEmail, adminPassword);
            log.info("Cuenta administradora lista para {}.", adminEmail);
        } catch (BusinessException.Conflict ex) {
            log.info("Cuenta administradora ya existente para {}.", adminEmail);
        }
    }
}

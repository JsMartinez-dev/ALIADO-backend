package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorCodigoRecuperacion;
import java.security.SecureRandom;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class GeneradorCodigoRecuperacionSeguro implements GeneradorCodigoRecuperacion {
    private static final int CANTIDAD_CODIGOS = 1_000_000;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generar() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(CANTIDAD_CODIGOS));
    }
}

package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorTokenSesion;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class GeneradorTokenSeguroAdapter implements GeneradorTokenSesion {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generar() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

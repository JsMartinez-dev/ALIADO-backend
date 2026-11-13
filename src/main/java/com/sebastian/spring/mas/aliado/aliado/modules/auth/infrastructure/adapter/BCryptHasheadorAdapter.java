package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HasheadorPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptHasheadorAdapter implements HasheadorPassword {
    private final PasswordEncoder passwordEncoder;
    private final String hashFicticio;

    public BCryptHasheadorAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.hashFicticio = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
    }

    @Override
    public String hashear(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verificar(String password, String passwordHash) {
        boolean coincide = passwordEncoder.matches(password, passwordHash == null ? hashFicticio : passwordHash);
        return passwordHash != null && coincide;
    }
}

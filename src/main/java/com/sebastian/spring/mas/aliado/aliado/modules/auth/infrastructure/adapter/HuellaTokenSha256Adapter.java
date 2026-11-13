package com.sebastian.spring.mas.aliado.aliado.modules.auth.infrastructure.adapter;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class HuellaTokenSha256Adapter implements HuellaToken {
    @Override
    public String calcular(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible.", ex);
        }
    }
}

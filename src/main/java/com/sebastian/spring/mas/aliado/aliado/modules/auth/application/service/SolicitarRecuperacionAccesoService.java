package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.SolicitarRecuperacionCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.SolicitarRecuperacionAccesoUseCase;
import com.sebastian.spring.mas.aliado.aliado.common.correo.CorreoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.TokenRecuperacion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.TokenRecuperacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorCodigoRecuperacion;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SolicitarRecuperacionAccesoService implements SolicitarRecuperacionAccesoUseCase {
    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final GeneradorCodigoRecuperacion generador;
    private final HuellaToken huella;
    private final CorreoPort correo;
    private final Clock clock;

    public SolicitarRecuperacionAccesoService(UsuarioRepository usuarios, TokenRecuperacionRepository tokens,
            GeneradorCodigoRecuperacion generador, HuellaToken huella, CorreoPort correo, Clock clock) {
        this.usuarios = usuarios; this.tokens = tokens; this.generador = generador;
        this.huella = huella; this.correo = correo; this.clock = clock;
    }

    @Override
    public void ejecutar(SolicitarRecuperacionCommand command) {
        String email = Usuario.normalizarEmail(command.email());
        usuarios.porEmail(email).ifPresent(usuario -> {
            String codigo = generador.generar();
            OffsetDateTime ahora = OffsetDateTime.now(clock);
            tokens.guardar(new TokenRecuperacion(UUID.randomUUID(), usuario.id(), huella.calcular(codigo),
                    ahora, ahora.plus(Duration.ofMinutes(30)), false));
            correo.enviarHtml(email, "Recuperación de acceso a ALIADO",
                    PlantillaRecuperacionHtml.renderizar(codigo, 30));
        });
    }
}

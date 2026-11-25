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
    private final UsuarioRepository usuariosRepository;
    private final TokenRecuperacionRepository tokensRecuperacionRepository;
    private final GeneradorCodigoRecuperacion generadorCodigoRecuperacion;
    private final HuellaToken huellaToken;
    private final CorreoPort correoPort;
    private final Clock relojSistema;

    public SolicitarRecuperacionAccesoService(UsuarioRepository usuariosRepository,
            TokenRecuperacionRepository tokensRecuperacionRepository,
            GeneradorCodigoRecuperacion generadorCodigoRecuperacion, HuellaToken huellaToken,
            CorreoPort correoPort, Clock relojSistema) {
        this.usuariosRepository = usuariosRepository;
        this.tokensRecuperacionRepository = tokensRecuperacionRepository;
        this.generadorCodigoRecuperacion = generadorCodigoRecuperacion;
        this.huellaToken = huellaToken;
        this.correoPort = correoPort;
        this.relojSistema = relojSistema;
    }

    @Override
    public void ejecutar(SolicitarRecuperacionCommand command) {
        String correoElectronicoNormalizado = Usuario.normalizarEmail(command.email());
        usuariosRepository.porEmail(correoElectronicoNormalizado).ifPresent(usuario -> {
            String codigoRecuperacion = generadorCodigoRecuperacion.generar();
            OffsetDateTime fechaHoraSolicitud = OffsetDateTime.now(relojSistema);

            tokensRecuperacionRepository.guardar(new TokenRecuperacion(UUID.randomUUID(), usuario.id(),
                    huellaToken.calcular(codigoRecuperacion), fechaHoraSolicitud,
                    fechaHoraSolicitud.plus(Duration.ofMinutes(30)), false));

            correoPort.enviarHtml(correoElectronicoNormalizado, "Recuperación de acceso a ALIADO",
                    PlantillaRecuperacionHtml.renderizar(codigoRecuperacion, 30));
        });
    }
}

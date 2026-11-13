package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.LoginCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ResultadoLogin;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorTokenSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HasheadorPassword;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.IniciarSesionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CredencialesInvalidasException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CuentaBloqueadaException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.EstadoSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Sesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.Usuario;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.SesionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public class IniciarSesionService implements IniciarSesionUseCase {
    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final HasheadorPassword hasheadorPassword;
    private final GeneradorTokenSesion generadorTokenSesion;
    private final HuellaToken huellaToken;
    private final Clock clock;
    private final Duration duracionSesion;

    public IniciarSesionService(UsuarioRepository usuarioRepository, SesionRepository sesionRepository,
                                HasheadorPassword hasheadorPassword, GeneradorTokenSesion generadorTokenSesion,
                                HuellaToken huellaToken, Clock clock, Duration duracionSesion) {
        if (duracionSesion.isZero() || duracionSesion.isNegative()) {
            throw new IllegalArgumentException("La vigencia de sesión debe ser positiva.");
        }
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.hasheadorPassword = hasheadorPassword;
        this.generadorTokenSesion = generadorTokenSesion;
        this.huellaToken = huellaToken;
        this.clock = clock;
        this.duracionSesion = duracionSesion;
    }

    @Override
    public ResultadoLogin ejecutar(LoginCommand loginCommand) {
        var usuarioEncontrado = usuarioRepository.porEmailParaIniciarSesion(Usuario.normalizarEmail(loginCommand.email()));

        boolean passwordValida = hasheadorPassword.verificar(loginCommand.password(),
                usuarioEncontrado.map(Usuario::passwordHash).orElse(null));
        if (usuarioEncontrado.isEmpty() || !passwordValida) {
            throw new CredencialesInvalidasException();
        }
        Usuario usuario = usuarioEncontrado.get();

        if (usuario.bloqueado()) {
            throw new CuentaBloqueadaException();
        }

        OffsetDateTime fechaActual = OffsetDateTime.now(clock);

        for (Sesion sesionActiva : sesionRepository.sesionesActivasDeUsuario(usuario.id())) {
            sesionRepository.guardar(sesionActiva.cerrada(fechaActual));
        }

        String tokenSesion = generadorTokenSesion.generar();

        Sesion sesionNueva = new Sesion(UUID.randomUUID(), usuario.id(), huellaToken.calcular(tokenSesion),
                fechaActual, fechaActual.plus(duracionSesion), null, EstadoSesion.ACTIVA);

        sesionRepository.guardar(sesionNueva);
        return new ResultadoLogin(tokenSesion, usuario.rol().name());

    }
}

package com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.AuthService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.RegistrarUsuarioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.ResultadoLogin;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.RegistrarUsuarioUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.IniciarSesionUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.CerrarSesionUseCase;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.dto.AuthDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.mapper.AuthPresentationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {
    private final AuthService authService;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final IniciarSesionUseCase iniciarSesionUseCase;

    public AuthController(AuthService authService, CerrarSesionUseCase cerrarSesionUseCase,
                          RegistrarUsuarioUseCase registrarUsuarioUseCase,
                          IniciarSesionUseCase iniciarSesionUseCase,
                          ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        super(objectMapper, idempotencyService);
        this.authService = authService;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @GetMapping("/hola")
    public String hola(){
        return "hola";
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody AuthDtos.RegistroRequest req,
                                     @RequestHeader(value = "Idempotency-Key", required = false) String claveHeader) {
        return ejecutarIdempotente(claveHeader, "auth.registro", AuthDtos.RegistroResponse::id, 201, () -> {
            RegistrarUsuarioCommand command = AuthPresentationMapper.aCommand(req);
            return AuthPresentationMapper.aResponse(registrarUsuarioUseCase.ejecutar(command));
        });
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        ResultadoLogin resultado = iniciarSesionUseCase.ejecutar(AuthPresentationMapper.aCommand(req));
        return ResponseEntity.ok(AuthPresentationMapper.aResponse(resultado));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException.Unauthorized("SESION_INVALIDA", "No hay sesión activa.");
        }
        cerrarSesionUseCase.ejecutar(auth.substring("Bearer ".length()).trim());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperacion")
    public ResponseEntity<AuthDtos.MensajeResponse> recuperacion(
            @Valid @RequestBody AuthDtos.RecuperacionRequest req) {
        authService.solicitarRecuperacion(req.email());
        return ResponseEntity.accepted().body(new AuthDtos.MensajeResponse(
                "Si la cuenta existe, recibirás las instrucciones de recuperación."));
    }

    @PostMapping("/recuperacion/confirmar")
    public ResponseEntity<AuthDtos.MensajeResponse> confirmar(
            @Valid @RequestBody AuthDtos.ConfirmarRecuperacionRequest req) {
        authService.confirmarRecuperacion(req.token(), req.nuevaPassword());
        return ResponseEntity.ok(new AuthDtos.MensajeResponse("Contraseña actualizada."));
    }
}

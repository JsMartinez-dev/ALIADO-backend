package com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.AuthService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.RegistrarUsuarioCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.usecase.RegistrarUsuarioUseCase;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.SesionPort;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.dto.AuthDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.presentation.mapper.AuthPresentationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {
    private final AuthService authService;
    private final SesionPort sesionService;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public AuthController(AuthService authService, SesionPort sesionService,
                          RegistrarUsuarioUseCase registrarUsuarioUseCase,
                          ObjectMapper objectMapper, IdempotenciaPort idempotencyService) {
        super(objectMapper, idempotencyService);
        this.authService = authService;
        this.sesionService = sesionService;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
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
    public ResponseEntity<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        AuthService.LoginResultado resultado = authService.login(req.email(), req.password());
        return ResponseEntity.ok(new AuthDtos.LoginResponse(resultado.sessionToken(), resultado.rol()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // Plan SPEC-01: 401 sin sesión activa (no 204 siempre).
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException.Unauthorized("SESION_INVALIDA", "No hay sesión activa.");
        }
        boolean cerrada = sesionService.cerrarSesion(auth.substring("Bearer ".length()).trim());
        if (!cerrada) {
            throw new BusinessException.Unauthorized("SESION_INVALIDA", "La sesión no existe o ya fue cerrada.");
        }
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

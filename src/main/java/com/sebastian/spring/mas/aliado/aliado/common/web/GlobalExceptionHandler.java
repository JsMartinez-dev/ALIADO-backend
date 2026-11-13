package com.sebastian.spring.mas.aliado.aliado.common.web;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CorreoYaRegistradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CredencialesInvalidasException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CuentaBloqueadaException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.PasswordInvalidaException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.RolNoRegistrableException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.TerminosNoAceptadosException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(CredencialesInvalidasException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", ex.getMessage(), req);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ApiError> cuentaBloqueada(CuentaBloqueadaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.FORBIDDEN, "CUENTA_BLOQUEADA", ex.getMessage(), req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Datos inválidos.", req);
    }

    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<ApiError> correoDuplicado(CorreoYaRegistradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "CORREO_YA_REGISTRADO", ex.getMessage(), req);
    }

    @ExceptionHandler(TerminosNoAceptadosException.class)
    public ResponseEntity<ApiError> terminosNoAceptados(TerminosNoAceptadosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "TERMINOS_NO_ACEPTADOS", ex.getMessage(), req);
    }

    @ExceptionHandler(RolNoRegistrableException.class)
    public ResponseEntity<ApiError> rolNoRegistrable(RolNoRegistrableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "ROL_NO_REGISTRABLE", ex.getMessage(), req);
    }

    @ExceptionHandler(PasswordInvalidaException.class)
    public ResponseEntity<ApiError> passwordInvalida(PasswordInvalidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "PASSWORD_INVALIDA", ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> negocio(BusinessException ex, HttpServletRequest req) {
        HttpStatus estado = HttpStatus.resolve(ex.status());
        if (estado == null) {
            estado = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return respuesta(estado, ex.code(), ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Datos inválidos.");
        return respuesta(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", detalle, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> argumentoInvalido(IllegalArgumentException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "Datos inválidos.", req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> estadoInvalido(IllegalStateException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado.", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        // Carrera de unicidad (p. ej. registro duplicado concurrente): 409, no 500.
        return respuesta(HttpStatus.CONFLICT, "REGISTRO_DUPLICADO", "El registro ya existe.", req);
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> bloqueoOptimista(RuntimeException ex, HttpServletRequest req) {
        // Escritura concurrente sobre la misma versión: 409, no 500.
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_CONCURRENCIA",
                "El recurso fue modificado por otro usuario. Vuelve a intentarlo.", req);
    }

    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> multipart(RuntimeException ex, HttpServletRequest req) {
        // Parte faltante o multipart inválido (incluye tamaño excedido): 400, no 500.
        return respuesta(HttpStatus.BAD_REQUEST, "ARCHIVO_INVALIDO",
                "El archivo enviado es inválido o falta una parte requerida.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest req) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado.", req);
    }

    private ResponseEntity<ApiError> respuesta(HttpStatus status, String code, String message, HttpServletRequest req) {
        ApiError error = new ApiError(OffsetDateTime.now(), status.value(), code, message,
                req.getRequestURI(), UUID.randomUUID().toString());
        return ResponseEntity.status(status).body(error);
    }
}

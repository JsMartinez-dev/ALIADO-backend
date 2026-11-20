package com.sebastian.spring.mas.aliado.aliado.common.web;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.exception.OportunidadNoDisponibleException;

import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.ServicioPortafolioNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PortafolioInvalidoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.DatosPerfilProfesionalInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.ConflictoAgendaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.CompromisoNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.exception.ExperienciaProfesionalInvalidaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.CatalogoPerfilInvalidoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalYaExisteException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.ZonaUrgenciaInvalidasException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.FotoNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.DatosSolicitudInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoEncontradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.exception.SolicitudNoModificableException;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.exception.PerfilPublicoNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ConflictoVersionPerfilException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.DatosPerfilInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.DatosUbicacionInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.VisibilidadInvalidaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.PerfilNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.EliminacionPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.SolicitudExpiradaException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.SinSolicitudPendienteException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ServicioBloqueaEliminacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.exception.ConflictoEliminacionException;
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
    @ExceptionHandler(OportunidadNoDisponibleException.class)
    public ResponseEntity<ApiError> oportunidadNoDisponible(OportunidadNoDisponibleException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "OPORTUNIDAD_NO_ENCONTRADA", ex.getMessage(), req);
    }
    @ExceptionHandler(DatosPerfilProfesionalInvalidosException.class)
    public ResponseEntity<ApiError> datosPerfilProfesionalInvalidos(DatosPerfilProfesionalInvalidosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }
    @ExceptionHandler(ConflictoAgendaException.class)
    public ResponseEntity<ApiError> conflictoAgenda(ConflictoAgendaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_AGENDA", ex.getMessage(), req);
    }
    @ExceptionHandler(CompromisoNoEncontradoException.class)
    public ResponseEntity<ApiError> compromisoNoEncontrado(CompromisoNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "COMPROMISO_NO_ENCONTRADO", ex.getMessage(), req);
    }

    @ExceptionHandler(PortafolioInvalidoException.class)
    public ResponseEntity<ApiError> portafolioInvalido(PortafolioInvalidoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "PORTAFOLIO_INVALIDO", ex.getMessage(), req);
    }
    @ExceptionHandler(ServicioPortafolioNoEncontradoException.class)
    public ResponseEntity<ApiError> servicioPortafolioNoEncontrado(ServicioPortafolioNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "PORTAFOLIO_NO_ENCONTRADO", ex.getMessage(), req);
    }

    @ExceptionHandler(PerfilProfesionalNoEncontradoException.class)
    public ResponseEntity<ApiError> perfilProfesionalNoEncontrado(PerfilProfesionalNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "PERFIL_NO_ENCONTRADO", ex.getMessage(), req);
    }

    @ExceptionHandler(ExperienciaProfesionalInvalidaException.class)
    public ResponseEntity<ApiError> experienciaProfesionalInvalida(ExperienciaProfesionalInvalidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "EXPERIENCIA_INVALIDA", ex.getMessage(), req);
    }

    @ExceptionHandler(CatalogoPerfilInvalidoException.class)
    public ResponseEntity<ApiError> catalogoPerfilInvalido(CatalogoPerfilInvalidoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(PerfilProfesionalYaExisteException.class)
    public ResponseEntity<ApiError> perfilProfesionalYaExiste(PerfilProfesionalYaExisteException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "PERFIL_YA_EXISTE", ex.getMessage(), req);
    }

    @ExceptionHandler(FotoNoEncontradaException.class)
    public ResponseEntity<ApiError> fotoNoEncontrada(FotoNoEncontradaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "FOTO_NO_ENCONTRADA", ex.getMessage(), req);
    }

    @ExceptionHandler(DatosSolicitudInvalidosException.class)
    public ResponseEntity<ApiError> datosSolicitudInvalidos(DatosSolicitudInvalidosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<ApiError> solicitudNoEncontrada(SolicitudNoEncontradaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "SOLICITUD_NO_ENCONTRADA", ex.getMessage(), req);
    }

    @ExceptionHandler(SolicitudNoModificableException.class)
    public ResponseEntity<ApiError> solicitudNoModificable(SolicitudNoModificableException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(ZonaUrgenciaInvalidasException.class)
    public ResponseEntity<ApiError> zonaUrgenciaInvalidas(ZonaUrgenciaInvalidasException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(PerfilPublicoNoDisponibleException.class)
    public ResponseEntity<ApiError> perfilPublicoPrestadorNoDisponible(PerfilPublicoNoDisponibleException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "PERFIL_NO_DISPONIBLE", ex.getMessage(), req);
    }

    @ExceptionHandler(EliminacionPendienteException.class)
    public ResponseEntity<ApiError> eliminacionPendiente(EliminacionPendienteException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "ELIMINACION_PENDIENTE", ex.getMessage(), req);
    }

    @ExceptionHandler(SolicitudExpiradaException.class)
    public ResponseEntity<ApiError> solicitudExpirada(SolicitudExpiradaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.GONE, "SOLICITUD_EXPIRADA", ex.getMessage(), req);
    }

    @ExceptionHandler(SinSolicitudPendienteException.class)
    public ResponseEntity<ApiError> sinSolicitudPendiente(SinSolicitudPendienteException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "SIN_SOLICITUD_PENDIENTE", ex.getMessage(), req);
    }

    @ExceptionHandler(ServicioBloqueaEliminacionException.class)
    public ResponseEntity<ApiError> servicioBloqueaEliminacion(ServicioBloqueaEliminacionException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "SERVICIO_EN_EJECUCION", ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictoEliminacionException.class)
    public ResponseEntity<ApiError> conflictoEliminacion(ConflictoEliminacionException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_CONCURRENCIA", ex.getMessage(), req);
    }

    @ExceptionHandler(PerfilNoDisponibleException.class)
    public ResponseEntity<ApiError> perfilNoDisponible(PerfilNoDisponibleException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "PERFIL_NO_DISPONIBLE", ex.getMessage(), req);
    }

    @ExceptionHandler(VisibilidadInvalidaException.class)
    public ResponseEntity<ApiError> visibilidadInvalida(VisibilidadInvalidaException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, "VISIBILIDAD_INVALIDA", ex.getMessage(), req);
    }

    @ExceptionHandler(DatosUbicacionInvalidosException.class)
    public ResponseEntity<ApiError> datosUbicacionInvalidos(DatosUbicacionInvalidosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictoVersionPerfilException.class)
    public ResponseEntity<ApiError> conflictoVersionPerfil(ConflictoVersionPerfilException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO_VERSION", ex.getMessage(), req);
    }

    @ExceptionHandler(DatosPerfilInvalidosException.class)
    public ResponseEntity<ApiError> datosPerfilInvalidos(DatosPerfilInvalidosException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.codigo(), ex.getMessage(), req);
    }

    @ExceptionHandler(PerfilNoEncontradoException.class)
    public ResponseEntity<ApiError> perfilNoEncontrado(PerfilNoEncontradoException ex, HttpServletRequest req) {
        return respuesta(HttpStatus.NOT_FOUND, "PERFIL_NO_ENCONTRADO", ex.getMessage(), req);
    }

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

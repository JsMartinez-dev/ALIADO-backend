package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.web.ApiError;
import com.sebastian.spring.mas.aliado.aliado.common.web.CurrentUser;
import com.sebastian.spring.mas.aliado.aliado.common.web.GlobalExceptionHandler;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.BandejaAvisosResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ConsultarBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.ResumenNoLeidasResult;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.MarcarAvisoLeidoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarBandejaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesOportunidadesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarNotificacionesUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ConsultarResumenNoLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarAvisoLeidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RegistrarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.ResolverOportunidadAvisoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RetirarDispositivoPushUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.usecase.RevocarVinculoPushDiferidoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.controller.NotificacionController;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F09-BANDEJA-04: contrato HTTP de la bandeja. Comprueba que las cuatro rutas pasan por su
 * caso de uso con el destinatario de la sesión, el cuerpo de las consultas, los 204 sin
 * cuerpo, el 401 sin sesión y el 404 común de un aviso ajeno o inexistente.
 */
class NotificacionLecturaControllerTest {

    private static final UUID CUENTA = UUID.randomUUID();
    private static final UUID AVISO = UUID.randomUUID();
    private static final OffsetDateTime GENERADO =
            OffsetDateTime.of(2026, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);

    private final ConsultarBandejaUseCase bandeja = mock(ConsultarBandejaUseCase.class);
    private final ConsultarResumenNoLeidasUseCase resumen = mock(ConsultarResumenNoLeidasUseCase.class);
    private final MarcarAvisoLeidoUseCase marcarAviso = mock(MarcarAvisoLeidoUseCase.class);
    private final MarcarTodasLeidasUseCase marcarTodas = mock(MarcarTodasLeidasUseCase.class);
    private final GlobalExceptionHandler errores = new GlobalExceptionHandler();
    private final NotificacionController controlador = new NotificacionController(
            mock(ConsultarNotificacionesUseCase.class),
            mock(ConsultarNotificacionesOportunidadesUseCase.class),
            bandeja, resumen, marcarAviso, marcarTodas,
            mock(RegistrarDispositivoPushUseCase.class),
            mock(RetirarDispositivoPushUseCase.class),
            mock(RevocarVinculoPushDiferidoUseCase.class),
            mock(ResolverOportunidadAvisoUseCase.class));

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void laBandejaUsaLaSesionYDevuelveLeidaEnNuloOEnIso8601() {
        autenticar();
        AvisoBandejaResult pendiente = new AvisoBandejaResult(AVISO, "NUEVA_OPORTUNIDAD", null,
                UUID.randomUUID(), "detalle", "ENTREGADA", GENERADO, null);
        AvisoBandejaResult leido = new AvisoBandejaResult(UUID.randomUUID(), "NUEVA_OPORTUNIDAD", null,
                UUID.randomUUID(), "detalle", "PENDIENTE", GENERADO.minusHours(1), GENERADO);
        when(bandeja.ejecutar(any())).thenReturn(new BandejaAvisosResult(
                List.of(pendiente, leido), "cursor-siguiente"));

        ResponseEntity<com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos.BandejaResponse> respuesta =
                controlador.bandeja("LEIDA", "NUEVA_OPORTUNIDAD", 50, "cursor-anterior");

        ArgumentCaptor<ConsultarBandejaCommand> comando = ArgumentCaptor.forClass(ConsultarBandejaCommand.class);
        verify(bandeja).ejecutar(comando.capture());
        assertEquals(CUENTA, comando.getValue().destinatarioId());
        assertEquals("LEIDA", comando.getValue().estadoLectura());
        assertEquals("NUEVA_OPORTUNIDAD", comando.getValue().tipo());
        assertEquals(50, comando.getValue().limite());
        assertEquals("cursor-anterior", comando.getValue().cursor());

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("cursor-siguiente", respuesta.getBody().cursorSiguiente());
        assertNull(respuesta.getBody().items().get(0).leidaEn());
        assertEquals(GENERADO, respuesta.getBody().items().get(1).leidaEn());
        // La lectura y la entrega son campos distintos: leída con la entrega aún pendiente.
        assertEquals("PENDIENTE", respuesta.getBody().items().get(1).estadoEntrega());
    }

    @Test
    void elResumenConsultaLaCuentaDeLaSesion() {
        autenticar();
        when(resumen.ejecutar(CUENTA)).thenReturn(new ResumenNoLeidasResult(true));

        var respuesta = controlador.resumen();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(true, respuesta.getBody().hayNoLeidas());
        verify(resumen).ejecutar(CUENTA);
    }

    @Test
    void lasEscriturasResponden204SinCuerpoConElDestinatarioDeLaSesion() {
        autenticar();

        var individual = controlador.marcarLeida(AVISO);
        var masiva = controlador.marcarTodasLeidas();

        assertEquals(HttpStatus.NO_CONTENT, individual.getStatusCode());
        assertNull(individual.getBody());
        assertEquals(HttpStatus.NO_CONTENT, masiva.getStatusCode());
        assertNull(masiva.getBody());
        verify(marcarAviso).ejecutar(new MarcarAvisoLeidoCommand(AVISO, CUENTA));
        verify(marcarTodas).ejecutar(new MarcarTodasLeidasCommand(CUENTA));
    }

    @Test
    void sinSesionLasCuatroRutasResponden401() {
        SecurityContextHolder.clearContext();
        Runnable[] llamadas = {
                () -> controlador.bandeja(null, null, null, null),
                controlador::resumen,
                () -> controlador.marcarLeida(AVISO),
                controlador::marcarTodasLeidas
        };

        for (Runnable llamada : llamadas) {
            BusinessException ex = assertThrows(BusinessException.class, llamada::run);
            assertEquals(401, ex.status());
            ResponseEntity<ApiError> respuesta = errores.negocio(ex, peticion("/api/v1/notificaciones"));
            assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
            assertEquals("SIN_SESION", respuesta.getBody().code());
        }
    }

    @Test
    void avisoAjenoEInexistenteProducenLaMismaRespuesta404() {
        autenticar();
        // Cero filas afectadas no distingue un aviso ajeno de uno inexistente.
        NotificacionRepository repositorio = mock(NotificacionRepository.class);
        when(repositorio.marcarLeida(any(), any(), any())).thenReturn(false);
        MarcarAvisoLeidoService servicio = new MarcarAvisoLeidoService(repositorio, Clock.systemUTC());

        BusinessException ajeno = assertThrows(BusinessException.class,
                () -> servicio.ejecutar(new MarcarAvisoLeidoCommand(AVISO, CUENTA)));
        BusinessException inexistente = assertThrows(BusinessException.class,
                () -> servicio.ejecutar(new MarcarAvisoLeidoCommand(UUID.randomUUID(), CUENTA)));

        ResponseEntity<ApiError> respuestaAjena = errores.negocio(ajeno,
                peticion("/api/v1/notificaciones/" + AVISO + "/lectura"));
        ResponseEntity<ApiError> respuestaInexistente = errores.negocio(inexistente,
                peticion("/api/v1/notificaciones/" + UUID.randomUUID() + "/lectura"));

        assertEquals(HttpStatus.NOT_FOUND, respuestaAjena.getStatusCode());
        assertEquals(respuestaAjena.getStatusCode(), respuestaInexistente.getStatusCode());
        assertEquals(respuestaAjena.getBody().code(), respuestaInexistente.getBody().code());
        assertEquals(respuestaAjena.getBody().message(), respuestaInexistente.getBody().message());
        assertEquals("NOTIFICACION_NO_ENCONTRADA", respuestaAjena.getBody().code());
    }

    @Test
    void unaEntradaInvalidaDelCasoDeUsoSeTraduceEn400() {
        autenticar();
        when(bandeja.ejecutar(any())).thenThrow(
                new BusinessException.BadRequest("CURSOR_INVALIDO", "Cursor inválido."));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controlador.bandeja(null, null, null, "no-es-un-cursor"));

        ResponseEntity<ApiError> respuesta = errores.negocio(ex, peticion("/api/v1/notificaciones/bandeja"));
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("CURSOR_INVALIDO", respuesta.getBody().code());
    }

    private static void autenticar() {
        CurrentUser usuario = new CurrentUser(CUENTA, "cuenta@aliado.test", "PRESTADOR");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private static HttpServletRequest peticion(String ruta) {
        HttpServletRequest peticion = mock(HttpServletRequest.class);
        when(peticion.getRequestURI()).thenReturn(ruta);
        return peticion;
    }
}

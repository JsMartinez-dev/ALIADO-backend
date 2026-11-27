package com.sebastian.spring.mas.aliado.aliado.modules.corrections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.idempotencia.IdempotenciaPort;
import com.sebastian.spring.mas.aliado.aliado.common.web.BaseController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.GeneradorTokenSesion;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HasheadorPassword;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.HuellaToken;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.IniciarSesionService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service.SesionService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.exception.CuentaBloqueadaException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.SesionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.domain.repository.UsuarioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.dto.LoginCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.ConsultarCalificacionPropiaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.ConsultarCalificacionPropiaService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.ConsultarCalificacionesPropiasService;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Calificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.Resena;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.EstadoVisibilidadResena;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.CalificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.ResenaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.PerfilProfesionalNoEncontradoException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.ConsultarPerfilPropioService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.ConsultarReportesPropiosService;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.OperacionesReportes;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.service.ConsultarServiciosPropiosService;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.EstadoServicio;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.model.ServicioContratado;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.domain.repository.ServicioRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.FotoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackendMvpCorrectionsTest {
    @Mock PerfilProfesionalRepository perfiles;
    @Mock ServicioRepository serviciosRepo;
    @Mock CalificacionRepository calificaciones;
    @Mock ResenaRepository resenas;
    @Mock ServicioLecturaPort serviciosLectura;
    @Mock OperacionesReportes operaciones;
    @Mock IdempotenciaPort idempotencia;

    private static OffsetDateTime ahora() { return OffsetDateTime.now(ZoneOffset.UTC); }

    @Test
    void replayIdempotenteDevuelveJsonPlanoYNoElPojoDeJsonNode() throws Exception {
        var clave = UUID.randomUUID();
        when(idempotencia.buscar(clave, "test.replay"))
                .thenReturn(Optional.of(new IdempotenciaPort.Respuesta(201,
                        new ObjectMapper().readTree("{\"id\":\"abc\"}"))));
        var controller = new ReplayController(new ObjectMapper(), idempotencia);
        var respuesta = controller.replay(clave);
        assertEquals(201, respuesta.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_JSON, respuesta.getHeaders().getContentType());
        assertEquals("{\"id\":\"abc\"}", respuesta.getBody());
    }

    @Test
    void solicitudExponeFotosComoClavesYComoDetalle() {
        UUID solicitudId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();
        var solicitud = new Solicitud(solicitudId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Fuga", Urgencia.ALTA, EstadoSolicitud.PUBLICADA, ahora(), ahora(), 0,
                List.of(new FotoSolicitud(fotoId, solicitudId, "fotos/fuga.jpg", 1)), List.of());
        var resultado = SolicitudResult.desde(solicitud);
        assertEquals(List.of("fotos/fuga.jpg"), resultado.fotos());
        assertEquals(fotoId, resultado.fotosDetalle().getFirst().id());
        assertEquals(1, resultado.fotosDetalle().getFirst().orden());
    }

    @Test
    void perfilPropioSeDevuelveCuandoExiste() {
        UUID prestador = UUID.randomUUID();
        var perfil = new PerfilProfesional(UUID.randomUUID(), prestador, Map.of(), EstadoExposicion.OCULTO,
                ahora(), ahora(), Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of());
        when(perfiles.porPrestadorId(prestador)).thenReturn(Optional.of(perfil));
        var resultado = new ConsultarPerfilPropioService(perfiles).ejecutar(prestador);
        assertEquals(perfil.id(), resultado.id());
        assertEquals(prestador, resultado.prestadorId());
    }

    @Test
    void perfilPropioInexistenteEs404() {
        UUID prestador = UUID.randomUUID();
        when(perfiles.porPrestadorId(prestador)).thenReturn(Optional.empty());
        assertThrows(PerfilProfesionalNoEncontradoException.class,
                () -> new ConsultarPerfilPropioService(perfiles).ejecutar(prestador));
    }

    @Test
    void serviciosPropiosIncluyenMetadatosDePagina() {
        UUID usuario = UUID.randomUUID();
        var s1 = servicio(usuario, usuario, EstadoServicio.CONTRATADO);
        var s2 = servicio(usuario, UUID.randomUUID(), EstadoServicio.FINALIZADO);
        when(serviciosRepo.porUsuario(usuario, 0, 1)).thenReturn(List.of(s1));
        when(serviciosRepo.contarPorUsuario(usuario)).thenReturn(2L);
        var resultado = new ConsultarServiciosPropiosService(serviciosRepo).ejecutar(usuario, 0, 1);
        assertEquals(1, resultado.items().size());
        assertEquals(2, resultado.totalElementos());
        assertEquals(2, resultado.totalPaginas());
        assertEquals(s1.id(), resultado.items().getFirst().id());
    }

    @Test
    void calificacionPropiaIncluyeEstadoDeResena() {
        UUID cliente = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        var servicio = servicio(cliente, UUID.randomUUID(), EstadoServicio.FINALIZADO);
        var calificacion = new Calificacion(UUID.randomUUID(), servicioId, cliente, UUID.randomUUID(), 5, ahora());
        var resena = new Resena(UUID.randomUUID(), calificacion.id(), "Buen trabajo", ahora(),
                EstadoVisibilidadResena.VISIBLE);
        when(serviciosLectura.porId(servicioId)).thenReturn(Optional.of(servicio));
        when(calificaciones.porServicioId(servicioId)).thenReturn(Optional.of(calificacion));
        when(resenas.porCalificacionId(calificacion.id())).thenReturn(Optional.of(resena));
        var resultado = new ConsultarCalificacionPropiaService(calificaciones, resenas, serviciosLectura)
                .ejecutar(new ConsultarCalificacionPropiaCommand(servicioId, cliente));
        assertEquals(calificacion.id(), resultado.calificacionId());
        assertTrue(resultado.tieneResena());
    }

    @Test
    void calificacionPropiaDeOtroClienteEs403() {
        UUID servicioId = UUID.randomUUID();
        var servicio = servicio(UUID.randomUUID(), UUID.randomUUID(), EstadoServicio.FINALIZADO);
        when(serviciosLectura.porId(servicioId)).thenReturn(Optional.of(servicio));
        var ex = assertThrows(BusinessException.class,
                () -> new ConsultarCalificacionPropiaService(calificaciones, resenas, serviciosLectura)
                        .ejecutar(new ConsultarCalificacionPropiaCommand(servicioId, UUID.randomUUID())));
        assertEquals(403, ex.status());
        assertEquals("NO_AUTORIZADO", ex.code());
    }

    @Test
    void historialDeCalificacionesSePagina() {
        UUID cliente = UUID.randomUUID();
        var lista = List.of(calificacion(cliente), calificacion(cliente), calificacion(cliente));
        when(calificaciones.porClienteId(cliente)).thenReturn(lista);
        when(resenas.porCalificacionId(any())).thenReturn(Optional.empty());
        var resultado = new ConsultarCalificacionesPropiasService(calificaciones, resenas)
                .ejecutar(cliente, 1, 2);
        assertEquals(1, resultado.items().size());
        assertEquals(3, resultado.totalElementos());
        assertEquals(2, resultado.totalPaginas());
        assertEquals(1, resultado.pagina());
    }

    @Test
    void reportesPropiosSePaginaConEvidencias() {
        UUID reportante = UUID.randomUUID();
        var reporte = new Reporte(UUID.randomUUID(), TipoReporte.SERVICIO, reportante, null, null,
                UUID.randomUUID(), "motivo", EstadoReporte.PENDIENTE, null, null, null, ahora(), null, 0);
        when(operaciones.reportesDe(reportante)).thenReturn(List.of(reporte));
        when(operaciones.evidenciasDe(reporte.id())).thenReturn(List.of());
        var resultado = new ConsultarReportesPropiosService(operaciones).ejecutar(reportante, 0, 20);
        assertEquals(1, resultado.items().size());
        assertEquals(reporte.id(), resultado.items().getFirst().id());
        assertEquals("SERVICIO", resultado.items().getFirst().tipo());
        assertEquals(1, resultado.totalPaginas());
    }

    @Test
    void sesionBloqueadaLanza403() {
        UUID usuarioId = UUID.randomUUID();
        String token = "token";
        var sesion = new Sesion(UUID.randomUUID(), usuarioId, SesionService.hashToken(token), ahora(),
                ahora().plusHours(1), null, EstadoSesion.ACTIVA);
        var usuario = new Usuario(usuarioId, "u@test.com", "hash", Rol.CLIENTE,
                EstadoCuenta.BLOQUEADA_PREVENTIVAMENTE, ahora());
        when(sesionesRepo.porTokenHash(SesionService.hashToken(token))).thenReturn(Optional.of(sesion));
        when(usuarios.porId(usuarioId)).thenReturn(Optional.of(usuario));
        assertThrows(CuentaBloqueadaException.class, () -> new SesionService(sesionesRepo, usuarios).validar(token));
    }

    @Test
    void sesionValidaDevuelveUsuario() {
        UUID usuarioId = UUID.randomUUID();
        String token = "token";
        var sesion = new Sesion(UUID.randomUUID(), usuarioId, SesionService.hashToken(token), ahora(),
                ahora().plusHours(1), null, EstadoSesion.ACTIVA);
        var usuario = new Usuario(usuarioId, "u@test.com", "hash", Rol.CLIENTE, EstadoCuenta.ACTIVA, ahora());
        when(sesionesRepo.porTokenHash(SesionService.hashToken(token))).thenReturn(Optional.of(sesion));
        when(usuarios.porId(usuarioId)).thenReturn(Optional.of(usuario));
        assertTrue(new SesionService(sesionesRepo, usuarios).validar(token).isPresent());
    }

    @Test
    void administradorMovilSeRechazaAntesDeTocarSesiones() {
        var admin = new Usuario(UUID.randomUUID(), "admin@test.com", "hash", Rol.ADMINISTRADOR,
                EstadoCuenta.ACTIVA, ahora());
        when(usuarios.porEmailParaIniciarSesion("admin@test.com")).thenReturn(Optional.of(admin));
        when(hasheador.verificar("Clave1234", "hash")).thenReturn(true);
        var service = new IniciarSesionService(usuarios, sesionesRepo, hasheador, generador, huella,
                Clock.fixed(ahora().toInstant(), ZoneOffset.UTC), Duration.ofHours(1));
        var ex = assertThrows(BusinessException.class,
                () -> service.ejecutar(new LoginCommand("admin@test.com", "Clave1234", true)));
        assertEquals(403, ex.status());
        assertEquals("ADMINISTRADOR_NO_DISPONIBLE_EN_MOVIL", ex.code());
        verifyNoInteractions(sesionesRepo, generador, huella);
    }

    @Test
    void clienteMovilConservaLogin() {
        var cliente = new Usuario(UUID.randomUUID(), "cliente@test.com", "hash", Rol.CLIENTE,
                EstadoCuenta.ACTIVA, ahora());
        when(usuarios.porEmailParaIniciarSesion("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(hasheador.verificar("Clave1234", "hash")).thenReturn(true);
        when(sesionesRepo.sesionesActivasDeUsuario(cliente.id())).thenReturn(List.of());
        when(generador.generar()).thenReturn("nuevo-token");
        when(huella.calcular("nuevo-token")).thenReturn("nuevo-hash");
        var service = new IniciarSesionService(usuarios, sesionesRepo, hasheador, generador, huella,
                Clock.fixed(ahora().toInstant(), ZoneOffset.UTC), Duration.ofHours(1));
        var resultado = service.ejecutar(new LoginCommand("cliente@test.com", "Clave1234", true));
        assertEquals("nuevo-token", resultado.sessionToken());
        assertEquals("CLIENTE", resultado.rol());
        verify(sesionesRepo).guardar(any(Sesion.class));
    }

    @Mock UsuarioRepository usuarios;
    @Mock SesionRepository sesionesRepo;
    @Mock HasheadorPassword hasheador;
    @Mock GeneradorTokenSesion generador;
    @Mock HuellaToken huella;

    private ServicioContratado servicio(UUID cliente, UUID prestador, EstadoServicio estado) {
        return new ServicioContratado(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), cliente, prestador,
                estado, true, ahora(), 0, List.of(), List.of());
    }

    private static final class ReplayController extends BaseController {
        ReplayController(ObjectMapper mapper, IdempotenciaPort port) { super(mapper, port); }
        org.springframework.http.ResponseEntity<String> replay(UUID clave) {
            return respuestaGuardada(clave, "test.replay").orElseThrow();
        }
    }

    private Calificacion calificacion(UUID cliente) {
        return new Calificacion(UUID.randomUUID(), UUID.randomUUID(), cliente, UUID.randomUUID(), 5, ahora());
    }
}

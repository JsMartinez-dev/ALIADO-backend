package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.events.Eventos;
import com.sebastian.spring.mas.aliado.aliado.common.events.PublicadorEventos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarAvisoOportunidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.TrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.RegistroOportunidadNotificacionAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.CompatibilidadPort;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service.DetectarNuevaOportunidadService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.ConsultaPreferenciasService;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.PreferenciaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.model.TipoAviso;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.domain.repository.PreferenciaRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.CrearSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.PublicarSolicitudCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.SolicitudResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.dto.ZonaUrgenciaResult;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.CreacionSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.CrearSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.GuardarBorradorService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.service.PublicarSolicitudService;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.CrearSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.DefinirZonaUrgenciaUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.GuardarBorradorUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.usecase.PublicarSolicitudUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Urgencia;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.repository.SolicitudRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-05: ambas rutas de publicación generan un aviso y su trabajo push; el
 * borrador no genera ninguno; el reproceso no duplica y la preferencia apagada
 * conserva el aviso de bandeja.
 */
class EncoladoOportunidadTest {
    private final NotificacionRepositorioFalso notificaciones = new NotificacionRepositorioFalso();
    private final TrabajoRepositorioFalso trabajos = new TrabajoRepositorioFalso();
    private final PreferenciaRepositorioEnMemoria preferencias = new PreferenciaRepositorioEnMemoria();
    private final CompatibilidadFalsa compatibilidad = new CompatibilidadFalsa();
    private final SolicitudRepositorioFalso solicitudes = new SolicitudRepositorioFalso();

    private final UUID cliente = UUID.randomUUID();
    private final UUID categoria = UUID.randomUUID();
    private final UUID zona = UUID.randomUUID();
    private final UUID prestador = UUID.randomUUID();

    private PublicadorEventos eventos;
    private CrearSolicitudUseCase crear;
    private GuardarBorradorUseCase guardarBorrador;
    private PublicarSolicitudUseCase publicar;

    @BeforeEach
    void preparar() {
        var aviso = new RegistrarAvisoOportunidadService(notificaciones, trabajos,
                new ConsultaPreferenciasService(preferencias), Duration.ofMinutes(60));
        var registro = new RegistroOportunidadNotificacionAdapter(aviso);
        var detectar = new DetectarNuevaOportunidadService(compatibilidad, registro);
        eventos = evento -> {
            if (evento instanceof Eventos.SolicitudPublicada publicado) {
                detectar.ejecutar(publicado.solicitudId());
            }
        };
        DefinirZonaUrgenciaUseCase zonaUrgencia = command -> new ZonaUrgenciaResult(command.zonaId(), Urgencia.MEDIA);
        CreacionSolicitud creacion = new CreacionSolicitud(solicitudes, id -> true, zonaUrgencia, eventos);
        guardarBorrador = new GuardarBorradorService(creacion);
        crear = new CrearSolicitudService(creacion, guardarBorrador);
        publicar = new PublicarSolicitudService(solicitudes, id -> true, zonaUrgencia, eventos);
        compatibilidad.prestadores = List.of(prestador);
    }

    @Test
    void publicacionDirectaGeneraUnAvisoYTrabajoPorDestinatarioElegible() {
        habilitarPush(prestador, true);

        crear.ejecutar(comando("PUBLICADA"));

        List<Notificacion> avisos = notificaciones.porDestinatario(prestador);
        assertEquals(1, avisos.size());
        assertEquals(Canal.PUSH, avisos.get(0).canal());
        assertEquals(1, trabajos.trabajos.size());
        assertTrue(trabajos.trabajos.containsKey(avisos.get(0).id()));
    }

    @Test
    void guardarBorradorNoGeneraAvisoNiTrabajo() {
        crear.ejecutar(comando("BORRADOR"));

        assertEquals(0, notificaciones.porDestinatario(prestador).size());
        assertEquals(0, trabajos.trabajos.size());
        assertTrue(preferencias.consultas.isEmpty());
    }

    @Test
    void publicarUnBorradorGeneraElMismoResultadoQuePublicarDirecto() {
        habilitarPush(prestador, true);
        SolicitudResult borrador = crear.ejecutar(comando("BORRADOR"));

        publicar.ejecutar(new PublicarSolicitudCommand(borrador.id(), cliente));

        List<Notificacion> avisos = notificaciones.porDestinatario(prestador);
        assertEquals(1, avisos.size());
        assertEquals(1, trabajos.trabajos.size());
        assertTrue(trabajos.trabajos.containsKey(avisos.get(0).id()));
        assertEquals(EstadoSolicitud.PUBLICADA.name(), solicitudes.porId(borrador.id()).orElseThrow().estado().name());
    }

    @Test
    void reprocesarElEventoNoDuplicaAvisoNiTrabajo() {
        habilitarPush(prestador, true);
        SolicitudResult publicada = crear.ejecutar(comando("PUBLICADA"));

        eventos.publicar(new Eventos.SolicitudPublicada(publicada.id(), cliente, categoria, zona, "MEDIA",
                OffsetDateTime.now()));

        assertEquals(1, notificaciones.porDestinatario(prestador).size());
        assertEquals(1, trabajos.trabajos.size());
    }

    @Test
    void cadaDestinatarioCompatibleRecibeSuPropioAvisoYTrabajo() {
        UUID segundo = UUID.randomUUID();
        UUID tercero = UUID.randomUUID();
        compatibilidad.prestadores = List.of(prestador, segundo, tercero);
        habilitarPush(prestador, true);
        habilitarPush(segundo, true);
        habilitarPush(tercero, true);

        crear.ejecutar(comando("PUBLICADA"));

        assertEquals(3, notificaciones.total());
        assertEquals(3, trabajos.trabajos.size());
    }

    @Test
    void sinEleccionPersistidaElAvisoSeConservaPeroNoSeEncola() {
        // Sin fila en preferencias: NUEVA_OPORTUNIDAD/PUSH vale false (F09-PUSH-01).
        crear.ejecutar(comando("PUBLICADA"));

        assertEquals(1, notificaciones.porDestinatario(prestador).size(), "la bandeja conserva el aviso");
        assertEquals(0, trabajos.trabajos.size(), "sin elección persistida no se encola push");
        assertEquals(List.of("NUEVA_OPORTUNIDAD/PUSH"), preferencias.consultas);
    }

    @Test
    void preferenciaApagadaConservaElAvisoEnLaBandejaSinEncolar() {
        habilitarPush(prestador, false);

        SolicitudResult publicada = crear.ejecutar(comando("PUBLICADA"));

        assertEquals(1, notificaciones.porDestinatario(prestador).size());
        assertEquals(0, trabajos.trabajos.size());

        // Volver a habilitar no convierte en push un aviso histórico ya procesado.
        habilitarPush(prestador, true);
        eventos.publicar(new Eventos.SolicitudPublicada(publicada.id(), cliente, categoria, zona, "MEDIA",
                OffsetDateTime.now()));

        assertEquals(1, notificaciones.porDestinatario(prestador).size());
        assertEquals(0, trabajos.trabajos.size());

        // Una publicación nueva sí encola.
        crear.ejecutar(comando("PUBLICADA"));
        assertEquals(2, notificaciones.porDestinatario(prestador).size());
        assertEquals(1, trabajos.trabajos.size());
    }

    private void habilitarPush(UUID prestadorId, boolean habilitada) {
        preferencias.guardar(new PreferenciaNotificacion(prestadorId, TipoAviso.NUEVA_OPORTUNIDAD, "PUSH",
                habilitada, true));
    }

    private CrearSolicitudCommand comando(String estado) {
        return new CrearSolicitudCommand(cliente, categoria, "Descripción de la solicitud", zona, "MEDIA", estado);
    }

    private static final class NotificacionRepositorioFalso implements NotificacionRepository {
        private final Map<String, Notificacion> guardadas = new LinkedHashMap<>();

        @Override public boolean insertarSiNoExiste(Notificacion notificacion) {
            return guardadas.putIfAbsent(clave(notificacion), notificacion) == null;
        }

        @Override public Notificacion guardar(Notificacion notificacion) {
            guardadas.put(clave(notificacion), notificacion);
            return notificacion;
        }

        @Override public boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId, int eventVersion) {
            return guardadas.values().stream().anyMatch(n -> n.tipo().name().equals(tipo)
                    && n.aggregateId().equals(aggregateId) && n.destinatarioId().equals(destinatarioId)
                    && n.eventVersion() == eventVersion);
        }

        @Override public List<Notificacion> porDestinatario(UUID destinatarioId) {
            return guardadas.values().stream().filter(n -> n.destinatarioId().equals(destinatarioId)).toList();
        }

        @Override public List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo) {
            return porDestinatario(destinatarioId).stream().filter(n -> n.tipo().name().equals(tipo)).toList();
        }

        @Override public List<Notificacion> pendientesParaEntrega() { return List.of(); }

        int total() { return guardadas.size(); }

        private static String clave(Notificacion n) {
            return n.tipo().name() + "|" + n.aggregateId() + "|" + n.destinatarioId() + "|" + n.eventVersion();
        }
    }

    private static final class TrabajoRepositorioFalso implements TrabajoPushRepository {
        private final Map<UUID, Duration> trabajos = new LinkedHashMap<>();

        @Override public boolean encolarSiNoExiste(UUID notificacionId, Duration vigencia) {
            return trabajos.putIfAbsent(notificacionId, vigencia) == null;
        }
    }

    private static final class PreferenciaRepositorioEnMemoria implements PreferenciaRepository {
        private final Map<String, PreferenciaNotificacion> guardadas = new LinkedHashMap<>();
        private final List<String> consultas = new ArrayList<>();

        @Override public List<PreferenciaNotificacion> porUsuario(UUID usuarioId) {
            return guardadas.values().stream().filter(p -> p.usuarioId().equals(usuarioId)).toList();
        }

        @Override public Optional<PreferenciaNotificacion> porUsuarioTipoCanal(UUID usuarioId, String tipoAviso,
                                                                               String canal) {
            consultas.add(tipoAviso + "/" + canal);
            return Optional.ofNullable(guardadas.get(clave(usuarioId, tipoAviso, canal)));
        }

        @Override public PreferenciaNotificacion guardar(PreferenciaNotificacion preferencia) {
            guardadas.put(clave(preferencia.usuarioId(), preferencia.tipoAviso().name(), preferencia.canal()),
                    preferencia);
            return preferencia;
        }

        private static String clave(UUID usuarioId, String tipoAviso, String canal) {
            return usuarioId + "|" + tipoAviso + "|" + canal;
        }
    }

    private static final class CompatibilidadFalsa implements CompatibilidadPort {
        private List<UUID> prestadores = List.of();

        @Override public boolean esCompatible(UUID solicitudId, UUID prestadorId) {
            return prestadores.contains(prestadorId);
        }

        @Override public List<UUID> prestadoresCompatibles(UUID solicitudId) { return prestadores; }
    }

    private static final class SolicitudRepositorioFalso implements SolicitudRepository {
        private final Map<UUID, Solicitud> guardadas = new LinkedHashMap<>();

        @Override public Optional<Solicitud> porId(UUID id) { return Optional.ofNullable(guardadas.get(id)); }

        @Override public List<Solicitud> porCliente(UUID clienteId) {
            return guardadas.values().stream().filter(s -> s.clienteId().equals(clienteId)).toList();
        }

        @Override public List<Solicitud> publicadas() {
            return guardadas.values().stream().filter(s -> s.estado() == EstadoSolicitud.PUBLICADA).toList();
        }

        @Override public Solicitud guardar(Solicitud solicitud) {
            guardadas.put(solicitud.id(), solicitud);
            return solicitud;
        }

        @Override public List<Solicitud> buscarPorTexto(String texto) { return List.of(); }
    }
}

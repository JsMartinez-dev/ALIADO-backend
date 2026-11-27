package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.AvisoBandejaRegistrado;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EntregarAvisoBandejaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.CanalEntregaNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.EntregarAvisoBandejaService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoLectura;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.AvisoBandejaRegistradoListener;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.LogCanalEntregaAdapter;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-11: entrega del canal de bandeja disparada por el evento del aviso registrado.
 *
 * <p>Conserva el comportamiento vigente del antiguo worker de 60 segundos —los tipos
 * auditados no gastan intentos ni se reescriben, la preferencia se revalida y un fallo del
 * canal deja el aviso pendiente— sin consultar nunca la tabla de pendientes: la única
 * lectura es el aviso indicado por el evento.</p>
 */
class EntregaAvisoBandejaTest {

    private static final Instant AHORA = Instant.parse("2026-03-01T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    @Test
    void unTipoAuditadoConservaElAvisoYSusIntentosSinLlamarAlCanal() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.ACEPTACION_PROPUESTA, EstadoEntrega.PENDIENTE, 0);
        repositorio.guardadas.put(aviso.id(), aviso);

        servicio(repositorio, new LogCanalEntregaAdapter()).ejecutar(
                new EntregarAvisoBandejaCommand(aviso.id()));

        assertTrue(repositorio.escrituras.isEmpty(),
                "un tipo sin proveedor real no se reescribe: sigue en la bandeja sin gastar intentos");
    }

    @Test
    void elCanalLogEntregaPropuestaRecibidaUnaSolaVez() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.PENDIENTE, 0);
        repositorio.guardadas.put(aviso.id(), aviso);

        servicio(repositorio, new LogCanalEntregaAdapter()).ejecutar(
                new EntregarAvisoBandejaCommand(aviso.id()));

        Notificacion entregada = repositorio.escrituras.get(0);
        assertEquals(EstadoEntrega.ENTREGADA, entregada.estadoEntrega());
        assertEquals(1, entregada.intentos());
        assertNotNull(entregada.fechaEntrega());
        assertNull(repositorio.pendientesConsultadas,
                "la entrega nunca consulta la cola de pendientes");
    }

    @Test
    void sinPreferenciaVigenteNoSeContactaElCanal() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.PENDIENTE, 0);
        repositorio.guardadas.put(aviso.id(), aviso);
        CanalFalso canal = new CanalFalso(true);

        servicio(repositorio, canal, false).ejecutar(new EntregarAvisoBandejaCommand(aviso.id()));

        assertEquals(0, canal.envios);
        assertTrue(repositorio.escrituras.isEmpty());
    }

    @Test
    void unFalloDelCanalDejaElAvisoPendienteConUnIntento() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.PENDIENTE, 0);
        repositorio.guardadas.put(aviso.id(), aviso);

        servicio(repositorio, new CanalQueFalla()).ejecutar(new EntregarAvisoBandejaCommand(aviso.id()));

        Notificacion actualizada = repositorio.escrituras.get(0);
        assertEquals(EstadoEntrega.PENDIENTE, actualizada.estadoEntrega());
        assertEquals(1, actualizada.intentos());
        assertNull(actualizada.fechaEntrega());
    }

    @Test
    void alAgotarLosIntentosElAvisoQuedaFallido() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.PENDIENTE, 2);
        repositorio.guardadas.put(aviso.id(), aviso);

        servicio(repositorio, new CanalQueFalla()).ejecutar(new EntregarAvisoBandejaCommand(aviso.id()));

        assertEquals(EstadoEntrega.FALLIDA, repositorio.escrituras.get(0).estadoEntrega());
    }

    @Test
    void unAvisoYaEntregadoNoSeReentrega() {
        RepositorioFalso repositorio = new RepositorioFalso();
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.ENTREGADA, 1);
        repositorio.guardadas.put(aviso.id(), aviso);
        CanalFalso canal = new CanalFalso(true);

        servicio(repositorio, canal).ejecutar(new EntregarAvisoBandejaCommand(aviso.id()));

        assertEquals(0, canal.envios);
        assertTrue(repositorio.escrituras.isEmpty());
    }

    @Test
    void unAvisoInexistenteNoHaceNada() {
        RepositorioFalso repositorio = new RepositorioFalso();
        CanalFalso canal = new CanalFalso(true);

        servicio(repositorio, canal).ejecutar(new EntregarAvisoBandejaCommand(UUID.randomUUID()));

        assertEquals(0, canal.envios);
        assertTrue(repositorio.escrituras.isEmpty());
    }

    @Test
    void elListenerDelEventoEntregaExactamenteElAvisoRegistrado() {
        List<UUID> entregados = new ArrayList<>();
        AvisoBandejaRegistradoListener listener = new AvisoBandejaRegistradoListener(
                command -> entregados.add(command.notificacionId()));

        UUID primero = UUID.randomUUID();
        UUID segundo = UUID.randomUUID();
        listener.alRegistrarse(new AvisoBandejaRegistrado(primero));
        listener.alRegistrarse(new AvisoBandejaRegistrado(segundo));

        assertEquals(List.of(primero, segundo), entregados);
    }

    @Test
    void laEntregaPreservaLaFechaDeLecturaExistente() {
        RepositorioFalso repositorio = new RepositorioFalso();
        OffsetDateTime leida = OffsetDateTime.ofInstant(AHORA.minusSeconds(60), ZoneOffset.UTC);
        Notificacion aviso = aviso(TipoNotificacion.PROPUESTA_RECIBIDA, EstadoEntrega.PENDIENTE, 0, leida);
        repositorio.guardadas.put(aviso.id(), aviso);

        servicio(repositorio, new LogCanalEntregaAdapter()).ejecutar(
                new EntregarAvisoBandejaCommand(aviso.id()));

        Notificacion entregada = repositorio.escrituras.get(0);
        assertEquals(EstadoEntrega.ENTREGADA, entregada.estadoEntrega());
        assertEquals(leida, entregada.leidaEn(),
                "la entrega no es una lectura: conserva la primera fecha de lectura");
    }

    private static EntregarAvisoBandejaService servicio(NotificacionRepository repositorio,
                                                        CanalEntregaNotificacion canal) {
        return servicio(repositorio, canal, true);
    }

    private static EntregarAvisoBandejaService servicio(NotificacionRepository repositorio,
                                                        CanalEntregaNotificacion canal,
                                                        boolean preferenciaHabilitada) {
        ConsultaPreferenciasPort preferencias = (usuarioId, tipoAviso, canalAviso) -> preferenciaHabilitada;
        return new EntregarAvisoBandejaService(repositorio, canal, preferencias, RELOJ);
    }

    private static Notificacion aviso(TipoNotificacion tipo, EstadoEntrega estado, int intentos) {
        return aviso(tipo, estado, intentos, null);
    }

    private static Notificacion aviso(TipoNotificacion tipo, EstadoEntrega estado, int intentos,
                                      OffsetDateTime leidaEn) {
        return new Notificacion(UUID.randomUUID(), UUID.randomUUID(), tipo, UUID.randomUUID(), 1,
                "detalle", Canal.PUSH, estado, intentos,
                OffsetDateTime.ofInstant(AHORA.minusSeconds(5), ZoneOffset.UTC), null, leidaEn);
    }

    static final class RepositorioFalso implements NotificacionRepository {
        final Map<UUID, Notificacion> guardadas = new LinkedHashMap<>();
        final List<Notificacion> escrituras = new ArrayList<>();
        Object pendientesConsultadas;

        @Override public boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId,
                                              int eventVersion) { return false; }
        @Override public List<Notificacion> porDestinatario(UUID destinatarioId) { return List.of(); }
        @Override public List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo) {
            return List.of();
        }
        @Override public Optional<Notificacion> porId(UUID id) {
            return Optional.ofNullable(guardadas.get(id));
        }
        @Override public Notificacion guardar(Notificacion notificacion) {
            escrituras.add(notificacion);
            guardadas.put(notificacion.id(), notificacion);
            return notificacion;
        }
        @Override public boolean insertarSiNoExiste(Notificacion notificacion) {
            return guardadas.putIfAbsent(notificacion.id(), notificacion) == null;
        }
        @Override public boolean marcarLeida(UUID id, UUID destinatarioId, OffsetDateTime leidaEn) {
            Notificacion actual = guardadas.get(id);
            if (actual == null || !actual.destinatarioId().equals(destinatarioId)) {
                return false;
            }
            if (actual.leidaEn() == null) {
                guardadas.put(id, conLectura(actual, leidaEn));
            }
            return true;
        }
        @Override public int marcarTodasLeidas(UUID destinatarioId, OffsetDateTime leidaEn) {
            int actualizadas = 0;
            for (Notificacion actual : List.copyOf(guardadas.values())) {
                if (actual.destinatarioId().equals(destinatarioId) && actual.leidaEn() == null) {
                    guardadas.put(actual.id(), conLectura(actual, leidaEn));
                    actualizadas++;
                }
            }
            return actualizadas;
        }
        @Override public List<Notificacion> buscarBandeja(UUID destinatarioId,
                EstadoLectura estadoLectura, TipoNotificacion tipo, OffsetDateTime antesDeFecha,
                UUID antesDeId, int limite) {
            return List.of();
        }
        @Override public boolean hayNoLeidas(UUID destinatarioId) { return false; }

        private static Notificacion conLectura(Notificacion n, OffsetDateTime leidaEn) {
            return new Notificacion(n.id(), n.destinatarioId(), n.tipo(), n.aggregateId(),
                    n.eventVersion(), n.detalle(), n.canal(), n.estadoEntrega(), n.intentos(),
                    n.fechaGeneracion(), n.fechaEntrega(), leidaEn);
        }
    }

    static final class CanalFalso implements CanalEntregaNotificacion {
        final boolean disponible;
        int envios;

        CanalFalso(boolean disponible) {
            this.disponible = disponible;
        }

        @Override public boolean disponibleParaEntrega(Notificacion notificacion) {
            return disponible;
        }

        @Override public boolean entregar(Notificacion notificacion) {
            envios++;
            return true;
        }
    }

    static final class CanalQueFalla implements CanalEntregaNotificacion {
        @Override public boolean entregar(Notificacion notificacion) {
            throw new IllegalStateException("proveedor no disponible");
        }
    }
}

package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.EnvioPushPendiente;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.IniciarEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.PrepararEntregasPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarResultadoEntregaPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.port.RevalidacionOportunidadPushPort;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.IniciarEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.PrepararEntregasPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarResultadoEntregaPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Canal;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.CodigosEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ConfiguracionCanalPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.DestinoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntrega;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.EstadoTrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.LoteReclamadoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.Notificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PoliticaReintentoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.PendientesPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEntregaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.ResultadoEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoNotificacion;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TipoReclamacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.TrabajoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VentanaEnvioPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.ReclamacionTrabajoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.infrastructure.adapter.MetricasPushEnMemoria;
import com.sebastian.spring.mas.aliado.aliado.modules.preferencias.application.port.ConsultaPreferenciasPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F09-PUSH-08: preparación idempotente, revalidación por destinatario, supresión y
 * política de reintento/cierre del padre con dobles de repositorio y reloj controlado.
 */
class ProcesamientoPushUseCasesTest {

    private static final Instant AHORA = Instant.parse("2026-03-01T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);
    private static final UUID PRESTADOR = UUID.randomUUID();
    private static final UUID SOLICITUD = UUID.randomUUID();
    private static final UUID NOTIFICACION = UUID.randomUUID();
    private static final UUID TRABAJO = UUID.randomUUID();
    private static final UUID INSTALACION_A = UUID.randomUUID();
    private static final UUID INSTALACION_B = UUID.randomUUID();

    private RepositorioPushFalso reclamaciones;
    private NotificacionRepositorioFalso notificaciones;
    private DispositivoRepositorioFalso dispositivos;
    private PreferenciasFalsas preferencias;
    private RevalidacionFalsa revalidacion;
    private MetricasPushEnMemoria metricas;

    @BeforeEach
    void preparar() {
        reclamaciones = new RepositorioPushFalso();
        notificaciones = new NotificacionRepositorioFalso();
        dispositivos = new DispositivoRepositorioFalso();
        preferencias = new PreferenciasFalsas();
        revalidacion = new RevalidacionFalsa();
        metricas = new MetricasPushEnMemoria(RELOJ);
        reclamaciones.trabajos.put(TRABAJO, trabajo(EstadoTrabajoPush.PENDIENTE_PREPARACION,
                AHORA.plusSeconds(3_600)));
        notificaciones.guardadas.put(NOTIFICACION, aviso());
        dispositivos.vinculos.add(vinculo(INSTALACION_A, 1, "token-a"));
        dispositivos.vinculos.add(vinculo(INSTALACION_B, 1, "token-b"));
    }

    @Test
    void conElCanalApagadoLaPreparacionCierraSinCrearEntregasNiTocarLaBandeja() {
        prepararServicio(false).ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertEquals(List.of("CANAL_NO_HABILITADO"), reclamaciones.motivos);
        assertTrue(reclamaciones.destinos.isEmpty(), "no se prepara ningún envío");
        assertFalse(reclamaciones.confirmada, "el padre no queda PREPARADO");
        assertEquals(0, notificaciones.insertados.get(), "el aviso de bandeja no se toca");
    }

    @Test
    void conElCanalApagadoElEnvioSeSuprimeSinLlamarAlProveedor() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));

        Optional<EnvioPushPendiente> envio = iniciarServicio(false).ejecutar(
                new IniciarEntregaPushCommand(envioReclamacion(ENTREGA_A, INSTALACION_A, 1)));

        assertTrue(envio.isEmpty(), "sin envío no hay mensaje para el proveedor");
        assertEquals(EstadoEntregaPush.SUPRIMIDA, reclamaciones.resultados.get(0).estado());
        assertEquals(CodigosEntregaPush.CANAL_NO_HABILITADO,
                reclamaciones.resultados.get(0).ultimoCodigoError());
        assertEquals(0, reclamaciones.resultados.get(0).intentos(),
                "un apagado no consume intentos de la entrega");
        assertTrue(reclamaciones.cerrada, "el cierre del padre sigue evaluándose");
    }

    @Test
    void preparacionCreaUnaEntregaPorVinculoYConfirmaElPadreSinRepetirMatching() {
        PrepararEntregasPushService servicio = prepararServicio();

        servicio.ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertEquals(List.of(new DestinoPush(INSTALACION_A, 1), new DestinoPush(INSTALACION_B, 1)),
                reclamaciones.destinos);
        assertTrue(reclamaciones.confirmada, "la preparación marca el padre");
        assertEquals(List.of("confirmar", "crear", "cierre"),
                reclamaciones.orden,
                "el padre se confirma antes de crear las entregas, en la misma transacción");
        assertTrue(reclamaciones.motivos.isEmpty());
        assertEquals(0, notificaciones.insertados.get(), "no se crea un aviso nuevo");
    }

    @Test
    void preparacionSinConfirmarNoCreaEntregasNiCierraElPadre() {
        reclamaciones.confirmar = false;

        prepararServicio().ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertTrue(reclamaciones.destinos.isEmpty(),
                "una reclamación vencida no deja entregas creadas");
        assertFalse(reclamaciones.cerrada);
        assertTrue(reclamaciones.motivos.isEmpty());
        assertEquals(List.of("confirmar"), reclamaciones.orden);
    }

    @Test
    void preparacionSinDispositivosCierraElPadreConSuMotivo() {
        dispositivos.vinculos.clear();

        prepararServicio().ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertTrue(reclamaciones.destinos.isEmpty());
        assertEquals(List.of("SIN_DISPOSITIVOS_ELEGIBLES"), reclamaciones.motivos);
        assertFalse(reclamaciones.confirmada);
    }

    @Test
    void preparacionConPreferenciaApagadaNoContactaAlProveedorNiCreaEntregas() {
        preferencias.habilitado = false;

        prepararServicio().ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertTrue(reclamaciones.destinos.isEmpty());
        assertEquals(List.of("PREFERENCIA_DESHABILITADA"), reclamaciones.motivos);
    }

    @Test
    void preparacionConOportunidadCerradaCierraElPadre() {
        revalidacion.vigente = false;

        prepararServicio().ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertTrue(reclamaciones.destinos.isEmpty());
        assertEquals(List.of("OPORTUNIDAD_NO_VIGENTE"), reclamaciones.motivos);
    }

    @Test
    void preparacionVencidaCierraElPadreSinEntregas() {
        reclamaciones.trabajos.put(TRABAJO, trabajo(EstadoTrabajoPush.PENDIENTE_PREPARACION, AHORA));

        prepararServicio().ejecutar(new PrepararEntregasPushCommand(preparacion()));

        assertTrue(reclamaciones.destinos.isEmpty());
        assertEquals(List.of("VIGENCIA_AGOTADA"), reclamaciones.motivos);
    }

    @Test
    void iniciarEnvioDevuelveElMensajeConElTokenVigenteRevalidado() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));

        Optional<EnvioPushPendiente> envio = iniciarServicio().ejecutar(
                new IniciarEntregaPushCommand(envioReclamacion(ENTREGA_A, INSTALACION_A, 1)));

        assertTrue(envio.isPresent());
        assertEquals("token-a", envio.get().mensaje().tokenFcm());
        assertEquals(NOTIFICACION, envio.get().mensaje().notificacionId());
        assertEquals(SOLICITUD, envio.get().mensaje().solicitudId());
        assertEquals(0, envio.get().intentos());
        assertTrue(reclamaciones.resultados.isEmpty(), "sin supresión no se escribe resultado");
    }

    @Test
    void iniciarEnvioSuprimeCuandoElVinculoSeRevoco() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));
        dispositivos.vinculos.replaceAll(v -> v.instalacionId().equals(INSTALACION_A)
                ? revocado(v) : v);

        Optional<EnvioPushPendiente> envio = iniciarServicio().ejecutar(
                new IniciarEntregaPushCommand(envioReclamacion(ENTREGA_A, INSTALACION_A, 1)));

        assertTrue(envio.isEmpty());
        assertEquals(EstadoEntregaPush.SUPRIMIDA, reclamaciones.resultados.get(0).estado());
        assertEquals(CodigosEntregaPush.VINCULO_REVOCADO,
                reclamaciones.resultados.get(0).ultimoCodigoError());
        assertTrue(reclamaciones.cerrada, "la supresión evalúa el cierre del padre");
    }

    @Test
    void iniciarEnvioSuprimeSiLaGeneracionDelTokenCambio() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));
        dispositivos.vinculos.replaceAll(v -> v.instalacionId().equals(INSTALACION_A)
                ? vinculo(INSTALACION_A, 2, "token-rotado") : v);

        iniciarServicio().ejecutar(new IniciarEntregaPushCommand(
                envioReclamacion(ENTREGA_A, INSTALACION_A, 1)));

        assertEquals(CodigosEntregaPush.GENERACION_OBSOLETA,
                reclamaciones.resultados.get(0).ultimoCodigoError());
    }

    @Test
    void iniciarEnvioSuprimePorPreferenciaApagada() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));
        preferencias.habilitado = false;

        assertTrue(iniciarServicio().ejecutar(new IniciarEntregaPushCommand(
                envioReclamacion(ENTREGA_A, INSTALACION_A, 1))).isEmpty());
        assertEquals(CodigosEntregaPush.PREFERENCIA_DESHABILITADA,
                reclamaciones.resultados.get(0).ultimoCodigoError());
    }

    @Test
    void iniciarEnvioSuprimePorOportunidadCerrada() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));
        revalidacion.vigente = false;

        assertTrue(iniciarServicio().ejecutar(new IniciarEntregaPushCommand(
                envioReclamacion(ENTREGA_A, INSTALACION_A, 1))).isEmpty());
        assertEquals(CodigosEntregaPush.OPORTUNIDAD_NO_VIGENTE,
                reclamaciones.resultados.get(0).ultimoCodigoError());
    }

    @Test
    void iniciarEnvioExpiraSiElTrabajoYaVencio() {
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));
        reclamaciones.trabajos.put(TRABAJO, trabajo(EstadoTrabajoPush.PREPARADO, AHORA));

        assertTrue(iniciarServicio().ejecutar(new IniciarEntregaPushCommand(
                envioReclamacion(ENTREGA_A, INSTALACION_A, 1))).isEmpty());
        assertEquals(EstadoEntregaPush.EXPIRADA, reclamaciones.resultados.get(0).estado());
        assertEquals(CodigosEntregaPush.VIGENCIA_AGOTADA,
                reclamaciones.resultados.get(0).ultimoCodigoError());
    }

    @Test
    void iniciarEnvioLiberaLaReclamacionCuandoNoAlcanzaLaVentana() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));

        Optional<EnvioPushPendiente> envio = iniciarServicio().ejecutar(
                new IniciarEntregaPushCommand(reclamacionConVencimiento(AHORA.plusSeconds(3))));

        assertTrue(envio.isEmpty(), "no hay tiempo para el timeout y el margen del resultado");
        assertTrue(reclamaciones.liberada,
                "la reclamación se libera para reclamarse con un vencimiento nuevo");
        assertTrue(reclamaciones.resultados.isEmpty(),
                "liberar la reclamación no marca un resultado terminal");
    }

    @Test
    void iniciarEnvioPermiteLaVentanaCuandoElTiempoRestanteAlcanza() {
        marcarPreparado();
        reclamaciones.entregas.put(ENTREGA_A, entrega(ENTREGA_A, INSTALACION_A, 1, 0));

        Optional<EnvioPushPendiente> envio = iniciarServicio().ejecutar(
                new IniciarEntregaPushCommand(reclamacionConVencimiento(AHORA.plusSeconds(5))));

        assertTrue(envio.isPresent(), "5 s restantes cubren la ventana de 5 s sin margen");
        assertFalse(reclamaciones.liberada);
    }

    @Test
    void aceptacionMarcaLaEntregaTerminalYCierraElPadre() {
        EnvioPushPendiente envio = envio(0, AHORA.plusSeconds(3_600));

        registrarServicio().ejecutar(new RegistrarResultadoEntregaPushCommand(envio,
                ResultadoEnvioPush.aceptado("fcm-1")));

        ResultadoEntregaPush guardado = reclamaciones.resultados.get(0);
        assertEquals(EstadoEntregaPush.ACEPTADA, guardado.estado());
        assertEquals(1, guardado.intentos());
        assertEquals("fcm-1", guardado.idMensajeFcm());
        assertNull(guardado.proximoIntentoEn());
        assertTrue(reclamaciones.cerrada);
    }

    @Test
    void errorTemporalProgramaElMaximoDeRetryAfterYBackoffConJitter() {
        RegistrarResultadoEntregaPushService servicio = registrarServicio();

        servicio.ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(0, AHORA.plusSeconds(3_600)),
                ResultadoEnvioPush.temporal("UNAVAILABLE", AHORA.plusSeconds(120))));
        servicio.ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(0, AHORA.plusSeconds(3_600)),
                ResultadoEnvioPush.temporal("UNAVAILABLE", AHORA.plusSeconds(5))));

        assertEquals(AHORA.plusSeconds(120), reclamaciones.resultados.get(0).proximoIntentoEn());
        assertEquals(AHORA.plusSeconds(30), reclamaciones.resultados.get(1).proximoIntentoEn());
        assertEquals(EstadoEntregaPush.PENDIENTE, reclamaciones.resultados.get(1).estado());
        assertEquals(1, reclamaciones.resultados.get(1).intentos());
    }

    @Test
    void errorTemporalAgotadoTerminaFallidaDefinitiva() {
        registrarServicio().ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(4, AHORA.plusSeconds(3_600)),
                ResultadoEnvioPush.temporal("UNAVAILABLE", null)));

        ResultadoEntregaPush guardado = reclamaciones.resultados.get(0);
        assertEquals(EstadoEntregaPush.FALLIDA_DEFINITIVA, guardado.estado());
        assertEquals(CodigosEntregaPush.INTENTOS_AGOTADOS, guardado.ultimoCodigoError());
        assertEquals(5, guardado.intentos());
    }

    @Test
    void errorTemporalFueraDeVigenciaTerminaExpirada() {
        registrarServicio().ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(0, AHORA.plusSeconds(20)),
                ResultadoEnvioPush.temporal("UNAVAILABLE", null)));

        ResultadoEntregaPush guardado = reclamaciones.resultados.get(0);
        assertEquals(EstadoEntregaPush.EXPIRADA, guardado.estado());
        assertEquals(CodigosEntregaPush.VIGENCIA_AGOTADA, guardado.ultimoCodigoError());
        assertNull(guardado.proximoIntentoEn());
    }

    @Test
    void tokenDesregistradoRevocaSoloLaGeneracionEnviadaYSuprime() {
        registrarServicio().ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(1, AHORA.plusSeconds(3_600)),
                ResultadoEnvioPush.tokenDesregistrado("UNREGISTERED")));

        assertEquals(EstadoEntregaPush.SUPRIMIDA, reclamaciones.resultados.get(0).estado());
        assertEquals(CodigosEntregaPush.TOKEN_DESREGISTRADO,
                reclamaciones.resultados.get(0).ultimoCodigoError());
        assertEquals(INSTALACION_A, dispositivos.revocadaInstalacion);
        assertEquals(1L, dispositivos.revocadaGeneracion);
    }

    @Test
    void resultadoTardioNoCierraElPadre() {
        reclamaciones.guardarResultado = false;

        registrarServicio().ejecutar(new RegistrarResultadoEntregaPushCommand(
                envio(0, AHORA.plusSeconds(3_600)), ResultadoEnvioPush.aceptado("fcm-1")));

        assertFalse(reclamaciones.cerrada, "sin escritura no se evalúa el cierre");
    }

    private void marcarPreparado() {
        reclamaciones.trabajos.put(TRABAJO,
                trabajo(EstadoTrabajoPush.PREPARADO, AHORA.plusSeconds(3_600)));
    }

    private PrepararEntregasPushService prepararServicio() {
        return prepararServicio(true);
    }

    private PrepararEntregasPushService prepararServicio(boolean canalHabilitado) {
        return new PrepararEntregasPushService(reclamaciones, notificaciones, dispositivos,
                preferencias, revalidacion, configuracion(canalHabilitado), metricas);
    }

    private IniciarEntregaPushService iniciarServicio() {
        return iniciarServicio(true);
    }

    private IniciarEntregaPushService iniciarServicio(boolean canalHabilitado) {
        return new IniciarEntregaPushService(reclamaciones, notificaciones, dispositivos,
                preferencias, revalidacion,
                new VentanaEnvioPush(Duration.ofSeconds(5), Duration.ZERO),
                configuracion(canalHabilitado), metricas, RELOJ);
    }

    private static ConfiguracionCanalPush configuracion(boolean habilitado) {
        return ConfiguracionCanalPush.desde(habilitado, "NUEVA_OPORTUNIDAD");
    }

    private RegistrarResultadoEntregaPushService registrarServicio() {
        // Jitter 0 para que el backoff sea determinista en las aserciones.
        return new RegistrarResultadoEntregaPushService(reclamaciones, dispositivos,
                new PoliticaReintentoPush(5, Duration.ofSeconds(30), () -> 0.0), metricas, RELOJ);
    }

    private static ReclamacionPush preparacion() {
        return ReclamacionPush.preparacion(TRABAJO, UUID.randomUUID(),
                AHORA.plusSeconds(45));
    }

    private static ReclamacionPush envioReclamacion(UUID entrega, UUID instalacion, long generacion) {
        return ReclamacionPush.envio(entrega, TRABAJO, instalacion, generacion,
                UUID.randomUUID(), AHORA.plusSeconds(45));
    }

    private static ReclamacionPush reclamacionConVencimiento(Instant venceEn) {
        return ReclamacionPush.envio(ENTREGA_A, TRABAJO, INSTALACION_A, 1, RECLAMACION, venceEn);
    }

    private static EnvioPushPendiente envio(int intentos, Instant expiraEn) {
        return new EnvioPushPendiente(ENTREGA_A, TRABAJO, INSTALACION_A, 1, RECLAMACION,
                intentos, expiraEn, AHORA,
                new com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.MensajePush(
                        NOTIFICACION, SOLICITUD, TipoNotificacion.NUEVA_OPORTUNIDAD, "token-a"));
    }

    private static final UUID ENTREGA_A = UUID.randomUUID();
    private static final UUID RECLAMACION = UUID.randomUUID();

    private static Notificacion aviso() {
        return new Notificacion(NOTIFICACION, PRESTADOR, TipoNotificacion.NUEVA_OPORTUNIDAD,
                SOLICITUD, 1, "detalle", Canal.PUSH, EstadoEntrega.PENDIENTE, 0,
                OffsetDateTime.ofInstant(AHORA, ZoneOffset.UTC), null, null);
    }

    private static TrabajoPush trabajo(EstadoTrabajoPush estado, Instant expiraEn) {
        return new TrabajoPush(TRABAJO, NOTIFICACION, estado, AHORA.minusSeconds(60), expiraEn,
                estado == EstadoTrabajoPush.FINALIZADO ? "MOTIVO" : null);
    }

    private static VinculoPush vinculo(UUID instalacion, long generacion, String token) {
        return new VinculoPush(instalacion, generacion, PRESTADOR, token, null,
                new byte[32], OffsetDateTime.ofInstant(AHORA, ZoneOffset.UTC), null);
    }

    private static VinculoPush revocado(VinculoPush v) {
        return new VinculoPush(v.instalacionId(), v.generacion(), v.usuarioId(), null, null,
                v.revocacionSha256(), v.vinculadoEn(), OffsetDateTime.ofInstant(AHORA, ZoneOffset.UTC));
    }

    private static EntregaPush entrega(UUID id, UUID instalacion, long generacion, int intentos) {
        return new EntregaPush(id, TRABAJO, instalacion, generacion, EstadoEntregaPush.PENDIENTE,
                intentos, AHORA);
    }

    private static final class RepositorioPushFalso implements ReclamacionTrabajoPushRepository {
        final Map<UUID, TrabajoPush> trabajos = new LinkedHashMap<>();
        final Map<UUID, EntregaPush> entregas = new LinkedHashMap<>();
        final List<DestinoPush> destinos = new ArrayList<>();
        final List<ResultadoEntregaPush> resultados = new ArrayList<>();
        final List<String> motivos = new ArrayList<>();
        final List<String> orden = new ArrayList<>();
        boolean confirmar = true;
        boolean confirmada;
        boolean cerrada;
        boolean liberada;
        boolean guardarResultado = true;

        @Override public LoteReclamadoPush reclamar(int maximo, Duration vencimiento) {
            return LoteReclamadoPush.vacio();
        }
        @Override public boolean confirmarPreparacion(UUID trabajoId, UUID reclamacionId) {
            orden.add("confirmar");
            confirmada = confirmar;
            return confirmar;
        }
        @Override public boolean guardarResultadoEntrega(ResultadoEntregaPush resultado) {
            if (!guardarResultado) {
                return false;
            }
            resultados.add(resultado);
            return true;
        }
        @Override public boolean liberarReclamacionDeEntrega(UUID entregaId, UUID reclamacionId) {
            liberada = true;
            return true;
        }
        @Override public Optional<Instant> proximoPlazo() { return Optional.empty(); }
        @Override public PendientesPush pendientes() {
            return new PendientesPush(trabajos.size(), entregas.size(), null);
        }
        @Override public Optional<TrabajoPush> buscarTrabajo(UUID trabajoId) {
            return Optional.ofNullable(trabajos.get(trabajoId));
        }
        @Override public Optional<EntregaPush> buscarEntrega(UUID entregaId) {
            return Optional.ofNullable(entregas.get(entregaId));
        }
        @Override public boolean trabajoExpirado(UUID trabajoId) {
            TrabajoPush trabajo = trabajos.get(trabajoId);
            return trabajo != null && trabajo.expiradoEn(RELOJ.instant());
        }
        @Override public void crearEntregas(UUID trabajoId, List<DestinoPush> destinos) {
            orden.add("crear");
            this.destinos.addAll(destinos);
        }
        @Override public boolean finalizarTrabajo(UUID trabajoId, UUID reclamacionId, String motivo) {
            motivos.add(motivo);
            return true;
        }
        @Override public boolean cerrarPadreSiCorresponde(UUID trabajoId) {
            orden.add("cierre");
            cerrada = true;
            return true;
        }
    }

    private static final class NotificacionRepositorioFalso implements NotificacionRepository {
        final Map<UUID, Notificacion> guardadas = new LinkedHashMap<>();
        final AtomicInteger insertados = new AtomicInteger();

        @Override public boolean existeEvento(String tipo, UUID aggregateId, UUID destinatarioId,
                                              int eventVersion) { return false; }
        @Override public List<Notificacion> porDestinatario(UUID destinatarioId) { return List.of(); }
        @Override public List<Notificacion> porDestinatarioYTipo(UUID destinatarioId, String tipo) {
            return List.of();
        }
        @Override public Optional<Notificacion> porId(UUID id) {
            return Optional.ofNullable(guardadas.get(id));
        }
        @Override public Notificacion guardar(Notificacion notificacion) { return notificacion; }
        @Override public boolean insertarSiNoExiste(Notificacion notificacion) {
            insertados.incrementAndGet();
            return true;
        }
    }

    private static final class DispositivoRepositorioFalso implements DispositivoPushRepository {
        final List<VinculoPush> vinculos = new ArrayList<>();
        UUID revocadaInstalacion;
        long revocadaGeneracion;

        @Override public void bloquearRegistro() { }
        @Override public void asegurarInstalacion(InstalacionPush instalacion) { }
        @Override public Optional<InstalacionPush> buscarInstalacion(UUID instalacionId) {
            return Optional.empty();
        }
        @Override public Optional<InstalacionPush> bloquearInstalacion(UUID instalacionId) {
            return Optional.empty();
        }
        @Override public Optional<VinculoPush> vinculoActivo(UUID instalacionId) {
            return vinculos.stream()
                    .filter(v -> v.instalacionId().equals(instalacionId) && v.activo())
                    .findFirst();
        }
        @Override public List<VinculoPush> vinculosActivosDe(UUID usuarioId) {
            return vinculos.stream().filter(VinculoPush::activo)
                    .filter(v -> v.usuarioId().equals(usuarioId)).toList();
        }
        @Override public boolean existeCredencial(byte[] huellaCredencial) { return false; }
        @Override public Optional<UUID> instalacionConTokenActivo(byte[] huellaToken) {
            return Optional.empty();
        }
        @Override public long siguienteGeneracion(UUID instalacionId) { return 1; }
        @Override public void guardarVinculo(VinculoPush vinculo) { }
        @Override public boolean revocarVinculo(UUID instalacionId, long generacion,
                                                OffsetDateTime momento) {
            revocadaInstalacion = instalacionId;
            revocadaGeneracion = generacion;
            return true;
        }
        @Override public boolean revocarVinculoPropio(UUID instalacionId, UUID usuarioId,
                                                      byte[] huellaCredencial, OffsetDateTime momento) {
            return false;
        }
        @Override public boolean revocarVinculoPorCredencial(UUID instalacionId,
                                                             byte[] huellaCredencial,
                                                             OffsetDateTime momento) {
            return false;
        }
    }

    private static final class PreferenciasFalsas implements ConsultaPreferenciasPort {
        boolean habilitado = true;

        @Override public boolean avisoHabilitado(UUID usuarioId, String tipoAviso, String canal) {
            return habilitado;
        }
    }

    private static final class RevalidacionFalsa implements RevalidacionOportunidadPushPort {
        boolean vigente = true;

        @Override public boolean sigueVigente(UUID solicitudId, UUID prestadorId) {
            return vigente;
        }
    }
}

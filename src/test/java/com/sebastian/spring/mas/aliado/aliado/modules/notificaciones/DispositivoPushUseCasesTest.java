package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RegistrarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RetirarDispositivoPushCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.RevocarVinculoPushDiferidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RegistrarDispositivoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RetirarDispositivoPushService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.RevocarVinculoPushDiferidoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.HuellaPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.InstalacionPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.model.VinculoPush;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.DispositivoPushRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.dto.NotificacionDtos;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.presentation.mapper.NotificacionPresentationMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de la API autenticada de dispositivos push (F09-PUSH-01/D-08). */
class DispositivoPushUseCasesTest {
    private static final String PRUEBA = "a".repeat(43);
    private static final String OTRA_PRUEBA = "b".repeat(43);
    private static final String CREDENCIAL_1 = "c".repeat(43);
    private static final String CREDENCIAL_2 = "d".repeat(43);
    private static final String CREDENCIAL_3 = "e".repeat(43);
    private static final String TOKEN_1 = "token-fcm-instalacion-1";
    private static final String TOKEN_2 = "token-fcm-instalacion-2";

    private final FakeDispositivoPushRepository repositorio = new FakeDispositivoPushRepository();
    private final RegistrarDispositivoPushService registrar = new RegistrarDispositivoPushService(repositorio);
    private final RetirarDispositivoPushService retirar = new RetirarDispositivoPushService(repositorio);
    private final RevocarVinculoPushDiferidoService revocar = new RevocarVinculoPushDiferidoService(repositorio);

    @Test
    void altaCreaLaPrimeraGeneracion() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();

        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        InstalacionPush guardada = repositorio.buscarInstalacion(instalacion).orElseThrow();
        assertEquals("ANDROID", guardada.plataforma());
        assertTrue(guardada.coincidePrueba(HuellaPush.de(PRUEBA).valor()));
        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(1, activo.generacion());
        assertEquals(usuario, activo.usuarioId());
        assertEquals(TOKEN_1, activo.tokenFcm());
    }

    @Test
    void repetirElPutIdenticoConservaLaGeneracion() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        assertEquals(1, repositorio.vinculos(instalacion).size());
        assertEquals(1, repositorio.vinculoActivo(instalacion).orElseThrow().generacion());
    }

    @Test
    void otraCredencialParaElMismoTitularYTokenNoReemplazaElVinculo() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_2)));

        assertEquals("VINCULO_PUSH_EN_USO", ex.code());
        assertEquals(409, ex.status());
        assertEquals(CREDENCIAL_1, credencialActiva(instalacion));
        assertEquals(1, repositorio.vinculos(instalacion).size());
    }

    @Test
    void laRotacionDelTokenAbreUnaGeneracionNuevaYRetiraLaAnterior() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_2, CREDENCIAL_2));

        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(2, activo.generacion());
        assertEquals(TOKEN_2, activo.tokenFcm());
        VinculoPush anterior = repositorio.vinculos(instalacion).stream()
                .filter(v -> v.generacion() == 1).findFirst().orElseThrow();
        assertFalse(anterior.activo());
        assertNull(anterior.tokenFcm());
        assertEquals(2, repositorio.vinculos(instalacion).size());
    }

    @Test
    void unaCredencialRetiradaNoReactivaElVinculo() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_2, CREDENCIAL_1)));

        assertEquals("VINCULO_PUSH_EN_USO", ex.code());
        assertEquals(1, repositorio.vinculoActivo(instalacion).orElseThrow().generacion());
    }

    @Test
    void unPutAtrasadoConElTokenAnteriorNoRevierteLaRotacion() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_2, CREDENCIAL_2));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1)));

        assertEquals("VINCULO_PUSH_EN_USO", ex.code());
        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(2, activo.generacion());
        assertEquals(TOKEN_2, activo.tokenFcm());
        assertTrue(activo.coincideCredencial(HuellaPush.de(CREDENCIAL_2).valor()));
    }

    @Test
    void elCambioDeCuentaRevocaLaAsociacionPrevia() {
        UUID instalacion = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));

        registrar.ejecutar(alta(instalacion, usuarioB, PRUEBA, TOKEN_1, CREDENCIAL_2));

        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(usuarioB, activo.usuarioId());
        assertEquals(2, activo.generacion());
        assertEquals(CREDENCIAL_2, credencialActiva(instalacion));
        assertFalse(repositorio.vinculos(instalacion).stream()
                .filter(v -> v.generacion() == 1).findFirst().orElseThrow().activo());
    }

    @Test
    void unTokenActivoEnOtraInstalacionDevuelveConflicto() {
        UUID primera = UUID.randomUUID();
        UUID segunda = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(primera, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(alta(segunda, usuarioB, OTRA_PRUEBA, TOKEN_1, CREDENCIAL_2)));

        assertEquals("TOKEN_PUSH_EN_USO", ex.code());
        assertEquals(409, ex.status());
        assertTrue(repositorio.vinculos(segunda).isEmpty());
    }

    @Test
    void elUuidSinLaPruebaCorrectaNoReasignaUnaInstalacionAjena() {
        UUID instalacion = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(alta(instalacion, usuarioB, OTRA_PRUEBA, TOKEN_2, CREDENCIAL_2)));

        assertEquals("INSTALACION_NO_DISPONIBLE", ex.code());
        assertEquals(404, ex.status());
        assertEquals(usuarioA, repositorio.vinculoActivo(instalacion).orElseThrow().usuarioId());
        assertTrue(repositorio.vinculoActivo(instalacion).orElseThrow().coincideCredencial(
                HuellaPush.de(CREDENCIAL_1).valor()));
    }

    @Test
    void elPermisoApagadoRetiraLaAsociacionSinExigirCredencial() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        registrar.ejecutar(new RegistrarDispositivoPushCommand(instalacion, usuario, PRUEBA,
                "ANDROID", false, null, null));

        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());
        assertFalse(repositorio.vinculos(instalacion).getFirst().activo());
        assertNull(repositorio.vinculos(instalacion).getFirst().tokenFcm());
    }

    @Test
    void repetirElPermisoApagadoEsSeguro() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));
        var apagado = new RegistrarDispositivoPushCommand(instalacion, usuario, PRUEBA,
                "ANDROID", false, null, null);

        registrar.ejecutar(apagado);
        registrar.ejecutar(apagado);

        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());
    }

    @Test
    void elPermisoApagadoDeOtraCuentaNoRevocaElVinculoAjeno() {
        UUID instalacion = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));

        registrar.ejecutar(new RegistrarDispositivoPushCommand(instalacion, usuarioB, PRUEBA,
                "ANDROID", false, null, null));

        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(usuarioA, activo.usuarioId());
        assertEquals(CREDENCIAL_1, credencialActiva(instalacion));
    }

    @Test
    void unaCredencialDistintaNoRetiraUnVinculoRenovado() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrar.ejecutar(new RegistrarDispositivoPushCommand(instalacion, usuario, PRUEBA,
                        "ANDROID", false, null, CREDENCIAL_2)));

        assertEquals("VINCULO_PUSH_EN_USO", ex.code());
        assertEquals(CREDENCIAL_1, credencialActiva(instalacion));
    }

    @Test
    void laBajaEnLineaRevocaSoloElVinculoPropioYEsIdempotente() {
        UUID instalacion = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));

        retirar.ejecutar(new RetirarDispositivoPushCommand(instalacion, usuarioB, PRUEBA, CREDENCIAL_1));
        assertEquals(CREDENCIAL_1, credencialActiva(instalacion));

        retirar.ejecutar(new RetirarDispositivoPushCommand(instalacion, usuarioA, PRUEBA, CREDENCIAL_1));
        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());

        retirar.ejecutar(new RetirarDispositivoPushCommand(instalacion, usuarioA, PRUEBA, CREDENCIAL_1));
        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());
    }

    @Test
    void laBajaConPruebaIncorrectaNoModificaElVinculo() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        retirar.ejecutar(new RetirarDispositivoPushCommand(instalacion, usuario, OTRA_PRUEBA, CREDENCIAL_1));

        assertEquals(CREDENCIAL_1, credencialActiva(instalacion));
    }

    @Test
    void unaBajaAtrasadaDeLaCuentaAnteriorNoAfectaAlVinculoNuevo() {
        UUID instalacion = UUID.randomUUID();
        UUID usuarioA = UUID.randomUUID();
        UUID usuarioB = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuarioA, PRUEBA, TOKEN_1, CREDENCIAL_1));
        registrar.ejecutar(alta(instalacion, usuarioB, PRUEBA, TOKEN_1, CREDENCIAL_2));

        retirar.ejecutar(new RetirarDispositivoPushCommand(instalacion, usuarioA, PRUEBA, CREDENCIAL_1));

        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        assertEquals(usuarioB, activo.usuarioId());
        assertEquals(CREDENCIAL_2, credencialActiva(instalacion));
    }

    @Test
    void laBajaDiferidaRevocaSinSesionYEsSeguraAlRepetir() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();
        registrar.ejecutar(alta(instalacion, usuario, PRUEBA, TOKEN_1, CREDENCIAL_1));

        revocar.ejecutar(new RevocarVinculoPushDiferidoCommand(instalacion, CREDENCIAL_1));
        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());

        revocar.ejecutar(new RevocarVinculoPushDiferidoCommand(instalacion, CREDENCIAL_1));
        revocar.ejecutar(new RevocarVinculoPushDiferidoCommand(instalacion, CREDENCIAL_2));
        assertTrue(repositorio.vinculoActivo(instalacion).isEmpty());
    }

    @Test
    void elMapperRechazaDatosInvalidosSinDistinguirElMotivo() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();

        assertCodigo("DATOS_INVALIDOS", () -> NotificacionPresentationMapper.aCommand(instalacion, usuario,
                "prueba-invalida", new NotificacionDtos.DispositivoPushRequest("ANDROID", true, TOKEN_1, CREDENCIAL_1)));
        assertCodigo("DATOS_INVALIDOS", () -> NotificacionPresentationMapper.aCommand(instalacion, usuario,
                PRUEBA, new NotificacionDtos.DispositivoPushRequest("IOS", true, TOKEN_1, CREDENCIAL_1)));
        assertCodigo("DATOS_INVALIDOS", () -> NotificacionPresentationMapper.aCommand(instalacion, usuario,
                PRUEBA, new NotificacionDtos.DispositivoPushRequest("ANDROID", true, null, CREDENCIAL_1)));
        assertCodigo("DATOS_INVALIDOS", () -> NotificacionPresentationMapper.aCommand(instalacion, usuario,
                PRUEBA, new NotificacionDtos.DispositivoPushRequest("ANDROID", true, TOKEN_1, "cred-invalida")));
        assertCodigo("DATOS_INVALIDOS", () -> NotificacionPresentationMapper.aCommand(instalacion, usuario,
                PRUEBA, new NotificacionDtos.DispositivoPushRequest("ANDROID", null, TOKEN_1, CREDENCIAL_1)));
    }

    @Test
    void elMapperAceptaLaSincronizacionValidaYElPermisoApagadoSinToken() {
        UUID instalacion = UUID.randomUUID();
        UUID usuario = UUID.randomUUID();

        var alta = NotificacionPresentationMapper.aCommand(instalacion, usuario, PRUEBA,
                new NotificacionDtos.DispositivoPushRequest("ANDROID", true, TOKEN_1, CREDENCIAL_1));
        assertEquals(TOKEN_1, alta.tokenFcm());
        assertEquals(CREDENCIAL_1, alta.credencialRevocacion());

        var baja = NotificacionPresentationMapper.aCommand(instalacion, usuario, PRUEBA,
                new NotificacionDtos.DispositivoPushRequest("ANDROID", false, null, null));
        assertFalse(baja.permisoConcedido());
        assertNull(baja.tokenFcm());
        assertNull(baja.credencialRevocacion());
    }

    private RegistrarDispositivoPushCommand alta(UUID instalacion, UUID usuario, String prueba, String token,
                                                 String credencial) {
        return new RegistrarDispositivoPushCommand(instalacion, usuario, prueba, "ANDROID", true, token, credencial);
    }

    private String credencialActiva(UUID instalacion) {
        VinculoPush activo = repositorio.vinculoActivo(instalacion).orElseThrow();
        for (String candidata : List.of(CREDENCIAL_1, CREDENCIAL_2, CREDENCIAL_3)) {
            if (activo.coincideCredencial(HuellaPush.de(candidata).valor())) {
                return candidata;
            }
        }
        return null;
    }

    private void assertCodigo(String esperado, org.junit.jupiter.api.function.Executable ejecutable) {
        BusinessException ex = assertThrows(BusinessException.class, ejecutable);
        assertEquals(esperado, ex.code());
        assertEquals(400, ex.status());
    }

    /** Doble en memoria que reproduce las restricciones de unicidad de V14. */
    private static final class FakeDispositivoPushRepository implements DispositivoPushRepository {
        private final Map<UUID, InstalacionPush> instalaciones = new LinkedHashMap<>();
        private final Map<UUID, List<VinculoPush>> vinculos = new LinkedHashMap<>();

        @Override
        public void bloquearRegistro() {
            // Sin concurrencia real en el doble.
        }

        @Override
        public void asegurarInstalacion(InstalacionPush instalacion) {
            instalaciones.putIfAbsent(instalacion.id(), instalacion);
        }

        @Override
        public Optional<InstalacionPush> buscarInstalacion(UUID instalacionId) {
            return Optional.ofNullable(instalaciones.get(instalacionId));
        }

        @Override
        public Optional<InstalacionPush> bloquearInstalacion(UUID instalacionId) {
            return buscarInstalacion(instalacionId);
        }

        @Override
        public Optional<VinculoPush> vinculoActivo(UUID instalacionId) {
            return vinculos(instalacionId).stream().filter(VinculoPush::activo).findFirst();
        }

        @Override
        public List<VinculoPush> vinculosActivosDe(UUID usuarioId) {
            return vinculos.values().stream().flatMap(List::stream)
                    .filter(VinculoPush::activo)
                    .filter(v -> v.usuarioId().equals(usuarioId))
                    .filter(v -> v.tokenFcm() != null && !v.tokenFcm().isBlank())
                    .toList();
        }

        @Override
        public boolean existeCredencial(byte[] huellaCredencial) {
            return vinculos.values().stream().flatMap(List::stream)
                    .anyMatch(v -> Arrays.equals(v.revocacionSha256(), huellaCredencial));
        }

        @Override
        public Optional<UUID> instalacionConTokenActivo(byte[] huellaToken) {
            return vinculos.entrySet().stream()
                    .filter(e -> e.getValue().stream()
                            .anyMatch(v -> v.activo() && v.usaToken(huellaToken)))
                    .map(Map.Entry::getKey).findFirst();
        }

        @Override
        public long siguienteGeneracion(UUID instalacionId) {
            return vinculos(instalacionId).stream().mapToLong(VinculoPush::generacion).max().orElse(0L) + 1L;
        }

        @Override
        public void guardarVinculo(VinculoPush vinculo) {
            if (vinculoActivo(vinculo.instalacionId()).isPresent()) {
                throw new IllegalStateException("uq_vinculo_push_instalacion_activa");
            }
            if (existeCredencial(vinculo.revocacionSha256())) {
                throw new IllegalStateException("uq_vinculo_push_revocacion");
            }
            if (vinculo.tokenFcmSha256() != null && instalacionConTokenActivo(vinculo.tokenFcmSha256()).isPresent()) {
                throw new IllegalStateException("uq_vinculo_push_token_activo");
            }
            vinculos.computeIfAbsent(vinculo.instalacionId(), id -> new ArrayList<>()).add(vinculo);
        }

        @Override
        public boolean revocarVinculo(UUID instalacionId, long generacion, OffsetDateTime momento) {
            return revocar(instalacionId, v -> v.generacion() == generacion, momento);
        }

        @Override
        public boolean revocarVinculoPropio(UUID instalacionId, UUID usuarioId, byte[] huellaCredencial,
                                            OffsetDateTime momento) {
            return revocar(instalacionId, v -> v.esDe(usuarioId)
                    && Arrays.equals(v.revocacionSha256(), huellaCredencial), momento);
        }

        @Override
        public boolean revocarVinculoPorCredencial(UUID instalacionId, byte[] huellaCredencial,
                                                   OffsetDateTime momento) {
            return revocar(instalacionId, v -> Arrays.equals(v.revocacionSha256(), huellaCredencial), momento);
        }

        List<VinculoPush> vinculos(UUID instalacionId) {
            return vinculos.getOrDefault(instalacionId, List.of());
        }

        private boolean revocar(UUID instalacionId, java.util.function.Predicate<VinculoPush> condicion,
                                OffsetDateTime momento) {
            List<VinculoPush> lista = vinculos.getOrDefault(instalacionId, List.of());
            for (int i = 0; i < lista.size(); i++) {
                VinculoPush actual = lista.get(i);
                if (actual.activo() && condicion.test(actual)) {
                    lista.set(i, new VinculoPush(actual.instalacionId(), actual.generacion(),
                            actual.usuarioId(), null, null, actual.revocacionSha256(),
                            actual.vinculadoEn(), momento));
                    return true;
                }
            }
            return false;
        }
    }
}

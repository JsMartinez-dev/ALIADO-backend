package com.sebastian.spring.mas.aliado.aliado.modules.notificaciones;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarAvisoLeidoCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.dto.MarcarTodasLeidasCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.MarcarAvisoLeidoService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.application.service.MarcarTodasLeidasService;
import com.sebastian.spring.mas.aliado.aliado.modules.notificaciones.domain.repository.NotificacionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F09-BANDEJA-02: los dos casos de uso delegan la escritura en el puerto con la hora del
 * servidor, sin consultar la existencia global. Un aviso ajeno y uno inexistente producen
 * el mismo recurso no encontrado porque ambos afectan cero filas.
 */
class MarcarLecturaAvisosUseCasesTest {

    private static final UUID AVISO = UUID.randomUUID();
    private static final UUID CUENTA = UUID.randomUUID();
    private static final Instant AHORA = Instant.parse("2026-03-01T10:00:00Z");
    private static final OffsetDateTime ESPERADA = OffsetDateTime.ofInstant(AHORA, ZoneOffset.UTC);

    private final NotificacionRepository notificaciones = mock(NotificacionRepository.class);

    @Test
    void laLecturaIndividualUsaLaHoraDelRelojYNoConsultaLaExistenciaGlobal() {
        when(notificaciones.marcarLeida(AVISO, CUENTA, ESPERADA)).thenReturn(true);

        servicioIndividual().ejecutar(new MarcarAvisoLeidoCommand(AVISO, CUENTA));

        verify(notificaciones).marcarLeida(AVISO, CUENTA, ESPERADA);
        verify(notificaciones, never()).porId(any());
    }

    @Test
    void unAvisoPropioYaLeidoTambienEsUnExito() {
        // La sentencia cuenta la fila propia aunque COALESCE conserve la fecha anterior.
        when(notificaciones.marcarLeida(AVISO, CUENTA, ESPERADA)).thenReturn(true);

        assertDoesNotThrow(() -> servicioIndividual().ejecutar(
                new MarcarAvisoLeidoCommand(AVISO, CUENTA)));
    }

    @Test
    void avisoAjenoEInexistenteProducenElMismoRecursoNoEncontrado() {
        // Cero filas no distingue un aviso ajeno de uno inexistente.
        when(notificaciones.marcarLeida(any(), any(), any())).thenReturn(false);

        BusinessException ajeno = assertThrows(BusinessException.class,
                () -> servicioIndividual().ejecutar(new MarcarAvisoLeidoCommand(AVISO, CUENTA)));
        BusinessException inexistente = assertThrows(BusinessException.class,
                () -> servicioIndividual().ejecutar(new MarcarAvisoLeidoCommand(UUID.randomUUID(), CUENTA)));

        assertEquals("NOTIFICACION_NO_ENCONTRADA", ajeno.code());
        assertEquals(404, ajeno.status());
        assertEquals(ajeno.code(), inexistente.code());
        assertEquals(ajeno.status(), inexistente.status());
        assertEquals(ajeno.getMessage(), inexistente.getMessage());
        verify(notificaciones, never()).porId(any());
    }

    @Test
    void laLecturaMasivaUsaElDestinatarioYLaHoraSinConsultarAvisos() {
        when(notificaciones.marcarTodasLeidas(CUENTA, ESPERADA)).thenReturn(0);

        assertDoesNotThrow(() -> servicioMasivo().ejecutar(new MarcarTodasLeidasCommand(CUENTA)));

        verify(notificaciones).marcarTodasLeidas(CUENTA, ESPERADA);
        verify(notificaciones, never()).porId(any());
    }

    private MarcarAvisoLeidoService servicioIndividual() {
        return new MarcarAvisoLeidoService(notificaciones, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private MarcarTodasLeidasService servicioMasivo() {
        return new MarcarTodasLeidasService(notificaciones, Clock.fixed(AHORA, ZoneOffset.UTC));
    }
}

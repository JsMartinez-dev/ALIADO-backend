package com.sebastian.spring.mas.aliado.aliado.modules.confianza;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.RolConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.domain.repository.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.port.ReportePort;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfianzaUseCasesTest {
    private final CalificacionRepository calificaciones = mock(CalificacionRepository.class);
    private final ResenaRepository resenas = mock(ResenaRepository.class);
    private final ReputacionRepository reputaciones = mock(ReputacionRepository.class);
    private final SolicitudVerificacionRepository verificaciones = mock(SolicitudVerificacionRepository.class);
    private final ServicioLecturaPort servicios = mock(ServicioLecturaPort.class);
    private final ReportePort reportes = mock(ReportePort.class);
    private final RolConsultaPort roles = mock(RolConsultaPort.class);
    private final OperacionesConfianza operaciones = new OperacionesConfianza(calificaciones, resenas, reputaciones,
            verificaciones, servicios, reportes, roles);

    @Test void valorFueraDeRangoNoConsultaNiPersiste() {
        var service = new CalificarServicioService(operaciones, new RecalcularReputacionService(operaciones));
        assertThrows(BusinessException.BadRequest.class,
                () -> service.ejecutar(new CalificarServicioCommand(UUID.randomUUID(), UUID.randomUUID(), 6)));
        verifyNoInteractions(servicios, calificaciones, reputaciones);
    }

    @Test void resenaExigeCalificacionPrevia() {
        UUID id = UUID.randomUUID();
        when(calificaciones.porId(id)).thenReturn(Optional.empty());
        assertThrows(BusinessException.NotFound.class,
                () -> new AgregarResenaService(operaciones).ejecutar(new AgregarResenaCommand(id, UUID.randomUUID(), "bien")));
        verifyNoInteractions(resenas);
    }

    @Test void resenaAjenaNoSeGuarda() {
        UUID id = UUID.randomUUID();
        when(calificaciones.porId(id)).thenReturn(Optional.of(new Calificacion(id, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), 5, OffsetDateTime.now())));
        assertThrows(BusinessException.Forbidden.class,
                () -> new AgregarResenaService(operaciones).ejecutar(new AgregarResenaCommand(id, UUID.randomUUID(), "bien")));
        verify(resenas, never()).guardar(any());
    }

    @Test void reputacionIncluyeTodasLasCalificaciones() {
        UUID prestador = UUID.randomUUID();
        when(calificaciones.porPrestadorId(prestador)).thenReturn(List.of(
                new Calificacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), prestador, 4, OffsetDateTime.now()),
                new Calificacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), prestador, 5, OffsetDateTime.now())));
        new RecalcularReputacionService(operaciones).ejecutar(new RecalcularReputacionCommand(prestador));
        verify(reputaciones).guardar(argThat(r -> r.cantidadCalificaciones() == 2
                && r.valorAgregado().compareTo(new BigDecimal("4.50")) == 0));
    }

    @Test void consultaSinPrestadorDevuelveNoEncontrado() {
        assertThrows(BusinessException.NotFound.class,
                () -> new ConsultarConfianzaService(operaciones).ejecutar(new ConsultarConfianzaCommand(UUID.randomUUID())));
    }

    @Test void resenaOcultaNoGeneraReporte() {
        UUID id = UUID.randomUUID();
        when(resenas.porId(id)).thenReturn(Optional.of(new Resena(id, UUID.randomUUID(), "texto",
                OffsetDateTime.now(), EstadoVisibilidadResena.OCULTA)));
        assertThrows(BusinessException.Conflict.class,
                () -> new ReportarResenaService(operaciones).ejecutar(new ReportarResenaCommand(id, UUID.randomUUID(), "motivo")));
        verifyNoInteractions(reportes);
    }
}

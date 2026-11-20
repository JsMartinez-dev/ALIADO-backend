package com.sebastian.spring.mas.aliado.aliado.modules.reportes;

import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.common.storage.ArchivoStorageService;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.UsuarioConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.confianza.application.port.ResenaConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.reportes.domain.repository.*;
import com.sebastian.spring.mas.aliado.aliado.modules.servicios.application.port.ServicioLecturaPort;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteUseCasesTest {
    private final ReporteRepository reportes = mock(ReporteRepository.class);
    private final EvidenciaReporteRepository evidencias = mock(EvidenciaReporteRepository.class);
    private final UsuarioConsultaPort usuarios = mock(UsuarioConsultaPort.class);
    private final ArchivoStorageService storage = mock(ArchivoStorageService.class);
    private final ServicioLecturaPort servicios = mock(ServicioLecturaPort.class);
    private final OperacionesReportes operaciones = new OperacionesReportes(reportes, evidencias,
            mock(ResenaConsultaPort.class), usuarios, servicios, storage);

    @Test void noPermiteAutoReporte() {
        UUID actor = UUID.randomUUID();
        when(usuarios.existe(actor)).thenReturn(true);
        assertThrows(BusinessException.BadRequest.class,
                () -> new ReportarUsuarioService(operaciones).ejecutar(new ReportarUsuarioCommand(actor, actor, "motivo")));
        verify(reportes, never()).guardar(any());
    }

    @Test void usuarioInexistenteNoGeneraReporte() {
        assertThrows(BusinessException.NotFound.class,
                () -> new ReportarUsuarioService(operaciones).ejecutar(new ReportarUsuarioCommand(UUID.randomUUID(), UUID.randomUUID(), "motivo")));
        verify(reportes, never()).guardar(any());
    }

    @Test void servicioInexistenteNoGeneraReporte() {
        UUID servicio = UUID.randomUUID();
        when(servicios.porId(servicio)).thenReturn(Optional.empty());
        assertThrows(BusinessException.NotFound.class,
                () -> new ReportarServicioService(operaciones).ejecutar(new ReportarServicioCommand(servicio, UUID.randomUUID(), "motivo")));
        verify(reportes, never()).guardar(any());
    }

    @Test void evidenciaAjenaNoEscribeArchivo() {
        UUID id = UUID.randomUUID();
        when(reportes.porIdParaActualizar(id)).thenReturn(Optional.of(new Reporte(id, TipoReporte.USUARIO, UUID.randomUUID(),
                null, UUID.randomUUID(), null, "motivo", EstadoReporte.PENDIENTE,
                null, null, null, OffsetDateTime.now(), null, 0)));
        assertThrows(BusinessException.Forbidden.class,
                () -> new AdjuntarEvidenciaReporteService(operaciones).ejecutar(new AdjuntarEvidenciaReporteCommand(
                        id, UUID.randomUUID(), "foto.png", "image/png", new byte[]{1})));
        verifyNoInteractions(storage, evidencias);
    }
}

package com.sebastian.spring.mas.aliado.aliado.modules.administracion;

import com.sebastian.spring.mas.aliado.aliado.common.auditoria.AuditoriaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.exception.AdministracionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.port.ConsultaEliminacionCuentaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.model.BloqueoPreventivo;
import com.sebastian.spring.mas.aliado.aliado.modules.administracion.domain.repository.BloqueoRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.auth.application.port.*;
import com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.port.PerfilBusquedaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.application.port.SolicitudConsultaPort;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdministracionUseCasesTest {
    @Test
    void levantarBloqueoMantieneCuentaEliminadaDesactivada() {
        verificarLevantamiento(true);
    }

    @Test
    void levantarBloqueoReactivaCuentaSinEliminacionConfirmada() {
        verificarLevantamiento(false);
    }

    private void verificarLevantamiento(boolean eliminada) {
        UUID usuario = UUID.randomUUID();
        UUID administrador = UUID.randomUUID();
        UUID bloqueoId = UUID.randomUUID();
        BloqueoRepository bloqueos = mock(BloqueoRepository.class);
        CuentaBloqueoPort cuentas = mock(CuentaBloqueoPort.class);
        AuditoriaPort auditoria = mock(AuditoriaPort.class);
        ConsultaEliminacionCuentaPort eliminaciones = mock(ConsultaEliminacionCuentaPort.class);
        when(eliminaciones.tieneEliminacionConfirmada(usuario)).thenReturn(eliminada);
        when(bloqueos.porId(bloqueoId)).thenReturn(Optional.of(new BloqueoPreventivo(
                bloqueoId, usuario, administrador, "Investigación", OffsetDateTime.now(), null, true)));
        var operaciones = new BloqueoCuentaService(bloqueos, eliminaciones,
                mock(UsuarioConsultaPort.class), cuentas, auditoria);
        new LevantarBloqueoService(operaciones).ejecutar(new LevantarBloqueoCommand(usuario, bloqueoId, administrador));
        verify(bloqueos).guardar(argThat(b -> !b.activo() && b.fechaFin() != null));
        verify(auditoria).registrar(administrador, "ACCOUNT_UNBLOCKED", "USUARIO", usuario, null);
        if (eliminada) {
            verify(cuentas, never()).desbloquearCuenta(any());
        } else {
            verify(cuentas).desbloquearCuenta(usuario);
        }
    }

    @Test
    void bloqueoRechazaMotivoVacioSinCambiarCuenta() {
        UUID usuario = UUID.randomUUID();
        UsuarioConsultaPort usuarios = mock(UsuarioConsultaPort.class);
        when(usuarios.existe(usuario)).thenReturn(true);
        CuentaBloqueoPort cuentas = mock(CuentaBloqueoPort.class);
        var operaciones = new BloqueoCuentaService(mock(BloqueoRepository.class),
                mock(ConsultaEliminacionCuentaPort.class), usuarios, cuentas, mock(AuditoriaPort.class));
        assertThrows(AdministracionOperacionException.class, () -> new BloquearCuentaService(operaciones)
                .ejecutar(new BloquearCuentaCommand(usuario, UUID.randomUUID(), "  ")));
        verifyNoInteractions(cuentas);
    }

    @Test
    void busquedaRechazaTextoVacioAntesDeConsultarFuentes() {
        UsuarioConsultaPort usuarios = mock(UsuarioConsultaPort.class);
        var operaciones = new BusquedaAdminService(usuarios, mock(PerfilBusquedaPort.class),
                mock(SolicitudConsultaPort.class));
        var error = assertThrows(AdministracionOperacionException.class,
                () -> new BuscarCuentasPerfilesService(operaciones).ejecutar(new BuscarCuentasPerfilesCommand(" ")));
        assertEquals("BUSQUEDA_REQUERIDA", error.codigo());
        verifyNoInteractions(usuarios);
    }
}

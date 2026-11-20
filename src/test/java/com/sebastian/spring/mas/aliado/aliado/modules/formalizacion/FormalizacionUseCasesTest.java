package com.sebastian.spring.mas.aliado.aliado.modules.formalizacion;

import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.FormalizacionService;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.dto.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.exception.FormalizacionOperacionException;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.application.service.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.model.*;
import com.sebastian.spring.mas.aliado.aliado.modules.formalizacion.domain.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FormalizacionUseCasesTest {
    private final RutaRepository rutas = mock(RutaRepository.class);
    private final PasoRepository pasos = mock(PasoRepository.class);
    private final EnlaceRepository enlaces = mock(EnlaceRepository.class);
    private final ProgresoRepository progresos = mock(ProgresoRepository.class);
    private final FormalizacionService operaciones = new FormalizacionService(rutas, pasos, enlaces, progresos);

    @Test
    void progresoAlCienConservaAdvertenciaOrientativa() {
        UUID ruta = UUID.randomUUID();
        when(rutas.porId(ruta)).thenReturn(Optional.of(new Ruta(ruta, "Ruta", "Orientativa", true)));
        when(progresos.guardar(any())).thenAnswer(i -> i.getArgument(0));
        var result = new ActualizarProgresoFormalizacionService(operaciones)
                .ejecutar(new ActualizarProgresoFormalizacionCommand(UUID.randomUUID(), ruta, 100));
        assertEquals(100, result.valorOrientativo());
        assertEquals(FormalizacionService.AVISO_LEGAL, result.avisoLegal());
    }

    @Test
    void progresoFueraDeRangoNoSeGuarda() {
        assertThrows(FormalizacionOperacionException.class, () -> new ActualizarProgresoFormalizacionService(operaciones)
                .ejecutar(new ActualizarProgresoFormalizacionCommand(UUID.randomUUID(), UUID.randomUUID(), 101)));
        verifyNoInteractions(progresos);
    }

    @Test
    void enlaceRechazaEsquemaEjecutableAntesDeGuardar() {
        UUID id = UUID.randomUUID();
        when(enlaces.porId(id)).thenReturn(Optional.of(new Enlace(id, UUID.randomUUID(), "Oficial",
                "https://example.org", EstadoEnlace.DISPONIBLE)));
        var error = assertThrows(FormalizacionOperacionException.class, () -> new ActualizarEnlaceInstitucionalService(operaciones)
                .ejecutar(new ActualizarEnlaceInstitucionalCommand(id, null, "javascript:alert(1)", null)));
        assertEquals("URL_INVALIDA", error.codigo());
        verify(enlaces, never()).guardar(any());
    }

    @Test
    void vinculacionFutureApagadaRechazaAcceso() {
        var error = assertThrows(FormalizacionOperacionException.class,
                () -> new ConsultarVinculacionInstitucionalService(() -> false).ejecutar());
        assertEquals("FUNCIONALIDAD_DESHABILITADA", error.codigo());
    }

    @Test
    void vinculacionFutureEncendidaNoFingeImplementacion() {
        var error = assertThrows(FormalizacionOperacionException.class,
                () -> new ConsultarVinculacionInstitucionalService(() -> true).ejecutar());
        assertEquals(FormalizacionOperacionException.Tipo.NO_IMPLEMENTADO, error.tipo());
    }
}

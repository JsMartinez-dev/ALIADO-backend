package com.sebastian.spring.mas.aliado.aliado.modules.corrections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sebastian.spring.mas.aliado.aliado.common.exception.BusinessException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.CrearPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.EditarPerfilProfesionalCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaCatalogoPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.CrearPerfilProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service.EditarPerfilProfesionalService;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.DatosGeneralesProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.EstadoExposicion;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.model.PerfilProfesional;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.domain.repository.PerfilProfesionalRepository;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.presentation.dto.DatosGeneralesDtos;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatosGeneralesBe05Test {
    @Mock PerfilProfesionalRepository perfiles;
    @Mock ConsultaCatalogoPerfilPort catalogo;

    @Test
    void creaPerfilConDescripcionNormalizadaYDescartaClavesDesconocidas() {
        UUID prestadorId = UUID.randomUUID();
        when(perfiles.porPrestadorId(prestadorId)).thenReturn(Optional.empty());
        when(catalogo.categoriaActiva(any())).thenReturn(true);
        when(catalogo.zonaActiva(any())).thenReturn(true);
        when(perfiles.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = new CrearPerfilProfesionalService(perfiles, catalogo).ejecutar(
                new CrearPerfilProfesionalCommand(prestadorId, "  Presentación profesional  ",
                        List.of(UUID.randomUUID()), List.of(UUID.randomUUID()), List.of()));

        assertEquals(Map.of("descripcion", "Presentación profesional"), resultado.datosGenerales());
    }

    @Test
    void editaDescripcionSinPerderClavesHeredadas() {
        UUID prestadorId = UUID.randomUUID();
        var existente = perfil(prestadorId, Map.of(
                "descripcion", "Anterior",
                "resumen", "Clave heredada",
                "telefono", "3000000000"));
        when(perfiles.porPrestadorId(prestadorId)).thenReturn(Optional.of(existente));
        when(perfiles.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = new EditarPerfilProfesionalService(perfiles).ejecutar(
                new EditarPerfilProfesionalCommand(prestadorId, "Nueva presentación"));

        assertEquals("Nueva presentación", resultado.datosGenerales().get("descripcion"));
        assertEquals("Clave heredada", resultado.datosGenerales().get("resumen"));
        assertEquals("3000000000", resultado.datosGenerales().get("telefono"));
    }

    @Test
    void descripcionVaciaSeEliminaYConservaClavesHeredadas() {
        UUID prestadorId = UUID.randomUUID();
        var existente = perfil(prestadorId, Map.of("descripcion", "Anterior", "resumen", "Heredada"));
        when(perfiles.porPrestadorId(prestadorId)).thenReturn(Optional.of(existente));
        when(perfiles.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = new EditarPerfilProfesionalService(perfiles).ejecutar(
                new EditarPerfilProfesionalCommand(prestadorId, "   "));

        assertEquals(Map.of("resumen", "Heredada"), resultado.datosGenerales());
    }

    @Test
    void descripcionDemasiadoLargaEs400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> DatosGeneralesProfesional.normalizarDescripcion("a".repeat(1001)));
        assertEquals(400, ex.status());
        assertEquals("DATOS_GENERALES_INVALIDOS", ex.code());
    }

    @Test
    void jsonDesconocidoSeIgnoraYNoSeConvierteEnContrato() throws Exception {
        var mapper = new ObjectMapper();
        var request = mapper.readValue(
                "{\"descripcion\":\"Texto\",\"anosExperiencia\":12,\"telefono\":\"300\",\"correo\":\"a@b.co\"}",
                DatosGeneralesDtos.DatosGeneralesRequest.class);

        assertEquals("Texto", request.descripcion());
    }

    @Test
    void respuestaSoloExponeDescripcion() {
        var respuesta = DatosGeneralesDtos.DatosGeneralesResponse.desde(Map.of(
                "descripcion", "Texto público",
                "telefono", "300",
                "correo", "a@b.co"));

        assertEquals("Texto público", respuesta.descripcion());
    }

    private PerfilProfesional perfil(UUID prestadorId, Map<String, Object> datos) {
        var ahora = OffsetDateTime.now();
        return new PerfilProfesional(UUID.randomUUID(), prestadorId, datos, EstadoExposicion.PUBLICO,
                ahora, ahora, Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of());
    }
}

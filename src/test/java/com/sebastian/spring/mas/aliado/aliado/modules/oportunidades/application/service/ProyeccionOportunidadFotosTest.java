package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;

import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.EstadoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.FotoSolicitud;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProyeccionOportunidadFotosTest {
    @Test
    void incluyeIdentificadoresYOrdenSinExponerLaClaveInterna() {
        var consultas = mock(ConsultaOportunidadesPort.class);
        var categoriaId = UUID.randomUUID();
        var zonaId = UUID.randomUUID();
        var solicitudId = UUID.randomUUID();
        var fotoId = UUID.randomUUID();
        var ahora = OffsetDateTime.now();
        var solicitud = new Solicitud(solicitudId, UUID.randomUUID(), categoriaId, zonaId, "Fuga de agua",
                null, EstadoSolicitud.PUBLICADA, ahora, ahora, 0,
                List.of(new FotoSolicitud(fotoId, solicitudId, "fotos/privada/abc.jpg", 2)), List.of());
        when(consultas.categoriaPorId(categoriaId)).thenReturn(Optional.empty());
        when(consultas.zonaPorId(zonaId)).thenReturn(Optional.empty());

        var result = new ProyeccionOportunidad(consultas).proyectar(solicitud);

        assertEquals(1, result.fotosDetalle().size());
        assertEquals(fotoId, result.fotosDetalle().get(0).id());
        assertEquals(2, result.fotosDetalle().get(0).orden());
    }
}

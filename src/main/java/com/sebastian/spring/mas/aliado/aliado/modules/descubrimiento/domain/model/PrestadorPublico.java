package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.domain.model;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PrestadorPublico(UUID id, List<String> categorias, List<String> zonas,
                               BigDecimal reputacionAgregada, int cantidadCalificaciones,
                               String nivelVerificacion, List<Bloque> disponibilidad,
                               Map<String, Object> datosGenerales, List<ResenaPublica> resenas) {
    public record Bloque(Integer diaSemana, LocalTime horaInicio, LocalTime horaFin) {}
    public record ResenaPublica(UUID id, int valor, String texto, OffsetDateTime fechaCreacion) {}
}

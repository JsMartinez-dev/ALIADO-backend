package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public record VistaPreviaPerfilResult(UUID prestadorId, List<String> categorias, List<String> zonas,
 BigDecimal reputacion, int cantidadCalificaciones, String nivelVerificacion,
 List<PerfilProfesionalResult.DisponibilidadResult> disponibilidad, Map<String,Object> datosGenerales,
 List<ResenaResult> resenas) {
 public record ResenaResult(UUID id,int valor,String texto,OffsetDateTime fechaCreacion) {}
}

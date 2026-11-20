package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.mapper;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarTableroQuery;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.presentation.dto.OportunidadDtos;
import java.util.UUID;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.ConsultarDetalleOportunidadQuery;
public final class OportunidadPresentationMapper {
 private OportunidadPresentationMapper() {}
 public static ConsultarDetalleOportunidadQuery aDetalleQuery(UUID solicitudId, UUID prestadorId) {
  return new ConsultarDetalleOportunidadQuery(solicitudId,prestadorId);
 }
 public static ConsultarTableroQuery aQuery(UUID prestadorId,String categoria,String urgencia) {
  return new ConsultarTableroQuery(prestadorId,categoria,urgencia);
 }
 public static OportunidadDtos.OportunidadResponse aResponse(OportunidadResult result) {
  return new OportunidadDtos.OportunidadResponse(result.solicitudId(),result.categoria(),result.zonaAproximada(),
    result.urgencia(),result.descripcion(),result.fechaCreacion(),result.fotos());
 }
}

package com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.dto.OportunidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.oportunidades.application.port.ConsultaOportunidadesPort;
import com.sebastian.spring.mas.aliado.aliado.modules.solicitudes.domain.model.Solicitud;
public class ProyeccionOportunidad {
 private final ConsultaOportunidadesPort consultas;
 public ProyeccionOportunidad(ConsultaOportunidadesPort consultas) {this.consultas=consultas;}
 public OportunidadResult proyectar(Solicitud s) {
  return proyectar(s, null);
 }
 public OportunidadResult proyectarDetalle(Solicitud solicitud) {
  return proyectar(solicitud, consultas.nombreVisibleDe(solicitud.clienteId()).orElse(null));
 }
 private OportunidadResult proyectar(Solicitud s, String nombreVisibleCliente) {
  String categoria=consultas.categoriaPorId(s.categoriaId()).map(c->c.nombre()).orElse(s.categoriaId().toString());
  String zona=consultas.zonaPorId(s.zonaId()).map(z->z.nombre()).orElse(s.zonaId().toString());
  return new OportunidadResult(s.id(),categoria,zona,s.urgencia()==null?null:s.urgencia().name(),
    s.descripcion(),s.fechaCreacion(),
    s.fotos().stream().map(f -> new OportunidadResult.FotoDetalleResult(f.id(), f.orden())).toList(),
    nombreVisibleCliente);
 }
}

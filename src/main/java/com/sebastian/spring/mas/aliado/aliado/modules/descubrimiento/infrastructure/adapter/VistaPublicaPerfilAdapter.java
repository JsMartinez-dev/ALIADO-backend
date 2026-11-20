package com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.infrastructure.adapter;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.usecase.ConsultarPerfilPublicoUseCase;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.port.ConsultaDescubrimientoPort;
import com.sebastian.spring.mas.aliado.aliado.modules.descubrimiento.application.exception.PerfilPublicoNoDisponibleException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.VistaPreviaPerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.DisponibilidadResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaVistaPublicaPerfilPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reutiliza la política y proyección de descubrimiento sin dependencia inversa de perfiles. */
@Component
public class VistaPublicaPerfilAdapter implements ConsultaVistaPublicaPerfilPort {
 private final ConsultarPerfilPublicoUseCase publico;
 private final ConsultaDescubrimientoPort consultas;
 public VistaPublicaPerfilAdapter(ConsultarPerfilPublicoUseCase publico, ConsultaDescubrimientoPort consultas) {
  this.publico=publico;this.consultas=consultas;
 }
 @Override public VistaPreviaPerfilResult consultar(UUID prestadorId) {
  var vista=publico.ejecutar(prestadorId);
  var perfil=consultas.porPrestadorId(prestadorId).orElseThrow(PerfilPublicoNoDisponibleException::new);
  return new VistaPreviaPerfilResult(prestadorId,vista.categorias(),vista.zonas(),vista.reputacionAgregada(),
    vista.cantidadCalificaciones(),vista.nivelVerificacion(),perfil.disponibilidades().stream().map(d -> new DisponibilidadResult(
      d.id(),d.diaSemana(),d.horaInicio(),d.horaFin())).toList(),vista.datosGenerales(),vista.resenas().stream()
      .map(r -> new VistaPreviaPerfilResult.ResenaResult(r.id(),r.valor(),r.texto(),r.fechaCreacion())).toList());
 }
}

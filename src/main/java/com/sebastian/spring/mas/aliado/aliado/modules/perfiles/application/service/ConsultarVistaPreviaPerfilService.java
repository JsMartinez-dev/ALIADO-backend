package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.VistaPreviaPerfilResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.port.ConsultaVistaPublicaPerfilPort;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ConsultarVistaPreviaPerfilUseCase;
import java.util.UUID;
public class ConsultarVistaPreviaPerfilService implements ConsultarVistaPreviaPerfilUseCase {
 private final ConsultaVistaPublicaPerfilPort consulta;
 public ConsultarVistaPreviaPerfilService(ConsultaVistaPublicaPerfilPort consulta) {this.consulta=consulta;}
 @Override public VistaPreviaPerfilResult ejecutar(UUID prestadorId) {return consulta.consultar(prestadorId);}
}

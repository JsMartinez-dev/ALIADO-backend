package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.GuardarCompromisoAgendaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.CompromisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.DatosPerfilProfesionalInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.ActualizarCompromisoAgendaUseCase;
public class ActualizarCompromisoAgendaService implements ActualizarCompromisoAgendaUseCase {
 private final GuardadoCompromisoAgenda guardado;
 public ActualizarCompromisoAgendaService(GuardadoCompromisoAgenda guardado) {this.guardado=guardado;}
 @Override public CompromisoResult ejecutar(GuardarCompromisoAgendaCommand command) {
 if(command.compromisoId()==null) {throw new DatosPerfilProfesionalInvalidosException("COMPROMISO_INVALIDO", "PUT /mio/agenda exige el id del compromiso; sin id usa POST para crear.");}
 return guardado.guardar(command.prestadorId(), command.compromisoId(), command.inicio(), command.fin());
 }
}

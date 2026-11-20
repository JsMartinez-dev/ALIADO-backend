package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.GuardarCompromisoAgendaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.CompromisoResult;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.DatosPerfilProfesionalInvalidosException;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase.CrearCompromisoAgendaUseCase;
public class CrearCompromisoAgendaService implements CrearCompromisoAgendaUseCase {
 private final GuardadoCompromisoAgenda guardado;
 public CrearCompromisoAgendaService(GuardadoCompromisoAgenda guardado) {this.guardado=guardado;}
 @Override public CompromisoResult ejecutar(GuardarCompromisoAgendaCommand command) {
 
 return guardado.guardar(command.prestadorId(), null, command.inicio(), command.fin());
 }
}

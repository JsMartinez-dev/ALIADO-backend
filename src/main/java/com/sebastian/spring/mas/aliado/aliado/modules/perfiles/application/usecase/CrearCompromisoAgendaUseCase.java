package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.GuardarCompromisoAgendaCommand;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.CompromisoResult;
public interface CrearCompromisoAgendaUseCase {CompromisoResult ejecutar(GuardarCompromisoAgendaCommand command);}

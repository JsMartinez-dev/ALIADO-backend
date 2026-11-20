package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.usecase;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto.PerfilProfesionalResult.CompromisoResult;
import java.util.List;
import java.util.UUID;
public interface ListarCompromisosAgendaUseCase {List<CompromisoResult> ejecutar(UUID prestadorId);}

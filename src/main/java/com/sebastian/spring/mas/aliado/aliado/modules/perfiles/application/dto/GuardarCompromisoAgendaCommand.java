package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.time.OffsetDateTime;
import java.util.UUID;
public record GuardarCompromisoAgendaCommand(UUID prestadorId, UUID compromisoId, OffsetDateTime inicio, OffsetDateTime fin) {}

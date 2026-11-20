package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.dto;
import java.util.UUID;
public record AgregarServicioPortafolioCommand(UUID prestadorId, String descripcion,
        String nombreArchivo, String tipoContenido, byte[] contenido) {}

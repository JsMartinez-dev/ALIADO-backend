package com.sebastian.spring.mas.aliado.aliado.modules.perfil.application.dto;

import java.time.OffsetDateTime;

public record EliminacionResult(String estado, OffsetDateTime expiraEn, long archivosPendientes,
                                java.util.UUID operacionId) {
    public EliminacionResult(String estado, OffsetDateTime expiraEn) {
        this(estado, expiraEn, 0, null);
    }

    public EliminacionResult(String estado, OffsetDateTime expiraEn, long archivosPendientes) {
        this(estado, expiraEn, archivosPendientes, null);
    }

    public EliminacionResult conArchivosPendientes(long pendientes) {
        return new EliminacionResult(estado, expiraEn, pendientes, operacionId);
    }
}

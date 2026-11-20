package com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.service;
import com.sebastian.spring.mas.aliado.aliado.modules.perfiles.application.exception.CatalogoPerfilInvalidoException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/** Validación compartida de las selecciones del perfil. */
public final class CatalogoPerfilValidacion {
    private CatalogoPerfilValidacion() {}
    public static Set<UUID> validarActivos(List<UUID> ids, String que, Predicate<UUID> activo) {
        if (ids == null || ids.isEmpty()) {
            throw new CatalogoPerfilInvalidoException("CATALOGO_REQUERIDO",
                    "El perfil requiere al menos una " + que + ".");
        }
        Set<UUID> validos = new HashSet<>();
        for (UUID id : ids) {
            if (id == null || !activo.test(id)) {
                throw new CatalogoPerfilInvalidoException("CATALOGO_INVALIDO",
                        "Un elemento del catálogo no existe o no está activo: " + id);
            }
            validos.add(id);
        }
        return validos;
    }
}

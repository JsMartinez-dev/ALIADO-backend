package com.sebastian.spring.mas.aliado.aliado.modules.catalogo.presentation.controller;

import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.application.port.CatalogoConsultaPort;
import com.sebastian.spring.mas.aliado.aliado.modules.catalogo.presentation.dto.CatalogoDtos;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {
    private final CatalogoConsultaPort catalogoPort;

    public CatalogoController(CatalogoConsultaPort catalogoPort) {
        this.catalogoPort = catalogoPort;
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CatalogoDtos.CategoriaResponse>> categorias() {
        return ResponseEntity.ok(catalogoPort.categoriasActivas().stream()
                .map(c -> new CatalogoDtos.CategoriaResponse(c.id(), c.nombre()))
                .toList());
    }

    @GetMapping("/zonas")
    public ResponseEntity<List<CatalogoDtos.ZonaResponse>> zonas() {
        return ResponseEntity.ok(catalogoPort.zonasActivas().stream()
                .map(z -> new CatalogoDtos.ZonaResponse(z.id(), z.nombre(), z.coberturaGeografica()))
                .toList());
    }
}

package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlantillaRecuperacionHtmlTest {
    @Test
    void representaCodigoYVigenciaEnUnaPlantillaHtml() {
        String html = PlantillaRecuperacionHtml.renderizar("001234", 30);
        assertTrue(html.contains("001234"));
        assertTrue(html.contains("30 minutos"));
        assertTrue(html.contains("<html lang=\"es\">"));
    }
}

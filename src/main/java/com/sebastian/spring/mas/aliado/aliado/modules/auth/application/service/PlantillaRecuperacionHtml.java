package com.sebastian.spring.mas.aliado.aliado.modules.auth.application.service;

public final class PlantillaRecuperacionHtml {
    private static final String RUTA_PLANTILLA = "/templates/email/recuperacion-acceso.html";

    private PlantillaRecuperacionHtml() {}

    public static String renderizar(String codigo, int minutosVigencia) {
        try (var input = PlantillaRecuperacionHtml.class.getResourceAsStream(RUTA_PLANTILLA)) {
            if (input == null) {
                throw new IllegalStateException("No se encontró la plantilla de recuperación de acceso.");
            }
            String plantilla = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return plantilla.replace("{{codigo}}", codigo)
                    .replace("{{minutosVigencia}}", Integer.toString(minutosVigencia));
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("No se pudo leer la plantilla de recuperación de acceso.", ex);
        }
    }
}

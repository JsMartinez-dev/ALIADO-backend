package com.sebastian.spring.mas.aliado.aliado.modules.propuestas.presentation.dto;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Acepta solo un entero JSON para tarifaCop.
 * Jackson coacciona por defecto decimales y cadenas a Integer; este parser
 * rechaza ambos antes de que la validación de Bean pueda normalizarlos.
 */
public final class TarifaCopDeserializer extends ValueDeserializer<Integer> {

    private static final String MENSAJE =
            "La tarifa debe ser un entero positivo en pesos colombianos.";

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token != JsonToken.VALUE_NUMBER_INT) {
            return context.reportInputMismatch(Integer.class, MENSAJE);
        }
        long value = parser.getLongValue();
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            return context.reportInputMismatch(Integer.class, MENSAJE);
        }
        return (int) value;
    }
}

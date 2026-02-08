package com.comercialvalerio.common.json;

import jakarta.json.stream.JsonParser;

/** Utilidades para {@link JsonParser}. */
public final class JsonParserUtils {
    private JsonParserUtils() {
    }

    /**
     * Devuelve el evento actual del parser o avanza cuando no hay uno disponible.
     * Si el parser no tiene más eventos o encuentra un valor nulo, se devuelve {@code null}.
     */
    public static JsonParser.Event currentEventOrNext(JsonParser parser) {
        if (!parser.hasNext()) {
            return null;
        }
        JsonParser.Event ev = null;
        try {
            ev = parser.currentEvent();
        } catch (IllegalStateException | UnsupportedOperationException ex) {
            // sin evento actual, avanzar al siguiente
        }
        if (ev == null) {
            ev = parser.next();
        }
        while (ev == JsonParser.Event.KEY_NAME
                || ev == JsonParser.Event.START_OBJECT
                || ev == JsonParser.Event.START_ARRAY) {
            if (!parser.hasNext()) {
                return null;
            }
            ev = parser.next();
        }
        return ev == JsonParser.Event.VALUE_NULL ? null : ev;
    }
}

package com.comercialvalerio.common.json;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import static com.comercialvalerio.common.json.JsonParserUtils.currentEventOrNext;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Deserializador base para tipos temporales usando una lista de {@link DateTimeFormatter}s.
 */
public abstract class AbstractTemporalDeserializer<T> implements JsonbDeserializer<T> {
    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        JsonParser.Event ev = currentEventOrNext(parser);
        if (ev == null) {
            return null;
        }
        if (ev == JsonParser.Event.VALUE_STRING) {
            String str = parser.getString();
            for (DateTimeFormatter fmt : formatters()) {
                try {
                    return parse(str, fmt);
                } catch (DateTimeParseException ex) {
                    // intentar con el siguiente patrón
                }
            }
            throw new JsonbException(invalidFormatMessage());
        } else if (ev == JsonParser.Event.VALUE_NUMBER) {
            int year = parser.getInt();
            return fromYear(year);
        }
        throw new JsonbException("Token inesperado " + ev + " al leer " + typeName());
    }

    /** Lista de formatters usada para analizar valores de texto. */
    protected abstract List<DateTimeFormatter> formatters();

    /** Analiza la cadena usando el formatter proporcionado. */
    protected abstract T parse(String str, DateTimeFormatter formatter) throws DateTimeParseException;

    /** Crea un valor solo con la parte del año. */
    protected abstract T fromYear(int year);

    /** Mensaje de error cuando ninguno de los formatters coincide. */
    protected abstract String invalidFormatMessage();

    /** Nombre del tipo temporal para los mensajes de error. */
    protected abstract String typeName();
}

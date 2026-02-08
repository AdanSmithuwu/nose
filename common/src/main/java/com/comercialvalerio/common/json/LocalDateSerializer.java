package com.comercialvalerio.common.json;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Serializa valores de {@link LocalDate} en formato ISO-8601. */
public class LocalDateSerializer implements JsonbSerializer<LocalDate> {
    @Override
    public void serialize(LocalDate obj, JsonGenerator generator, SerializationContext ctx) {
        if (obj == null) {
            generator.writeNull();
        } else {
            generator.write(obj.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
}

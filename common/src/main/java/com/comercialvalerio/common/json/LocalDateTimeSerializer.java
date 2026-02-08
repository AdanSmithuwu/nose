package com.comercialvalerio.common.json;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import java.time.LocalDateTime;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME;

/** Serializa valores de {@link LocalDateTime} usando el patrón DATETIME2 de SQL Server. */
public class LocalDateTimeSerializer implements JsonbSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime obj, JsonGenerator generator, SerializationContext ctx) {
        if (obj == null) {
            generator.writeNull();
        } else {
            generator.write(obj.format(SQL_DATETIME));
        }
    }
}

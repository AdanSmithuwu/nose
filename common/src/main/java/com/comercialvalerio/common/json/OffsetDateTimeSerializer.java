package com.comercialvalerio.common.json;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import java.time.OffsetDateTime;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME_WITH_ZONE;

/** Serializa valores de {@link OffsetDateTime} usando el patrón DATETIME2 de SQL Server. */
public class OffsetDateTimeSerializer implements JsonbSerializer<OffsetDateTime> {
    @Override
    public void serialize(OffsetDateTime obj, JsonGenerator generator, SerializationContext ctx) {
        if (obj == null) {
            generator.writeNull();
        } else {
            generator.write(obj.format(SQL_DATETIME_WITH_ZONE));
        }
    }
}

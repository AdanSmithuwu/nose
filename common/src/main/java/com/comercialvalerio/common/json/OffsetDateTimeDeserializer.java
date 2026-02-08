package com.comercialvalerio.common.json;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import static com.comercialvalerio.common.json.JsonParserUtils.currentEventOrNext;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME_WITH_ZONE;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/** Deserializa cadenas ISO o segundos epoch en {@link OffsetDateTime}. */
public class OffsetDateTimeDeserializer implements JsonbDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter FLEX_WITH_ZONE = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .appendOffset("+HH:MM", "Z")
            .toFormatter();

    private static final DateTimeFormatter FLEX = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter();
    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        JsonParser.Event ev = currentEventOrNext(parser);
        if (ev == null) {
            return null;
        }
        try {
            if (ev == JsonParser.Event.VALUE_STRING) {
                String str = parser.getString();
                try {
                    return OffsetDateTime.parse(str);
                } catch (DateTimeParseException ex) {
                    // intentar formatos de SQL Server
                }
                try {
                    return OffsetDateTime.parse(str, SQL_DATETIME_WITH_ZONE);
                } catch (DateTimeParseException ex) {
                    // intentar con fracción variable y zona
                }
                try {
                    return OffsetDateTime.parse(str, FLEX_WITH_ZONE);
                } catch (DateTimeParseException ex) {
                    // intentar sin offset
                }
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(str, SQL_DATETIME);
                    return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                } catch (DateTimeParseException ex) {
                    // intentar fracción variable sin zona
                }
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(str, FLEX);
                    return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                } catch (DateTimeParseException ex) {
                    return parseEpoch(new BigDecimal(str));
                }
            } else if (ev == JsonParser.Event.VALUE_NUMBER) {
                BigDecimal bd = parser.getBigDecimal();
                return parseEpoch(bd);
            }
        } catch (NumberFormatException | DateTimeParseException ex) {
            throw new JsonbException("Formato de fecha y hora inválido", ex);
        }
        throw new JsonbException("Token inesperado " + ev + " al leer OffsetDateTime");
    }

    private OffsetDateTime parseEpoch(BigDecimal bd) {
        long seconds = bd.longValue();
        int nanos = bd.remainder(BigDecimal.ONE).movePointRight(9).intValue();
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
    }
}

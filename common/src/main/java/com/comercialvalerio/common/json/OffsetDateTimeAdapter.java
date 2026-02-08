package com.comercialvalerio.common.json;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.SQL_DATETIME_WITH_ZONE;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/** Adaptador para OffsetDateTime con soporte para formatos de SQL Server. */
public class OffsetDateTimeAdapter implements JsonbAdapter<OffsetDateTime, String> {
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
    public String adaptToJson(OffsetDateTime obj) {
        return obj == null ? null : obj.format(SQL_DATETIME_WITH_ZONE);
    }

    @Override
    public OffsetDateTime adaptFromJson(String obj) {
        if (obj == null) {
            return null;
        }
        try {
            try {
                return OffsetDateTime.parse(obj);
            } catch (DateTimeParseException ex) {
                // intentar formatos de SQL Server
            }
            try {
                return OffsetDateTime.parse(obj, SQL_DATETIME_WITH_ZONE);
            } catch (DateTimeParseException ex) {
                // intentar con fracción variable y zona
            }
            try {
                return OffsetDateTime.parse(obj, FLEX_WITH_ZONE);
            } catch (DateTimeParseException ex) {
                // intentar sin offset
            }
            try {
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(obj, SQL_DATETIME);
                return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
            } catch (DateTimeParseException ex) {
                // intentar fracción variable sin zona
            }
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(obj, FLEX);
            return ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
        } catch (DateTimeParseException ex) {
            throw new JsonbException("Formato de fecha y hora inválido", ex);
        }
    }
}

package com.comercialvalerio.common.json;

import jakarta.json.bind.JsonbException;
import jakarta.json.bind.adapter.JsonbAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.comercialvalerio.common.json.DateTimeFormatterUtils.YEAR_ONLY_DATE;

/**
 * Adaptador JSON-B para manejar conversiones de {@link LocalDate}.
 */
public class LocalDateAdapter implements JsonbAdapter<LocalDate, String> {
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            YEAR_ONLY_DATE
    );

    @Override
    public String adaptToJson(LocalDate obj) {
        return obj == null ? null : obj.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public LocalDate adaptFromJson(String obj) {
        if (obj == null) {
            return null;
        }
        for (DateTimeFormatter fmt : FORMATTERS) {
            try {
                return LocalDate.parse(obj, fmt);
            } catch (DateTimeParseException ex) {
                // intentar con el siguiente formato
            }
        }
        throw new JsonbException("Formato de fecha inválido");
    }
}


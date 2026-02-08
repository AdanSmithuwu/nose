package com.comercialvalerio.common.json;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static com.comercialvalerio.common.json.DateTimeFormatterUtils.YEAR_ONLY_DATE;
import java.util.List;

/** Deserializa varios formatos de fecha en {@link LocalDate}. */
public class LocalDateDeserializer extends AbstractTemporalDeserializer<LocalDate> {
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            YEAR_ONLY_DATE
    );

    @Override
    protected List<DateTimeFormatter> formatters() {
        return FORMATTERS;
    }

    @Override
    protected LocalDate parse(String str, DateTimeFormatter formatter) {
        return LocalDate.parse(str, formatter);
    }

    @Override
    protected LocalDate fromYear(int year) {
        return LocalDate.of(year, 1, 1);
    }

    @Override
    protected String invalidFormatMessage() {
        return "Formato de fecha inválido";
    }

    @Override
    protected String typeName() {
        return "LocalDate";
    }
}

package com.comercialvalerio.presentation.ui.util;

import com.comercialvalerio.common.time.DateMapper;
import com.comercialvalerio.common.time.TimeZoneProvider;
import com.comercialvalerio.common.json.DateTimeFormatterUtils;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/** Métodos utilitarios para formatear fechas según la configuración regional. */
public final class DateFormatUtils {
    private static final String SHORT_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern(SHORT_PATTERN);

    private static final DateTimeFormatter SQL_FLEX_WITH_ZONE =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .appendOffset("+HH:MM", "Z")
                    .toFormatter();

    private static final DateTimeFormatter SQL_FLEX =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .optionalEnd()
                    .toFormatter();

    /**
     * Formato utilizado por el servidor para mostrar valores temporales sin
     * decimales ni zona horaria, empleando estilo latinoamericano.
     */
    private static final DateTimeFormatter SERVER_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateFormatUtils() {
    }

    /** Devuelve el patrón corto de fecha utilizado en la interfaz. */
    public static String getShortPattern() {
        return SHORT_PATTERN;
    }

    /** Formatea la fecha dada o retorna cadena vacía cuando {@code null}. */
    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    /** Devuelve el valor textual de {@code date} tal como lo envía el servidor. */
    public static String formatServer(LocalDate date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    /** Formatea la fecha y hora dadas o retorna cadena vacía cuando {@code null}. */
    public static String format(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FMT);
    }

    /**
     * Formatea el {@link OffsetDateTime} indicado en la zona horaria configurada
     * o devuelve una cadena vacía si es {@code null}.
     */
    public static String format(OffsetDateTime odt) {
        if (odt == null) {
            return "";
        }
        LocalDateTime ldt = DateMapper.toLocalDateTime(odt);
        return ldt.format(DATE_TIME_FMT);
    }

    /**
     * Devuelve el texto de {@code odt} usando el mismo formato que envia el
     * servidor.
     */
    public static String formatServer(OffsetDateTime odt) {
        return odt == null ? "" : odt.format(SERVER_FMT);
    }

    /**
     * Formatea segundos de época (con fracción opcional) utilizando la zona
     * horaria configurada.
     */
    public static String format(Number epochSeconds) {
        if (epochSeconds == null) {
            return "";
        }
        BigDecimal bd = new BigDecimal(epochSeconds.toString());
        long seconds = bd.longValue();
        int nanos = bd.remainder(BigDecimal.ONE).movePointRight(9).intValue();
        OffsetDateTime odt = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(seconds, nanos),
                TimeZoneProvider.zone());
        return format(odt);
    }

    /**
     * Formatea un valor de segundos de época utilizando el mismo patrón que el
     * servidor.
     */
    public static String formatServer(Number epochSeconds) {
        OffsetDateTime odt = parseOffsetDateTime(epochSeconds);
        return formatServer(odt);
    }

    /**
     * Devuelve el texto tal como lo proporciona el backend.
     * Solo retorna cadena vacía cuando el valor es {@code null}.
     */
    public static String formatServer(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof OffsetDateTime odt) {
            return formatServer(odt);
        }
        if (value instanceof LocalDate ld) {
            return formatServer(ld);
        }
        if (value instanceof Number n) {
            return formatServer(n);
        }
        if (value instanceof String s && !s.isBlank()) {
            OffsetDateTime odt = parseOffsetDateTime(s);
            return odt != null ? formatServer(odt) : s;
        }
        return value.toString();
    }

    /** Formatea el valor o muestra un guion cuando es {@code null}. */
    public static String formatOrDash(LocalDateTime dt) {
        return dt == null ? "‐" : format(dt);
    }

    /** Formatea el valor o muestra un guion cuando es {@code null}. */
    public static String formatOrDash(OffsetDateTime odt) {
        return odt == null ? "‐" : format(odt);
    }

    /**
     * Intenta convertir el valor indicado en un {@link OffsetDateTime}.
     * Acepta instancias de {@link OffsetDateTime}, {@link Number} con segundos
     * de época o cadenas compatibles con {@link OffsetDateTime#parse} y los
     * formatos de SQL Server definidos en {@link DateTimeFormatterUtils}.
     */
    public static OffsetDateTime parseOffsetDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime odt) {
            return odt;
        }
        if (value instanceof Number n) {
            BigDecimal bd = new BigDecimal(n.toString());
            long seconds = bd.longValue();
            int nanos = bd.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanos), ZoneOffset.UTC);
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return OffsetDateTime.parse(s);
            } catch (DateTimeParseException ex) {
                // intentar formatos de SQL Server
            }
            try {
                return OffsetDateTime.parse(s, DateTimeFormatterUtils.SQL_DATETIME_WITH_ZONE);
            } catch (DateTimeParseException ex) {
                // intentar con fracción variable y zona
            }
            try {
                return OffsetDateTime.parse(s, SQL_FLEX_WITH_ZONE);
            } catch (DateTimeParseException ex) {
                // intentar sin offset
            }
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatterUtils.SQL_DATETIME);
                return ldt.atZone(TimeZoneProvider.zone()).toOffsetDateTime();
            } catch (DateTimeParseException ex) {
                // intentar fracción variable sin zona
            }
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, SQL_FLEX);
                return ldt.atZone(TimeZoneProvider.zone()).toOffsetDateTime();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
        return null;
    }
}

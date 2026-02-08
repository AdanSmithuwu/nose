package com.comercialvalerio.presentation.ui.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Métodos utilitarios para formatear montos monetarios. */
public final class CurrencyUtils {
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final ThreadLocal<NumberFormat> FORMATTER =
            ThreadLocal.withInitial(() -> NumberFormat.getCurrencyInstance(ES_PE));

    private CurrencyUtils() {
    }

    /** Formatea el valor usando la configuración regional es-PE. */
    public static String format(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        NumberFormat nf = FORMATTER.get();
        return nf.format(value).replace('\u00A0', ' ');
    }
}

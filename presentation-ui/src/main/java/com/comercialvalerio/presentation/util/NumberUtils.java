package com.comercialvalerio.presentation.util;

import java.math.BigDecimal;

/** Métodos utilitarios para trabajar con números. */
public final class NumberUtils {

    private NumberUtils() {}

    /**
     * Devuelve la representación plana del {@link BigDecimal} indicado
     * sin ceros a la derecha. Si {@code value} es {@code null}
     * se devuelve una cadena vacía.
     *
     * @param value número a formatear
     * @return cadena plana sin ceros finales
     */
    public static String formatPlain(BigDecimal value) {
        if (value == null) return "";
        value = value.stripTrailingZeros();
        return value.toPlainString();
    }

    /**
     * Devuelve el valor con la escala indicada siempre presente. Si
     * {@code value} es {@code null} se devuelve una cadena vacía.
     *
     * @param value número a formatear
     * @param scale cantidad fija de decimales
     * @return representación en texto con la escala solicitada
     */
    public static String formatScale(BigDecimal value, int scale) {
        if (value == null) return "";
        return value.setScale(scale, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    /**
     * Devuelve el valor redondeado hacia abajo sin decimales. Si
     * {@code value} es {@code null} se devuelve una cadena vacía.
     *
     * @param value número a formatear
     * @return representación sin parte decimal
     */
    public static String formatInteger(BigDecimal value) {
        if (value == null) return "";
        return value.setScale(0, java.math.RoundingMode.DOWN).toPlainString();
    }

    /**
     * Devuelve el valor con al menos la escala indicada. Si posee
     * más decimales se conservan sin truncar.
     * Si {@code value} es {@code null} se devuelve una cadena vacía.
     *
     * @param value     número a formatear
     * @param minScale  cantidad mínima de decimales
     * @return representación en texto del número
     */
    public static String formatMinScale(BigDecimal value, int minScale) {
        if (value == null) return "";
        BigDecimal stripped = value.stripTrailingZeros();
        int digits = Math.max(minScale, stripped.scale());
        return stripped.setScale(digits, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }
}

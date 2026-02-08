package com.comercialvalerio.presentation.util;

import com.comercialvalerio.application.dto.ProductoDto;
import java.math.BigDecimal;

/** Métodos utilitarios para cálculos de precios. */
public final class PriceUtils {

    private PriceUtils() {}

    /**
     * Devuelve el precio adecuado para el producto y cantidad indicados.
     * Usa el precio mayorista cuando el producto lo permite y la
     * cantidad cumple con el mínimo requerido.
     */
    public static BigDecimal precioParaCantidad(ProductoDto prod, BigDecimal cantidad) {
        if (prod.mayorista()
                && prod.minMayorista() != null
                && prod.precioMayorista() != null
                && cantidad != null
                && cantidad.compareTo(BigDecimal.valueOf(prod.minMayorista())) >= 0) {
            return prod.precioMayorista();
        }
        return prod.precioUnitario();
    }

    /**
     * Devuelve siempre el precio mayorista cuando está disponible.
     * Si el producto no maneja precio mayorista, usa el unitario.
     */
    public static BigDecimal precioMayorista(ProductoDto prod) {
        if (prod.mayorista() && prod.precioMayorista() != null) {
            return prod.precioMayorista();
        }
        return prod.precioUnitario();
    }
}

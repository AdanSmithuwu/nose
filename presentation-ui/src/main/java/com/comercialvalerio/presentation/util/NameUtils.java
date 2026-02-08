package com.comercialvalerio.presentation.util;

/** Métodos utilitarios para trabajar con nombres de personas. */
public final class NameUtils {

    private NameUtils() {}

    /**
     * Devuelve una representación corta del nombre y apellidos indicados.
     * Se toma solo el primer elemento de cada uno si existen.
     *
     * @param nombres   nombres completos o {@code null}
     * @param apellidos apellidos completos o {@code null}
     * @return cadena con nombre y apellido abreviados
     */
    public static String formatNombreCorto(String nombres, String apellidos) {
        String nom = nombres == null ? "" : nombres.split("\\s+")[0];
        String ape = apellidos == null ? "" : apellidos.split("\\s+")[0];
        return (nom + " " + ape).trim();
    }
}

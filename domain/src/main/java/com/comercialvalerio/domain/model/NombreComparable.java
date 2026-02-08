package com.comercialvalerio.domain.model;

/** Interfaz para enumeraciones que comparan nombres sin distinguir mayúsculas. */
public interface NombreComparable {
    /** Devuelve el nombre legible para el valor del enum. */
    String getNombre();

    /**
     * Devuelve {@code true} si el nombre proporcionado coincide con esta instancia
     * sin considerar mayúsculas y minúsculas.
     */
    default boolean equalsNombre(String nombre) {
        return nombre != null && nombre.equalsIgnoreCase(getNombre());
    }
}

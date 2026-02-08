package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Enumeración de nombres válidos para {@link TipoProducto}. */
public enum TipoProductoNombre implements NombreComparable {
    UNIDAD_FIJA("Unidad fija"),
    VESTIMENTA("Vestimenta"),
    FRACCIONABLE("Fraccionable");

    private final String nombre;

    TipoProductoNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public static TipoProductoNombre fromNombre(String nombre) {
        return EnumByName.fromNombre(TipoProductoNombre.class, nombre);
    }
}

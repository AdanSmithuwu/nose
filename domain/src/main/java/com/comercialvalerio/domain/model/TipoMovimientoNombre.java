package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Enumeración de nombres válidos para {@link TipoMovimiento}. */
public enum TipoMovimientoNombre implements NombreComparable {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE("Ajuste"),
    CANCELACION("Cancelación");

    private final String nombre;

    TipoMovimientoNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public static TipoMovimientoNombre fromNombre(String nombre) {
        return EnumByName.fromNombre(TipoMovimientoNombre.class, nombre);
    }
}

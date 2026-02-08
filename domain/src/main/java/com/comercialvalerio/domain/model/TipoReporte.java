package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Tipo de reporte generado por el sistema. */
public enum TipoReporte implements NombreComparable {
    DIARIO("Diario"),
    MENSUAL("Mensual"),
    ROTACION("Rotacion");

    private final String nombre;

    TipoReporte(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static TipoReporte fromNombre(String nombre) {
        return EnumByName.fromNombre(TipoReporte.class, nombre);
    }

}

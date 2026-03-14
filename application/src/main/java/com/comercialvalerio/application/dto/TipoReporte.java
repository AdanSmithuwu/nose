package com.comercialvalerio.application.dto;

/** Tipo de reporte generado por el sistema. */
public enum TipoReporte {
    DIARIO("Diario"),
    MENSUAL("Mensual"),
    ROTACION("Rotacion");

    private final String nombre;

    TipoReporte(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static TipoReporte fromNombre(String nombre) {
        for (TipoReporte t : values()) {
            if (t.nombre.equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        throw new IllegalArgumentException("TipoReporte inválido: " + nombre);
    }

    @Override public String toString() { return nombre; }
}

package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Enumeración de nombres válidos para {@link Estado}. */
public enum EstadoNombre implements NombreComparable {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    DESACTIVADO("Desactivado"),
    INACTIVO_POR_UMBRAL("Inactivo por umbral"),
    EN_PROCESO("En Proceso"),
    COMPLETADA("Completada"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada");

    private final String nombre;

    EstadoNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public static EstadoNombre fromNombre(String nombre) {
        return EnumByName.fromNombre(EstadoNombre.class, nombre);
    }

}

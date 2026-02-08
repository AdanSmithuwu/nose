package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Enumeración de nombres válidos para {@link Rol}. */
public enum RolNombre implements NombreComparable {
    ADMINISTRADOR("Administrador"),
    EMPLEADO("Empleado");

    private final String nombre;

    RolNombre(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static RolNombre fromNombre(String nombre) {
        return EnumByName.fromNombre(RolNombre.class, nombre);
    }

}

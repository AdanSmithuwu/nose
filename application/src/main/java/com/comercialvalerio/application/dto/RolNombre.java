package com.comercialvalerio.application.dto;

/** Enumeración de nombres válidos para roles de usuario. */
public enum RolNombre {
    ADMINISTRADOR("Administrador"),
    EMPLEADO("Empleado");

    private final String nombre;

    RolNombre(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static RolNombre fromNombre(String nombre) {
        for (RolNombre r : values()) {
            if (r.nombre.equalsIgnoreCase(nombre)) {
                return r;
            }
        }
        throw new IllegalArgumentException("RolNombre inválido: " + nombre);
    }

    public boolean equalsNombre(String nombre) {
        return nombre != null && nombre.equalsIgnoreCase(this.nombre);
    }

    @Override public String toString() { return nombre; }
}

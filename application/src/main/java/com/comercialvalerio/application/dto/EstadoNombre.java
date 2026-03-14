package com.comercialvalerio.application.dto;

/** Enumeración de nombres válidos para estados. */
public enum EstadoNombre {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    DESACTIVADO("Desactivado"),
    INACTIVO_POR_UMBRAL("Inactivo por umbral"),
    EN_PROCESO("En Proceso"),
    COMPLETADA("Completada"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada");

    private final String nombre;
    
    EstadoNombre(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static EstadoNombre fromNombre(String nombre) {
        for (EstadoNombre e : values()) {
            if (e.nombre.equalsIgnoreCase(nombre)) {
                return e;
            }
        }
        throw new IllegalArgumentException("EstadoNombre inválido: " + nombre);
    }

    public boolean equalsNombre(String nombre) {
        return nombre != null && nombre.equalsIgnoreCase(this.nombre);
    }

    @Override public String toString() { return nombre; }
}

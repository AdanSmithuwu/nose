package com.comercialvalerio.application.dto;

/** Tipo de pedido admitido por el sistema (Domicilio o Especial). */
public enum TipoPedido {
    DOMICILIO("Domicilio"),
    ESPECIAL("Especial");

    private final String nombre;

    TipoPedido(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static TipoPedido fromNombre(String nombre) {
        for (TipoPedido t : values()) {
            if (t.nombre.equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        throw new IllegalArgumentException("TipoPedido inválido: " + nombre);
    }

    @Override public String toString() { return nombre; }
}

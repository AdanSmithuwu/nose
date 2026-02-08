package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.util.EnumByName;

/** Tipo de pedido admitido por el sistema (Domicilio o Especial). */
public enum TipoPedido implements NombreComparable {
    DOMICILIO("Domicilio"),
    ESPECIAL("Especial");

    private final String nombre;

    TipoPedido(String nombre) { this.nombre = nombre; }

    public String getNombre() { return nombre; }

    public static TipoPedido fromNombre(String nombre) {
        return EnumByName.fromNombre(TipoPedido.class, nombre);
    }

}

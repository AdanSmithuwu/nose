package com.comercialvalerio.application.dto;

public record TipoProductoDto(
    Integer idTipoProducto,
    String  nombre
) {
    @Override
    public String toString() {
        return nombre;
    }
}

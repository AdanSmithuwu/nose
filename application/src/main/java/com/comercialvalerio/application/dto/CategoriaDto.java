package com.comercialvalerio.application.dto;

public record CategoriaDto(
    Integer idCategoria,
    String  nombre,
    String  descripcion,
    String  estado
) {
    @Override
    public String toString() {
        return nombre;
    }
}

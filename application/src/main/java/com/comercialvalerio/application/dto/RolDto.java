package com.comercialvalerio.application.dto;

public record RolDto(
    Integer idRol,
    String  nombre,
    short   nivel
) {
    @Override
    public String toString() {
        return nombre;
    }
}

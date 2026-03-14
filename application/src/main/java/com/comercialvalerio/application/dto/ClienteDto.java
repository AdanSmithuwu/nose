package com.comercialvalerio.application.dto;
import java.time.LocalDate;

public record ClienteDto(
    Integer   idPersona,
    String    nombres,
    String    apellidos,
    String    dni,
    String    telefono,
    String    direccion,
    LocalDate fechaRegistro,
    String    estado,
    String    nombreCompleto
) {}

package com.comercialvalerio.application.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.comercialvalerio.common.DbConstraints;

public record ClienteCreateDto(
    @NotBlank(message = "nombres obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_PERSONA,
          message = "nombres máximo " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String nombres,
    @NotBlank(message = "apellidos obligatorio")
    @Size(max = DbConstraints.LEN_NOMBRE_PERSONA,
          message = "apellidos máximo " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres")
    @Pattern(regexp = "[^\\p{Cntrl}]+", message = "Carácteres inválidos")
    String apellidos,
    @NotBlank(message = "dni obligatorio")
    @Pattern(regexp = "\\d{" + DbConstraints.LEN_DNI + "}",
             message = "dni debe tener " + DbConstraints.LEN_DNI + " dígitos")
    String dni,
    @Size(max = DbConstraints.LEN_TELEFONO, message = "teléfono máximo " + DbConstraints.LEN_TELEFONO + " dígitos")
    @Pattern(regexp = "\\d{6," + DbConstraints.LEN_TELEFONO + "}", message =
             "teléfono debe tener entre 6 y " + DbConstraints.LEN_TELEFONO + " dígitos")
    String telefono,
    @NotBlank(message = "dirección obligatoria")
    @Size(max = DbConstraints.LEN_DIRECCION,
          message = "dirección máximo " + DbConstraints.LEN_DIRECCION + " caracteres")
    String direccion
) {}

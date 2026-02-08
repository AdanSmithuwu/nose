package com.comercialvalerio.domain.model;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;
import java.time.LocalDate;

/*
 * Cliente registrado (persona externa).
 * Además de los datos de {@link Persona} almacena la dirección de entrega.
 */
public class Cliente extends Persona {

    private String direccion;

    public Cliente(Integer idPersona, String nombres, String apellidos,
                   String dni, String telefono, String direccion,
                   LocalDate fechaRegistro, Estado estado) {
        super(idPersona, nombres, apellidos, dni, telefono,
              fechaRegistro, estado);
        validarDireccion(direccion);
        this.direccion = direccion.trim();
    }

    public Cliente() {}

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {
        validarDireccion(direccion);
        this.direccion = direccion.trim();
    }

    /* ---------- Validación interna ---------- */
    private void validarDireccion(String d) {
        requireNotBlank(d,
                "La dirección del cliente es obligatoria (máx. " + DbConstraints.LEN_DIRECCION + " caracteres)");
        requireMaxLength(d, DbConstraints.LEN_DIRECCION,
                "La dirección del cliente es obligatoria (máx. " + DbConstraints.LEN_DIRECCION + " caracteres)");
    }
    @Override
    public String toString() {
        return "Cliente{" +
                "idPersona=" + getIdPersona() +
                ", nombre='" + getNombres() + ' ' + getApellidos() + "'}";
    }
}

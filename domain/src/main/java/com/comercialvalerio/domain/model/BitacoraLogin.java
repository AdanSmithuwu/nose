package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import java.time.LocalDateTime;

/*
 * Registro de intentos de autenticación (éxitos y fallos).
 *
 * DDL (resumen):
 * • PK idBitacora   • FK empleado   • fechaEvento TIMESTAMP NOT NULL  
 * • exitoso BOOLEAN NOT NULL
 */
public class BitacoraLogin extends BaseEntity<Integer> {

    private Integer       idBitacora;  // PK autogenerada
    private Empleado      empleado;    // FK obligatorio
    private LocalDateTime fechaEvento; // no futura
    private boolean       exitoso;     // true = login correcto

    /* ---------- Constructor con invariantes ---------- */
    public BitacoraLogin(Integer idBitacora, Empleado empleado,
                         LocalDateTime fechaEvento, boolean exitoso) {
        validarEmpleado(empleado);
        validarFecha(fechaEvento);

        this.idBitacora  = idBitacora;
        this.empleado    = empleado;
        this.fechaEvento = fechaEvento;
        this.exitoso     = exitoso;
    }

    public BitacoraLogin() {}

    /* ---------- Getters ---------- */
    public Integer       getIdBitacora()  { return idBitacora; }
    @Override
    public Integer getId() { return idBitacora; }
    public Empleado      getEmpleado()    { return empleado; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public boolean       isExitoso()      { return exitoso; }

    /* ---------- Setters con validaciones ---------- */
    public void setIdBitacora(Integer id) {
        requireIdNotSet(this.idBitacora, id,
                "El idBitacora ya fue asignado y no puede modificarse");
        this.idBitacora = id;
    }

    public void setEmpleado(Empleado empleado) {
        validarEmpleado(empleado);
        this.empleado = empleado;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        validarFecha(fechaEvento);
        this.fechaEvento = fechaEvento;
    }

    public void setExitoso(boolean exitoso) { this.exitoso = exitoso; }

    /* ---------- Validaciones internas ---------- */
    private void validarEmpleado(Empleado e) {
        requireNotNull(e, "Debe registrarse el empleado que intentó iniciar sesión");
    }
    private void validarFecha(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha del evento no puede ser futura");
    }
}

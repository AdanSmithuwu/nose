package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;
import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/*
 * Entidad base para personas (cliente o empleado).
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idPersona</code></li>
 *   <li><code>dni</code> único (8 dígitos)</li>
 *   <li><code>telefono</code> opcional (6-15 dígitos)</li>
 *   <li><code>estado</code> FK a tabla <b>estado</b> (módulo = Persona)</li>
 * </ul>
 */
public class Persona extends BaseEntity<Integer> {

    private static final Logger LOG = Logger.getLogger(Persona.class.getName());
    private static final Pattern DNI_PATTERN =
            Pattern.compile("\\d{" + DbConstraints.LEN_DNI + "}");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
            "\\d{" + DbConstraints.TEL_MIN_DIGITS + "," + DbConstraints.LEN_TELEFONO + "}");
    private static final String  MODULO = "Persona";

    private Integer    idPersona;      // PK
    private String     nombres;        // obligatorio
    private String     apellidos;      // obligatorio
    private String     dni;            // único, 8 dígitos
    private String     telefono;       // opcional, 6-15 dígitos
    private LocalDate  fechaRegistro;  // no futura
    private Estado     estado;         // no nulo

    /* ---------- Constructor con invariantes ---------- */
    public Persona(Integer idPersona, String nombres, String apellidos,
                   String dni, String telefono,
                   LocalDate fechaRegistro, Estado estado) {

        validarNombres(nombres);
        validarApellidos(apellidos);
        validarDni(dni);
        validarTelefono(telefono);
        validarFechaRegistro(fechaRegistro);
        validarEstado(estado);

        this.idPersona     = idPersona;
        this.nombres       = nombres.trim();
        this.apellidos     = apellidos.trim();
        this.dni           = dni.trim();
        this.telefono      = telefono == null ? null : telefono.trim();
        this.fechaRegistro = fechaRegistro;
        this.estado        = estado;
    }

    public Persona() {}

    /* ---------- Getters ---------- */
    public Integer   getIdPersona()     { return idPersona; }
    @Override
    public Integer   getId()            { return idPersona; }
    public String    getNombres()       { return nombres; }
    public String    getApellidos()     { return apellidos; }
    public String    getDni()           { return dni; }
    public String    getTelefono()      { return telefono; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public Estado    getEstado()        { return estado; }

    /* ---------- Setters con validación ---------- */

    public void setIdPersona(Integer idPersona) {
        requireIdNotSet(this.idPersona, idPersona,
                "El idPersona ya fue asignado y no puede modificarse");
        this.idPersona = idPersona;
    }

    public void setNombres(String nombres) {
        validarNombres(nombres);
        this.nombres = nombres.trim();
    }

    public void setApellidos(String apellidos) {
        validarApellidos(apellidos);
        this.apellidos = apellidos.trim();
    }

    public void setDni(String dni) {
        validarDni(dni);
        this.dni = dni.trim();
    }

    public void setTelefono(String telefono) {
        validarTelefono(telefono);
        this.telefono = telefono == null ? null : telefono.trim();
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        validarFechaRegistro(fechaRegistro);
        this.fechaRegistro = fechaRegistro;
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        LOG.fine(() -> "Persona.setEstado -> "
                + (estado == null ? "null" : estado.getNombre()));
        this.estado = estado;
    }

    /* ---------- Validaciones internas ---------- */
    private void validarNombres(String n) {
        requireNotBlank(n, "Nombres obligatorios (máx. " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres)");
        requireMaxLength(n, DbConstraints.LEN_NOMBRE_PERSONA,
                "Nombres obligatorios (máx. " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres)");
    }
    private void validarApellidos(String a) {
        requireNotBlank(a,
                "Apellidos obligatorios (máx. " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres)");
        requireMaxLength(a, DbConstraints.LEN_NOMBRE_PERSONA,
                "Apellidos demasiado largos (máx. " + DbConstraints.LEN_NOMBRE_PERSONA + " caracteres)");
    }
    private void validarDni(String d) {
        if (d == null || !DNI_PATTERN.matcher(d).matches())
            throw new BusinessRuleViolationException(
                "DNI debe contener exactamente " + DbConstraints.LEN_DNI + " dígitos");
    }
    private void validarTelefono(String t) {
        if (t != null && !t.isBlank() && !TELEFONO_PATTERN.matcher(t).matches())
            throw new BusinessRuleViolationException(
                "Teléfono debe tener entre " + DbConstraints.TEL_MIN_DIGITS + " y "
                        + DbConstraints.LEN_TELEFONO + " dígitos o quedar vacío");
    }
    private void validarFechaRegistro(LocalDate f) {
        if (f == null || f.isAfter(LocalDate.now()))
            throw new BusinessRuleViolationException(
                "La fecha de registro no puede ser futura");
    }
    private void validarEstado(Estado e) {
        requireNotNull(e, "El estado de la persona es obligatorio");
        if (!MODULO.equalsIgnoreCase(e.getModulo()))
            throw new BusinessRuleViolationException(
                    "Estado inválido para Persona");
    }
}

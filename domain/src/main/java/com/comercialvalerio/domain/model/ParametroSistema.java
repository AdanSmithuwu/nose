package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import static com.comercialvalerio.domain.util.ValidationMessages.DESCRIPTION_TOO_LONG;
import com.comercialvalerio.common.DbConstraints;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

/*
 * Par clave-valor de configuración editable en tiempo de ejecución.
 *
 * DDL (resumen):
 * • PK clave VARCHAR(30)
 * • valor DECIMAL(10,2) NOT NULL CHECK(valor >= 0)
 * • descripcion NVARCHAR(120)
 * • actualizado DATETIME2 NOT NULL DEFAULT GETDATE()
 * • idEmpleado INT NOT NULL REFERENCES Empleado(idPersona)
 */
public class ParametroSistema {

    private static final Pattern CLAVE_PATTERN = Pattern.compile("[A-Z0-9_]+");

    private String        clave;        // PK
    private BigDecimal    valor;        // obligatorio
    private String        descripcion;  // opcional ≤ 120
    private LocalDateTime actualizado;  // no futura
    private Empleado      empleado;     // obligatorio (último editor)

    /* ---------- Constructor con invariantes ---------- */
    public ParametroSistema(String clave, BigDecimal valor, String descripcion,
                            LocalDateTime actualizado, Empleado empleado) {
        validarClave(clave);
        validarValor(valor);
        validarDescripcion(descripcion);
        validarFecha(actualizado);
        validarEmpleado(empleado);

        this.clave        = clave.trim().toUpperCase(Locale.ROOT);
        this.valor        = valor;
        this.descripcion  = descripcion == null ? null : descripcion.trim();
        this.actualizado  = actualizado;
        this.empleado     = empleado;
    }

    public ParametroSistema() {}

    /* ---------- Getters ---------- */
    public String        getClave()       { return clave; }
    public BigDecimal    getValor()       { return valor; }
    public String        getDescripcion() { return descripcion; }
    public LocalDateTime getActualizado() { return actualizado; }
    public Empleado      getEmpleado()    { return empleado; }

    /* ---------- Setters con validaciones ---------- */

    public void setClave(String clave) {
        // la PK puede cambiar solo si todavía es null
        requireIdNotSet(this.clave == null ? null : this.clave.toUpperCase(Locale.ROOT),
                clave == null ? null : clave.toUpperCase(Locale.ROOT),
                "La clave del parámetro ya fue asignada y no puede modificarse");
        validarClave(clave);
        this.clave = clave.trim().toUpperCase(Locale.ROOT);
    }

    public void setValor(BigDecimal valor) {
        validarValor(valor);
        this.valor = valor;
    }

    public void setDescripcion(String descripcion) {
        validarDescripcion(descripcion);
        this.descripcion = descripcion == null ? null : descripcion.trim();
    }

    public void setActualizado(LocalDateTime actualizado) {
        validarFecha(actualizado);
        this.actualizado = actualizado;
    }

    public void setEmpleado(Empleado empleado) {
        validarEmpleado(empleado);
        this.empleado = empleado;
    }

    /* ---------- Validaciones ---------- */
    private void validarClave(String c) {
        if (c == null || c.isBlank() || c.length() > DbConstraints.LEN_CLAVE_PARAM)
            throw new BusinessRuleViolationException(
                "La clave es obligatoria (máx. " + DbConstraints.LEN_CLAVE_PARAM + " caracteres)");
        if (!CLAVE_PATTERN.matcher(c).matches())
            throw new BusinessRuleViolationException(
                "La clave solo puede contener letras mayúsculas, números y '_' ");
    }
    private void validarValor(BigDecimal v) {
        requireNonNegative(v, "El valor debe ser \u2265 0");
        requirePrecision(v, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "valor fuera de rango (" + DbConstraints.PRECIO_PRECISION + ',' + DbConstraints.PRECIO_SCALE + ")");
    }
    private void validarDescripcion(String d) {
        if (d != null && d.length() > DbConstraints.LEN_DESCRIPCION)
            throw new BusinessRuleViolationException(
                DESCRIPTION_TOO_LONG);
    }
    private void validarFecha(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha de actualización no puede ser futura");
    }
    private void validarEmpleado(Empleado e) {
        if (e == null)
            throw new BusinessRuleViolationException(
                "Debe registrarse el empleado que modificó el parámetro");
    }
}

package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_CHARS_ONLY;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_REQUIRED_MAX_LENGTH;
import com.comercialvalerio.common.DbConstraints;
import java.util.regex.Pattern;

/*
 * Método de pago aceptado por Comercial's Valerio
 * (efectivo, Yape, Plin, tarjeta, etc.).
 *
 * <p>Del DDL:<br>
 * &nbsp;• PK (<code>idMetodoPago</code>)<br>
 * &nbsp;• Nombre único (<code>UNIQUE(nombre)</code>)</p>
 */
public class MetodoPago extends BaseEntity<Integer> {

    private static final Pattern NOMBRE_PATTERN = Pattern.compile("[^\\p{Cntrl}]+");

    private Integer idMetodoPago; // PK autogenerada
    private String  nombre;       // único

    /* ---------- Constructor con invariantes ---------- */
    public MetodoPago(Integer idMetodoPago, String nombre) {
        validarNombre(nombre);
        this.idMetodoPago = idMetodoPago;
        this.nombre       = nombre.trim();
    }

    /* Constructor vacío para frameworks. */
    public MetodoPago() {}

    /* ---------- Getters ---------- */
    public Integer getIdMetodoPago() { return idMetodoPago; }
    @Override
    public Integer getId() { return idMetodoPago; }
    public String  getNombre()       { return nombre;       }

    /* ---------- Setters ---------- */

    /* ID solo puede fijarse una vez. */
    public void setIdMetodoPago(Integer id) {
        requireIdNotSet(this.idMetodoPago, id,
                "El idMetodoPago ya fue asignado y no puede modificarse");
        this.idMetodoPago = id;
    }

    /* Valida y asigna el nombre. */
    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    /* ---------- Validaciones internas ---------- */

    private void validarNombre(String nombre) {
        requireNotBlank(nombre,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "del método de pago", DbConstraints.LEN_NOMBRE_CORTO));
        requireMaxLength(nombre, DbConstraints.LEN_NOMBRE_CORTO,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "del método de pago", DbConstraints.LEN_NOMBRE_CORTO));
        if (!NOMBRE_PATTERN.matcher(nombre).matches())
            throw new BusinessRuleViolationException(
                NAME_CHARS_ONLY);
    }
}

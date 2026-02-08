package com.comercialvalerio.domain.model;
import java.util.regex.Pattern;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_CHARS_ONLY;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_REQUIRED_MAX_LENGTH;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireMaxLength;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotBlank;

/*
 * Tipos de movimiento registrados en el kardex de inventario.
 * Los valores predeterminados son “Entrada”, “Salida”, “Ajuste” y “Cancelación”.
 *
 * <p>Del DDL:<br>
 * &nbsp;• PK (<code>idTipoMovimiento</code>)<br>
 * &nbsp;• Nombre único (<code>UNIQUE(nombre)</code>)</p>
 */
public class TipoMovimiento extends BaseEntity<Integer> {

  private static final Pattern NOMBRE_PATTERN = Pattern.compile("[^\\p{Cntrl}]+");

    private Integer idTipoMovimiento; // PK autogenerada
    private String  nombre;           // único

    /* ---------- Constructor con invariantes ---------- */
    public TipoMovimiento(Integer idTipoMovimiento, String nombre) {
        validarNombre(nombre);
        this.idTipoMovimiento = idTipoMovimiento;
        this.nombre           = nombre.trim();
    }

    public TipoMovimiento() {}

    /* ---------- Getters ---------- */
    public Integer getIdTipoMovimiento() { return idTipoMovimiento; }
    @Override
    public Integer getId() { return idTipoMovimiento; }
    public String  getNombre()           { return nombre;           }

    /* ---------- Setters ---------- */

    /* ID solo puede asignarse una vez. */
    public void setIdTipoMovimiento(Integer id) {
        requireIdNotSet(this.idTipoMovimiento, id,
                "El idTipoMovimiento ya fue asignado y no puede modificarse");
        this.idTipoMovimiento = id;
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
                        "del tipo de movimiento", DbConstraints.LEN_NOMBRE_CORTO));
        requireMaxLength(nombre, DbConstraints.LEN_NOMBRE_CORTO,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "del tipo de movimiento", DbConstraints.LEN_NOMBRE_CORTO));
        if (!NOMBRE_PATTERN.matcher(nombre).matches())
            throw new BusinessRuleViolationException(
                NAME_CHARS_ONLY);
    }
}

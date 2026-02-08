package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import static com.comercialvalerio.domain.util.ValidationMessages.DESCRIPTION_TOO_LONG;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_CHARS_ONLY;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_REQUIRED_MAX_LENGTH;
import static com.comercialvalerio.domain.util.ValidationMessages.STATE_REQUIRED;
import com.comercialvalerio.common.DbConstraints;
import java.util.regex.Pattern;

/*
 * Categoría comercial visible al cliente (p.ej. «Bebidas», «Lácteos», «Textiles»).
 *
 * <p>Del DDL:<br>
 * - PK (<code>idCategoria</code>)<br>
 * - Nombre único (<code>UNIQUE(nombre)</code>)</p>
 */
public class Categoria extends BaseEntity<Integer> {

    private static final Pattern NOMBRE_PATTERN = Pattern.compile("[^\\p{Cntrl}]+");
    private static final String  MODULO = "Categoria";

    private Integer idCategoria; // PK autogenerada
    private String  nombre;      // único
    private String  descripcion; // opcional
    private Estado  estado;      // FK obligatorio

    /* ---------- Constructor con invariantes ---------- */
    public Categoria(Integer idCategoria, String nombre, String descripcion, Estado estado) {
        validarNombre(nombre);
        validarDescripcion(descripcion);
        validarEstado(estado);
        this.idCategoria = idCategoria;
        this.nombre      = nombre.trim();
        this.descripcion = descripcion == null ? null : descripcion.trim();
        this.estado      = estado;
    }

    public Categoria(Integer idCategoria, String nombre) {
        this(idCategoria, nombre, null, null);
    }

    /* Constructor vacío requerido por frameworks. */
    public Categoria() {}

    /* ---------- Getters ---------- */
    public Integer getIdCategoria() { return idCategoria; }
    @Override
    public Integer getId() { return idCategoria; }
    public String  getNombre()      { return nombre;      }
    public String  getDescripcion() { return descripcion; }
    public Estado  getEstado()      { return estado;      }

    /* ---------- Setters con validaciones ---------- */

    /* ID solo puede fijarse una vez. */
    public void setIdCategoria(Integer idCategoria) {
        requireIdNotSet(this.idCategoria, idCategoria,
                "El idCategoria ya fue asignado y no puede modificarse");
        this.idCategoria = idCategoria;
    }

    /* Valida y asigna el nombre. */
    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) {
        validarDescripcion(descripcion);
        this.descripcion = descripcion == null ? null : descripcion.trim();
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        this.estado = estado;
    }

    /* ---------- Operaciones de dominio ---------- */

    /** Cambia el nombre de la categoría aplicando las validaciones. */
    public void cambiarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    /* ---------- Métodos privados de validación ---------- */

    private void validarNombre(String nombre) {
        requireNotBlank(nombre,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "de la categoría", DbConstraints.LEN_NOMBRE_CATEGORIA));
        requireMaxLength(nombre, DbConstraints.LEN_NOMBRE_CATEGORIA,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "de la categoría", DbConstraints.LEN_NOMBRE_CATEGORIA));
        if (!NOMBRE_PATTERN.matcher(nombre).matches())
            throw new BusinessRuleViolationException(NAME_CHARS_ONLY);
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > DbConstraints.LEN_DESCRIPCION)
            throw new BusinessRuleViolationException(
                DESCRIPTION_TOO_LONG);
    }

    private void validarEstado(Estado e) {
        requireNotNull(e, STATE_REQUIRED);
        if (!MODULO.equalsIgnoreCase(e.getModulo()))
            throw new BusinessRuleViolationException(
                    "Estado inválido para Categoria");
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "idCategoria=" + idCategoria +
                ", nombre='" + nombre + "'}";
    }
}

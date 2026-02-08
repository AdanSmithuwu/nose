package com.comercialvalerio.domain.model;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;

/*
 * Catálogo de tipos de producto existentes en la base de datos.
 * Los valores iniciales son “Unidad fija”, “Vestimenta” y “Fraccionable”.
 *
 * <p>Del DDL:<br>
 * &nbsp;• PK (<code>idTipoProducto</code>)<br>
 * &nbsp;• Nombre único (<code>UNIQUE(nombre)</code>)</p>
 */
public class TipoProducto extends BaseEntity<Integer> {

    private Integer idTipoProducto; // PK autogenerada
    private String  nombre;         // único

    /* ---------- Constructor con invariantes ---------- */
    public TipoProducto(Integer idTipoProducto, String nombre) {
        validarNombre(nombre);
        this.idTipoProducto = idTipoProducto;
        this.nombre         = nombre.trim();
    }

    public TipoProducto() {}

    /* ---------- Getters ---------- */
    public Integer getIdTipoProducto() { return idTipoProducto; }
    @Override
    public Integer getId() { return idTipoProducto; }
    public String  getNombre()         { return nombre;         }

    /* ---------- Setters ---------- */

    /* ID solo puede asignarse una vez. */
    public void setIdTipoProducto(Integer id) {
        requireIdNotSet(this.idTipoProducto, id,
                "El idTipoProducto ya fue asignado y no puede modificarse");
        this.idTipoProducto = id;
    }

    /* Valida y asigna el nombre. */
    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    /* ---------- Operaciones de dominio ---------- */

    /** Cambia el nombre del tipo de producto aplicando validaciones. */
    public void cambiarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    /* ---------- Validaciones internas ---------- */

    private void validarNombre(String nombre) {
        validateNombre(nombre, DbConstraints.LEN_NOMBRE_CORTO,
                       "del tipo de producto");
    }
}

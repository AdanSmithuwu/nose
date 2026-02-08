package com.comercialvalerio.domain.model;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;

/*
 * Catálogo de estados (p. ej. ACTIVO, INACTIVO, ENTREGADO) agrupados por módulo.
 */
public class Estado extends BaseEntity<Integer> {

    private Integer idEstado;   // PK (asignada por la BD)
    private String  nombre;     // nombre único dentro de cada módulo
    private String  modulo;     // contexto: Persona, Pedido, Producto, etc.

    /* ---------- Constructor principal con invariantes ---------- */
    public Estado(Integer idEstado, String nombre, String modulo) {
        validarNombre(nombre);
        validarModulo(modulo);
        this.idEstado = idEstado;
        this.nombre   = nombre.trim();
        this.modulo   = modulo.trim();
    }

    /* Constructor vacío necesario para frameworks */
    public Estado() {}

    /* ---------- Getters ---------- */
    public Integer getIdEstado()  { return idEstado; }
    @Override
    public Integer getId()       { return idEstado; }
    public String  getNombre()    { return nombre;   }
    public String  getModulo()    { return modulo;   }

    /* ---------- Setters con validación ---------- */

    /*
     * Asigna el ID solo si aún no estaba definido.
     */
    public void setIdEstado(Integer idEstado) {
        requireIdNotSet(this.idEstado, idEstado,
                "El idEstado ya fue asignado y no puede modificarse");
        this.idEstado = idEstado;
    }

    /* Valida y asigna el nombre. */
    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    /* Valida y asigna el módulo. */
    public void setModulo(String modulo) {
        validarModulo(modulo);
        this.modulo = modulo.trim();
    }

    /* ---------- Operaciones de dominio ---------- */

    /** Cambia el nombre del estado respetando las validaciones internas. */
    public void cambiarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    /** Cambia el módulo asociado al estado respetando las invariantes. */
    public void cambiarModulo(String nuevoModulo) {
        setModulo(nuevoModulo);
    }

    /* ---------- Métodos privados de validación ---------- */

    private void validarNombre(String nombre) {
        validateNombre(nombre, DbConstraints.LEN_NOMBRE_CORTO, "del estado");
    }

    private void validarModulo(String modulo) {
        validateModulo(modulo);
    }
}

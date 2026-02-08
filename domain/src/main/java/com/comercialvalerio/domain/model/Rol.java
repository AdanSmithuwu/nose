package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;

/*
 * Rol de seguridad/permiso dentro del sistema.
 * <p>Según el DDL, la tabla <b>rol</b> impone:</p>
 * <ul>
 *   <li>PK&nbsp;(<code>idRol</code>)</li>
 *   <li>Nombre único (<code>UNIQUE(nombre)</code>)</li>
 *   <li>nivel&nbsp;TINYINT&nbsp;(0-9) para jerarquía de permisos</li>
 * </ul>
 */
public class Rol extends BaseEntity<Integer> {

    private Integer idRol;  // PK (auto-numérica)
    private String  nombre; // único
    private short   nivel;  // 0-9

    /* ---------- Constructor principal con invariantes ---------- */
    public Rol(Integer idRol, String nombre, short nivel) {
        validarNombre(nombre);
        validarNivel(nivel);
        this.idRol  = idRol;
        this.nombre = nombre.trim();
        this.nivel  = nivel;
    }

    public Rol() {}

    /* ---------- Getters ---------- */
    public Integer getIdRol() { return idRol; }
    @Override
    public Integer getId() { return idRol; }
    public String  getNombre() { return nombre; }
    public short   getNivel()  { return nivel; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdRol(Integer idRol) {
        requireIdNotSet(this.idRol, idRol,
                "El idRol ya fue asignado y no puede modificarse");
        this.idRol = idRol;
    }

    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    public void setNivel(short nivel) {
        validarNivel(nivel);
        this.nivel = nivel;
    }

    /* ---------- Validaciones privadas ---------- */

    private void validarNombre(String nombre) {
        validateNombre(nombre, DbConstraints.LEN_NOMBRE_CORTO, "del rol");
    }

    private void validarNivel(short nivel) {
        if (nivel < 0 || nivel > 9)
            throw new BusinessRuleViolationException(
                "El nivel del rol debe estar entre 0 y 9");
    }
}

package com.comercialvalerio.domain.model;
import java.math.BigDecimal;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.STATE_REQUIRED;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNonNegative;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePositive;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Presentación o pack de un producto
 * (ej. “Caja x 12”, “Bidón 5 gal”, etc.).
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idPresentacion</code></li>
 *   <li>Clave única (<code>idProducto, cantidad</code>) — no puede existir más de
 *       una presentación con la misma cantidad para el mismo producto.</li>
 * </ul>
 */
public class Presentacion extends BaseEntity<Integer> {

    private static final String MODULO = "Producto";

    private Integer    idPresentacion; // PK autogenerada
    private Producto   producto;      // FK obligatorio
    private BigDecimal cantidad;      // > 0 (unidad física)
    private BigDecimal precio;        // ≥ 0 (precio del pack)
    private Estado     estado;        // FK obligatorio

    /* ---------- Constructor con invariantes ---------- */
    public Presentacion(Integer idPresentacion, Producto producto,
                        BigDecimal cantidad, BigDecimal precio, Estado estado) {

        validarProducto(producto);
        validarCantidad(cantidad);
        validarPrecio(precio);
        validarEstado(estado);

        this.idPresentacion = idPresentacion;
        this.producto       = producto;
        this.cantidad       = cantidad;
        this.precio         = precio;
        this.estado         = estado;
    }

    public Presentacion() {}

    /* ---------- Getters ---------- */
    public Integer    getIdPresentacion() { return idPresentacion; }
    @Override
    public Integer getId() { return idPresentacion; }
    public Producto   getProducto()       { return producto; }
    public BigDecimal getCantidad()       { return cantidad; }
    public BigDecimal getPrecio()         { return precio; }
    public Estado     getEstado()         { return estado; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdPresentacion(Integer id) {
        requireIdNotSet(this.idPresentacion, id,
                "El idPresentacion ya fue asignado y no puede modificarse");
        this.idPresentacion = id;
    }

    public void setProducto(Producto producto) {
        validarProducto(producto);
        this.producto = producto;
    }

    public void setCantidad(BigDecimal cantidad) {
        validarCantidad(cantidad);
        this.cantidad = cantidad;
    }

    public void setPrecio(BigDecimal precio) {
        validarPrecio(precio);
        this.precio = precio;
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        this.estado = estado;
    }

    /* ---------- Validaciones internas ---------- */

    private void validarProducto(Producto p) {
        requireNotNull(p, "El producto es obligatorio en Presentacion");
    }

    private void validarCantidad(BigDecimal c) {
        requirePositive(c, "La cantidad de la presentación debe ser mayor a 0");
        requirePrecision(c, DbConstraints.CANTIDAD_PRECISION, DbConstraints.CANTIDAD_SCALE,
                "cantidad fuera de rango (" + DbConstraints.CANTIDAD_PRECISION + ','
                        + DbConstraints.CANTIDAD_SCALE + ')');
    }

    private void validarPrecio(BigDecimal p) {
        requireNonNegative(p, "El precio de la presentación debe ser ≥ 0");
        requirePrecision(p, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "precio fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                        + DbConstraints.PRECIO_SCALE + ')');
    }

    private void validarEstado(Estado e) {
        requireNotNull(e, STATE_REQUIRED);
        if (!MODULO.equalsIgnoreCase(e.getModulo()))
            throw new BusinessRuleViolationException(
                    "Estado inválido para Presentacion");
    }
}

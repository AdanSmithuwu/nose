package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.QUANTITY_GREATER_THAN_ZERO;
import static com.comercialvalerio.domain.util.ValidationMessages.UNIT_PRICE_POSITIVE;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNonNegative;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePositive;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Línea de detalle de una transacción (venta / pedido).
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idDetalle</code></li>
 *   <li>FK a <b>transaccion</b>, <b>producto</b>, <b>talla_stock</b> (opcional)</li>
 *   <li>Clave única <code>(idTransaccion, idProducto, idTallaStock)</code>
 *       para evitar duplicados.</li>
 * </ul>
 */
public class DetalleTransaccion extends BaseEntity<Integer> {

    private Integer    idDetalle;       // PK autogenerada
    private Transaccion transaccion;    // obligatorio
    private Producto   producto;        // obligatorio
    private TallaStock tallaStock;      // opcional
    private BigDecimal cantidad;        // > 0
    private BigDecimal precioUnitario;  // ≥ 0
    private BigDecimal subtotal;        // cantidad * precioUnitario

    /* ---------- Constructor con invariantes ---------- */
    public DetalleTransaccion(Integer idDetalle, Transaccion transaccion,
                              Producto producto, TallaStock tallaStock,
                              BigDecimal cantidad, BigDecimal precioUnitario,
                              BigDecimal subtotal) {

        validarTransaccion(transaccion);
        validarProducto(producto);
        validarTallaStock(tallaStock, producto);
        validarCantidad(cantidad);
        validarPrecio(precioUnitario);
        validarSubtotal(subtotal, cantidad, precioUnitario);

        this.idDetalle      = idDetalle;
        this.transaccion    = transaccion;
        this.producto       = producto;
        this.tallaStock     = tallaStock;
        this.cantidad       = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal       = subtotal;
    }

    public DetalleTransaccion() {}

    /* ---------- Getters ---------- */
    public Integer    getIdDetalle()      { return idDetalle; }
    @Override
    public Integer getId() { return idDetalle; }
    public Transaccion getTransaccion()   { return transaccion; }
    public Producto   getProducto()       { return producto; }
    public TallaStock getTallaStock()     { return tallaStock; }
    public BigDecimal getCantidad()       { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal()       { return subtotal; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdDetalle(Integer id) {
        requireIdNotSet(this.idDetalle, id,
                "El idDetalle ya fue asignado y no puede modificarse");
        this.idDetalle = id;
    }

    public void setTransaccion(Transaccion t) {
        validarTransaccion(t);
        this.transaccion = t;
    }

    public void setProducto(Producto p) {
        validarProducto(p);
        validarTallaStock(this.tallaStock, p);
        this.producto = p;
    }

    public void setTallaStock(TallaStock ts) {
        validarTallaStock(ts, this.producto);
        this.tallaStock = ts;
    }

    public void setCantidad(BigDecimal c) {
        validarCantidad(c);
        this.cantidad = c;
        recalcularSubtotal();
    }

    public void setPrecioUnitario(BigDecimal p) {
        validarPrecio(p);
        this.precioUnitario = p;
        recalcularSubtotal();
    }

    /**
     * Valida que la cantidad ingresada coincida con alguna presentación
     * registrada para productos fraccionables.
     */
    public void validarPresentacion(java.util.List<Presentacion> presentaciones) {
        if (producto == null || producto.getTipoProducto() == null
                || cantidad == null || presentaciones == null)
            return;

        if (TipoProductoNombre.FRACCIONABLE.equalsNombre(
                producto.getTipoProducto().getNombre())) {
            boolean match = presentaciones.stream()
                    .anyMatch(p -> {
                        var c = p.getCantidad();
                        return c != null
                                && cantidad.remainder(c)
                                    .compareTo(BigDecimal.ZERO) == 0;
                    });
            if (!match)
                throw new BusinessRuleViolationException(
                        "La cantidad no coincide con ninguna presentación registrada.");
        }
    }

    private void recalcularSubtotal() {
        if (cantidad != null && precioUnitario != null)
            this.subtotal = cantidad.multiply(precioUnitario)
                                    .setScale(DbConstraints.SUBTOTAL_SCALE,
                                              RoundingMode.HALF_EVEN);
    }

    /* ---------- Validaciones internas ---------- */

    private void validarTransaccion(Transaccion t) {
        requireNotNull(t, "La transacción es obligatoria en el detalle");
    }

    private void validarProducto(Producto p) {
        requireNotNull(p, "El producto es obligatorio en el detalle");
    }

    private void validarTallaStock(TallaStock ts, Producto p) {
        if (ts != null) {
            if (p == null)
                throw new BusinessRuleViolationException(
                    "Debe indicar el producto antes de la talla");
            if (!Objects.equals(ts.getProducto().getIdProducto(),
                                p.getIdProducto()))
                throw new BusinessRuleViolationException(
                    "La talla seleccionada no corresponde al producto");
        }
    }

    private void validarCantidad(BigDecimal c) {
        requirePositive(c, QUANTITY_GREATER_THAN_ZERO);
        requirePrecision(c, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                "cantidad fuera de rango (" + DbConstraints.STOCK_PRECISION + ',' + DbConstraints.STOCK_SCALE + ")");
    }

    private void validarPrecio(BigDecimal p) {
        requireNonNegative(p, UNIT_PRICE_POSITIVE);
        requirePrecision(p, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                "precioUnitario fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                        + DbConstraints.PRECIO_SCALE + ')');
    }

    private void validarSubtotal(BigDecimal s, BigDecimal c, BigDecimal p) {
        if (c == null || p == null || s == null) return;
        BigDecimal esperado = c.multiply(p)
                               .setScale(DbConstraints.SUBTOTAL_SCALE,
                                         RoundingMode.HALF_EVEN);
        requirePrecision(s, DbConstraints.SUBTOTAL_PRECISION, DbConstraints.SUBTOTAL_SCALE,
                "subtotal fuera de rango (" + DbConstraints.SUBTOTAL_PRECISION + ','
                        + DbConstraints.SUBTOTAL_SCALE + ')');
        if (s.compareTo(esperado) != 0)
            throw new BusinessRuleViolationException(
                "El subtotal debe ser cantidad × precioUnitario");
    }
}

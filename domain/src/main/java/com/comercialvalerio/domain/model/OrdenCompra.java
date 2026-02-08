package com.comercialvalerio.domain.model;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Registro de compras de productos solicitados en un pedido.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idOrdenCompra</code></li>
 *   <li>FK a <b>pedido</b>, <b>cliente</b> y <b>producto</b></li>
 *   <li><code>cantidad</code> > 0 y <code>fechaCumplida</code> opcional</li>
 * </ul>
 */
public class OrdenCompra extends BaseEntity<Integer> {
    private Integer idOrdenCompra;
    private Pedido  pedido;
    private Cliente cliente;
    private Producto producto;
    private BigDecimal cantidad;
    private LocalDateTime fechaCumplida;

    public OrdenCompra(Integer idOrdenCompra, Pedido pedido, Cliente cliente,
                       Producto producto, BigDecimal cantidad,
                       LocalDateTime fechaCumplida) {
        validarPedido(pedido);
        validarProducto(producto);
        validarCliente(cliente);
        validarCantidad(cantidad);
        validarFechaCumplida(fechaCumplida);
        this.idOrdenCompra = idOrdenCompra;
        this.pedido = pedido;
        this.cliente = cliente;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaCumplida = fechaCumplida;
    }

    public OrdenCompra() {}

    public Integer getIdOrdenCompra() { return idOrdenCompra; }
    @Override
    public Integer getId() { return idOrdenCompra; }
    public Pedido getPedido() { return pedido; }
    public Cliente getCliente() { return cliente; }
    public Producto getProducto() { return producto; }
    public BigDecimal getCantidad() { return cantidad; }
    public LocalDateTime getFechaCumplida() { return fechaCumplida; }

    public void setIdOrdenCompra(Integer id) {
        requireIdNotSet(this.idOrdenCompra, id,
                "El idOrdenCompra ya fue asignado y no puede modificarse");
        this.idOrdenCompra = id;
    }
    public void setPedido(Pedido p) { validarPedido(p); this.pedido = p; }
    public void setCliente(Cliente c) { validarCliente(c); this.cliente = c; }
    public void setProducto(Producto p) { validarProducto(p); this.producto = p; }
    public void setCantidad(BigDecimal c) { validarCantidad(c); this.cantidad = c; }
    public void setFechaCumplida(LocalDateTime f) { validarFechaCumplida(f); this.fechaCumplida = f; }

    private void validarPedido(Pedido p) {
        requireNotNull(p, "Pedido requerido");
    }
    private void validarCliente(Cliente c) {
        requireNotNull(c, "Cliente requerido");
    }
    private void validarProducto(Producto p) {
        requireNotNull(p, "Producto requerido");
    }
    private void validarCantidad(BigDecimal c) {
        requirePositive(c, "Cantidad debe ser >0");
        requirePrecision(c, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                "cantidad fuera de rango (" + DbConstraints.STOCK_PRECISION + ',' + DbConstraints.STOCK_SCALE + ")");
    }
    private void validarFechaCumplida(LocalDateTime f) {
        if (f != null && f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                    "fechaCumplida no puede ser futura");
    }
}

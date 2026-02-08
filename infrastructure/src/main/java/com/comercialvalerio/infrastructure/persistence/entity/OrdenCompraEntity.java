package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.STOCK_PRECISION;
import static com.comercialvalerio.common.DbConstraints.STOCK_SCALE;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "OrdenCompra")
@Table(name = "OrdenCompra")
@NamedQueries({
    @NamedQuery(name = "OrdenCompra.findAll",
                query = "SELECT o FROM OrdenCompra o ORDER BY o.idOrdenCompra"),
    @NamedQuery(name = "OrdenCompra.byPedido",
                query = "FROM OrdenCompra o WHERE o.pedido.idTransaccion = :idPed"),
    @NamedQuery(name = "OrdenCompra.countByProducto",
                query = "SELECT COUNT(o) FROM OrdenCompra o WHERE o.producto.idProducto = :idProd"),
    @NamedQuery(name = "OrdenCompra.deleteByPedido",
                query = "DELETE FROM OrdenCompra o WHERE o.pedido.idTransaccion = :idPed")
})
public class OrdenCompraEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrdenCompra")
    private Integer idOrdenCompra;

    @ManyToOne(optional = false) @JoinColumn(name = "idPedido")
    private PedidoEntity pedido;

    @ManyToOne(optional = false) @JoinColumn(name = "idCliente", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne(optional = false) @JoinColumn(name = "idProducto")
    private ProductoEntity producto;

    @Column(name = "cantidad", nullable = false,
            precision = STOCK_PRECISION, scale = STOCK_SCALE)
    private BigDecimal cantidad;

    @Column(name = "fechaCumplida")
    private LocalDateTime fechaCumplida;

    public Integer getIdOrdenCompra() { return idOrdenCompra; }
    public void setIdOrdenCompra(Integer idOrdenCompra) { this.idOrdenCompra = idOrdenCompra; }
    public PedidoEntity getPedido() { return pedido; }
    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public ClienteEntity getCliente() { return cliente; }
    public void setCliente(ClienteEntity cliente) { this.cliente = cliente; }
    public ProductoEntity getProducto() { return producto; }
    public void setProducto(ProductoEntity producto) { this.producto = producto; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFechaCumplida() { return fechaCumplida; }
    public void setFechaCumplida(LocalDateTime fechaCumplida) { this.fechaCumplida = fechaCumplida; }
}

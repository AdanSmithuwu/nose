package com.comercialvalerio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "OrdenCompraPdf")
@Table(name = "OrdenCompraPdf")
@NamedQueries({
    @NamedQuery(name = "OrdenCompraPdf.findAll",
                query = "SELECT o FROM OrdenCompraPdf o ORDER BY o.fechaGeneracion DESC"),
    @NamedQuery(name = "OrdenCompraPdf.byPedido",
                query = "FROM OrdenCompraPdf o WHERE o.pedido.idTransaccion = :idPed")
})
public class OrdenCompraPdfEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrdenCompra")
    private Integer idOrdenCompra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPedido")
    private PedidoEntity pedido;

    @Lob
    @Column(name = "bytesPdf", nullable = false)
    private byte[] bytesPdf;

    @Column(name = "fechaGeneracion", nullable = false)
    private LocalDateTime fechaGeneracion;

    public Integer getIdOrdenCompra() { return idOrdenCompra; }
    public void setIdOrdenCompra(Integer idOrdenCompra) { this.idOrdenCompra = idOrdenCompra; }
    public PedidoEntity getPedido() { return pedido; }
    public void setPedido(PedidoEntity pedido) { this.pedido = pedido; }
    public byte[] getBytesPdf() { return bytesPdf; }
    public void setBytesPdf(byte[] bytesPdf) { this.bytesPdf = bytesPdf; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}

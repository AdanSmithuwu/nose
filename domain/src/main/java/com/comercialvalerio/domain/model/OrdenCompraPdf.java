package com.comercialvalerio.domain.model;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import java.time.LocalDateTime;

/** PDF generado para una orden de compra vinculada a un pedido. */
public class OrdenCompraPdf extends BaseEntity<Integer> {
    private Integer idOrdenCompra;
    private Pedido pedido;
    private byte[] bytesPdf;
    private LocalDateTime fechaGeneracion;

    public OrdenCompraPdf(Integer idOrdenCompra, Pedido pedido, byte[] bytesPdf, LocalDateTime fechaGeneracion) {
        validarPedido(pedido);
        validarPdf(bytesPdf);
        validarFecha(fechaGeneracion);
        this.idOrdenCompra = idOrdenCompra;
        this.pedido = pedido;
        this.bytesPdf = bytesPdf.clone();
        this.fechaGeneracion = fechaGeneracion;
    }

    public OrdenCompraPdf() {}

    public Integer getIdOrdenCompra() { return idOrdenCompra; }
    @Override
    public Integer getId() { return idOrdenCompra; }
    public Pedido getPedido() { return pedido; }
    public byte[] getBytesPdf() { return bytesPdf.clone(); }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }

    public void setIdOrdenCompra(Integer id) {
        requireIdNotSet(this.idOrdenCompra, id,
                "El idOrdenCompra ya fue asignado");
        this.idOrdenCompra = id;
    }
    public void setPedido(Pedido p) { validarPedido(p); this.pedido = p; }
    public void setBytesPdf(byte[] pdf) { validarPdf(pdf); this.bytesPdf = pdf.clone(); }
    public void setFechaGeneracion(LocalDateTime f) { validarFecha(f); this.fechaGeneracion = f; }

    private void validarPedido(Pedido p) {
        if (p == null)
            throw new BusinessRuleViolationException("Pedido requerido");
    }
    private void validarPdf(byte[] pdf) {
        if (pdf == null || pdf.length == 0)
            throw new BusinessRuleViolationException("PDF vacío");
    }
    private void validarFecha(LocalDateTime f) {
        if (f == null || f.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException("Fecha inválida");
    }
}

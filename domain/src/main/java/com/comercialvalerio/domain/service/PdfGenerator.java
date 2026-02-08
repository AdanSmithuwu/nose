package com.comercialvalerio.domain.service;

import java.util.List;

import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Transaccion;

/** Servicio que genera el comprobante de venta en PDF. */
public interface PdfGenerator {
    /**
     * Genera el comprobante de la transacción en formato PDF.
     *
     * @param tx       transacción a procesar
     * @param detalles detalle de productos vendidos
     * @param pagos    pagos registrados en la transacción
     * @return bytes del PDF generado
     * @throws PdfGenerationException si ocurre un error al crear el PDF
     */
    byte[] generar(Transaccion tx,
                   List<DetalleTransaccion> detalles,
                   List<PagoTransaccion> pagos) throws PdfGenerationException;
}

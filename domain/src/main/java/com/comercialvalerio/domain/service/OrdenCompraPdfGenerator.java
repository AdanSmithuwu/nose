package com.comercialvalerio.domain.service;

import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.domain.model.Pedido;

/** Servicio que genera el PDF resumen de una orden de compra. */
public interface OrdenCompraPdfGenerator {
    /**
     * Genera el documento PDF para el pedido indicado.
     *
     * @param pedido pedido a procesar
     * @return bytes del PDF generado
     * @throws PdfGenerationException si ocurre un error al crear el PDF
     */
    byte[] generar(Pedido pedido) throws PdfGenerationException;
}

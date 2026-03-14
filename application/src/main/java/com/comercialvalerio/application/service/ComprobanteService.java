package com.comercialvalerio.application.service;
import com.comercialvalerio.application.dto.ComprobanteDto;
import com.comercialvalerio.application.dto.ComprobantePdfDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.exception.PdfGenerationException;

public interface ComprobanteService {
    ComprobanteDto generar(Integer idTransaccion) throws PdfGenerationException;
    ComprobanteDto obtenerPorTransaccion(Integer idTx);
    byte[] descargarPdf(Integer idTx);
    /** Obtiene el PDF de un comprobante con su nombre de archivo. */
    ComprobantePdfDto obtenerPdf(Integer idTx);
    void enviarWhatsApp(Integer idTx, TelefonoDto telefono);
}

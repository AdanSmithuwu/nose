package com.comercialvalerio.infrastructure.pdf;

import java.util.List;

import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.domain.service.PdfGenerator;

import jakarta.enterprise.context.ApplicationScoped;

/** Implementación de {@link PdfGenerator} basada en {@link BoletaPdfUtil}. */
@ApplicationScoped
public class BoletaPdfGenerator implements PdfGenerator {

    @Override
    public byte[] generar(Transaccion tx,
                          List<DetalleTransaccion> detalles,
                          List<PagoTransaccion> pagos) throws PdfGenerationException {
        return BoletaPdfUtil.generar(tx, detalles, pagos);
    }
}

package com.comercialvalerio.infrastructure.service.report;

import java.math.BigDecimal;
import java.util.List;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.Phrase;
import com.comercialvalerio.infrastructure.pdf.PdfUtilBase;
import com.comercialvalerio.common.MoneyUtils;

/** Métodos de utilidad para construir tablas usadas en reportes PDF. */
public final class PdfTableUtils {
    private PdfTableUtils() {
    }

    /**
     * Construye la tabla de resumen mostrada en los reportes.
     *
     * @param numVentas  número de ventas completadas
     * @param numPedidos número de pedidos entregados
     * @param bruto      monto bruto
     * @param neto       monto neto
     * @return tabla configurada lista para añadirse al documento
     */
    public static PdfPTable buildResumenTable(long numVentas, long numPedidos,
                                              BigDecimal bruto, BigDecimal neto) {
        PdfPTable resumenTbl = new PdfPTable(4);
        resumenTbl.setWidthPercentage(100);
        resumenTbl.addCell(PdfUtilBase.headerCell("Ventas completadas"));
        resumenTbl.addCell(PdfUtilBase.headerCell("Pedidos entregados"));
        resumenTbl.addCell(PdfUtilBase.headerCell("Importe bruto"));
        resumenTbl.addCell(PdfUtilBase.headerCell("Importe neto"));
        resumenTbl.addCell(Long.toString(numVentas));
        resumenTbl.addCell(Long.toString(numPedidos));
        resumenTbl.addCell(bruto.toString());
        resumenTbl.addCell(neto.toString());
        return resumenTbl;
    }

    /**
     * Construye la tabla de pagos listando cada método y su monto.
     *
     * @param pagos lista de filas de pago
     * @return tabla configurada lista para añadirse al documento
     */
    public static PdfPTable buildPagosTable(List<PagoMetodoRow> pagos) {
        PdfPTable pagosTbl = new PdfPTable(2);
        pagosTbl.setWidthPercentage(60);
        pagosTbl.addCell(PdfUtilBase.headerCell("Método"));
        pagosTbl.addCell(PdfUtilBase.headerCell("Monto"));
        for (PagoMetodoRow pRow : pagos) {
            pagosTbl.addCell(pRow.metodo());
            PdfPCell cmonto = new PdfPCell(new Phrase(MoneyUtils.format(pRow.monto())));
            cmonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pagosTbl.addCell(cmonto);
        }
        return pagosTbl;
    }
}

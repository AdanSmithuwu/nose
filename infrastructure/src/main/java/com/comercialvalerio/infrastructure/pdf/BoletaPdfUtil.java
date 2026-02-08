package com.comercialvalerio.infrastructure.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Transaccion;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.common.MoneyUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/* Utilidad muy sencilla para generar un comprobante/boleta de venta en PDF. */
public final class BoletaPdfUtil {
    private BoletaPdfUtil() {}

    /** Formateador para la fecha y hora de la transacción. */
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static byte[] generar(Transaccion tx,
                                 List<DetalleTransaccion> detalles,
                                 List<PagoTransaccion> pagos) throws PdfGenerationException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            return generarEn(tx, detalles, pagos, out);
        } catch (IOException e) {
            throw new PdfGenerationException("No se pudo generar el comprobante", e);
        }
    }

    static byte[] generarEn(Transaccion tx,
                            List<DetalleTransaccion> detalles,
                            List<PagoTransaccion> pagos,
                            ByteArrayOutputStream out) throws PdfGenerationException {
        try (Document doc = PdfDocumentHelper.openDocument(out)) {

        // ----- Encabezado con logo e información de la empresa -----
        PdfUtilBase.startDocument(doc, "Boleta de Venta");

        // ----- Datos de la transacci\u00f3n y del cliente -----
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.addCell("Transacci\u00f3n:");
        info.addCell(String.valueOf(tx.getIdTransaccion()));
        info.addCell("Fecha:");
        java.time.LocalDateTime f = tx.getFecha();
        if (tx instanceof Pedido p && p.getFechaHoraEntrega() != null)
            f = p.getFechaHoraEntrega();
        info.addCell(f.format(DATE_TIME_FMT));
        Cliente c = tx.getCliente();
        if (c != null) {
            info.addCell("Cliente:");
            info.addCell(c.getNombres() + " " + c.getApellidos());
            info.addCell("DNI:");
            info.addCell(c.getDni());
            if (c.getTelefono() != null) {
                info.addCell("Tel\u00e9fono:");
                info.addCell(c.getTelefono());
            }
        }
        doc.add(info);
        doc.add(new Paragraph(" "));

        PdfPTable tbl = PdfUtilBase.productTable(detalles);
        doc.add(tbl);
        doc.add(new Paragraph(" "));

        java.math.BigDecimal bruto = tx.getTotalBruto();
        if (bruto == null) {
            bruto = detalles.stream()
                    .map(DetalleTransaccion::getSubtotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        }
        java.math.BigDecimal desc = tx.getDescuento();
        if (desc == null) desc = java.math.BigDecimal.ZERO;
        java.math.BigDecimal carg = tx.getCargo();
        if (carg == null) carg = java.math.BigDecimal.ZERO;
        java.math.BigDecimal neto = tx.getTotalNeto();
        if (neto == null) neto = bruto.subtract(desc).add(carg);

        PdfPTable tot = new PdfPTable(2);
        tot.setWidthPercentage(50);
        tot.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.addCell("Total bruto:");
        PdfPCell cbruto = new PdfPCell(new Phrase(MoneyUtils.format(bruto)));
        cbruto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.addCell(cbruto);
        tot.addCell("Descuento:");
        PdfPCell cdesc = new PdfPCell(new Phrase(MoneyUtils.format(desc)));
        cdesc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.addCell(cdesc);
        tot.addCell("Cargo:");
        PdfPCell ccarg = new PdfPCell(new Phrase(MoneyUtils.format(carg)));
        ccarg.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.addCell(ccarg);
        tot.addCell(PdfUtilBase.headerCell("TOTAL NETO:"));
        PdfPCell ctn = new PdfPCell(new Phrase(MoneyUtils.format(neto)));
        ctn.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tot.addCell(ctn);
        doc.add(tot);

        if (pagos != null && !pagos.isEmpty()) {
            Paragraph pagosTit = new Paragraph("Pagos:", new Font(Font.HELVETICA, 12, Font.BOLD));
            pagosTit.setSpacingBefore(10f);
            doc.add(pagosTit);
            PdfPTable tp = new PdfPTable(2);
            tp.setWidthPercentage(60);
            tp.addCell(PdfUtilBase.headerCell("Método"));
            tp.addCell(PdfUtilBase.headerCell("Monto"));
            for (PagoTransaccion pago : pagos) {
                tp.addCell(pago.getMetodoPago().getNombre());
                PdfPCell cmonto = new PdfPCell(new Phrase(MoneyUtils.format(pago.getMonto())));
                cmonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tp.addCell(cmonto);
            }
            doc.add(tp);
        }

        Paragraph gracias = new Paragraph("\u00a1Gracias por su compra!", PdfDocumentHelper.smallItalicFont());
        gracias.setAlignment(Element.ALIGN_CENTER);
        gracias.setSpacingBefore(10f);
        doc.add(gracias);
        doc.add(new Paragraph(" "));
        } catch (DocumentException e) {
            throw new PdfGenerationException("No se pudo generar el comprobante", e);
        }
        return out.toByteArray();
    }

}

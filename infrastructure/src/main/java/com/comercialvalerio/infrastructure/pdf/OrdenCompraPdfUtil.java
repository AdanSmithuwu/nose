package com.comercialvalerio.infrastructure.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.common.MoneyUtils;
import com.comercialvalerio.domain.service.OrdenCompraPdfGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/** Genera un PDF sencillo con resumen de un pedido. */
@ApplicationScoped
public class OrdenCompraPdfUtil implements OrdenCompraPdfGenerator {

    /** Formateador para la fecha y hora del pedido. */
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public byte[] generar(Pedido pedido) throws PdfGenerationException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            return generarEn(pedido, out);
        } catch (IOException e) {
            throw new PdfGenerationException("Error generando orden", e);
        }
    }

    private static byte[] generarEn(Pedido pedido, ByteArrayOutputStream out) throws PdfGenerationException {
        byte[] bytes;
        try (Document doc = PdfDocumentHelper.openDocument(out)) {

        PdfUtilBase.startDocument(doc, "Orden de Compra");

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.addCell("Pedido:");
        info.addCell(String.valueOf(pedido.getIdTransaccion()));
        info.addCell("Fecha:");
        info.addCell(pedido.getFecha().format(DATE_TIME_FMT));
        Cliente c = pedido.getCliente();
        if (c != null) {
            info.addCell("Cliente:");
            info.addCell(c.getNombres() + " " + c.getApellidos());
            info.addCell("DNI:");
            info.addCell(c.getDni());
        }
        info.addCell("Dirección:");
        info.addCell(pedido.getDireccionEntrega());
        doc.add(info);
        doc.add(new Paragraph(" "));

        PdfPTable tbl = PdfUtilBase.productTable(pedido.getDetalles());
        doc.add(tbl);
        doc.add(new Paragraph(" "));

        if (pedido.getTotalBruto() != null && pedido.getDescuento() != null &&
            pedido.getCargo() != null && pedido.getTotalNeto() != null) {
            PdfPTable tot = new PdfPTable(2);
            tot.setWidthPercentage(50);
            tot.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.addCell("Total bruto:");
            PdfPCell cbruto = new PdfPCell(new Phrase(MoneyUtils.format(pedido.getTotalBruto())));
            cbruto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.addCell(cbruto);
            tot.addCell("Descuento:");
            PdfPCell cdesc = new PdfPCell(new Phrase(MoneyUtils.format(pedido.getDescuento())));
            cdesc.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.addCell(cdesc);
            tot.addCell("Cargo:");
            PdfPCell ccargo = new PdfPCell(new Phrase(MoneyUtils.format(pedido.getCargo())));
            ccargo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.addCell(ccargo);
            tot.addCell(PdfUtilBase.headerCell("TOTAL NETO:"));
            PdfPCell ctn = new PdfPCell(new Phrase(MoneyUtils.format(pedido.getTotalNeto())));
            ctn.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tot.addCell(ctn);
            doc.add(tot);
        }

        Paragraph gracias = new Paragraph("¡Gracias por su pedido!", PdfDocumentHelper.smallItalicFont());
        gracias.setAlignment(Element.ALIGN_CENTER);
        gracias.setSpacingBefore(10f);
        doc.add(gracias);
        doc.add(new Paragraph(" "));
        } catch (DocumentException e) {
            throw new PdfGenerationException("Error generando orden", e);
        }
        bytes = out.toByteArray();
        return bytes;
    }
}

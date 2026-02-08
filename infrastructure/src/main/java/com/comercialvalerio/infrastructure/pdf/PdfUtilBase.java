package com.comercialvalerio.infrastructure.pdf;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import com.comercialvalerio.infrastructure.config.AppConfig;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.common.MoneyUtils;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/** Métodos auxiliares compartidos por las utilidades de PDF. */
@ApplicationScoped
public class PdfUtilBase {
    PdfUtilBase() {}

    /** Caché que mantiene el logo de la empresa escalado. */
    private static final AtomicReference<Image> LOGO_REF = new AtomicReference<>();

    /** Formateador para las cantidades de productos. */
    private static final ThreadLocal<NumberFormat> NUMBER_FMT =
            ThreadLocal.withInitial(PdfUtilBase::createNumberFormat);

    /** Carga y escala la imagen del logo. */
    private static Image loadLogo() {
        try {
            var url = PdfUtilBase.class.getClassLoader().getResource("logo.png");
            if (url != null) {
                Image loaded = Image.getInstance(url);
                loaded.scaleToFit(60, 60);
                return loaded;
            }
        } catch (IOException | BadElementException e) {
            LOG.log(Level.WARNING, "No se pudo cargar la imagen del logo", e);
        }
        return null;
    }

    /** Crea un formateador de cantidades con configuración es-PE. */
    private static NumberFormat createNumberFormat() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("es", "PE"));
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        return nf;
    }

    /**
     * Devuelve el logo de la empresa ya escalado, cargándolo de forma
     * perezosa en el primer acceso exitoso.
     */
    static Image getLogo() {
        Image img = LOGO_REF.get();
        if (img == null) {
            Image loaded = loadLogo();
            if (loaded != null) {
                LOGO_REF.compareAndSet(null, loaded);
            }
            img = LOGO_REF.get();
        }
        return img;
    }
    private static final Logger LOG = Logger.getLogger(PdfUtilBase.class.getName());

    private static final String COMPANY_NAME = AppConfig.get("company.name");
    private static final String COMPANY_RUC = AppConfig.get("company.ruc");
    private static final String COMPANY_ADDRESS = AppConfig.get("company.address");
    private static final String COMPANY_PHONE = AppConfig.get("company.phone");

    /** Agrega la cabecera común con logo e información de la empresa. */
    static void addStandardHeader(Document doc) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{1, 3});
        header.setWidthPercentage(100);

        PdfPCell logoCell;
        Image baseLogo = getLogo();
        if (baseLogo != null) {
            try {
                Image img = Image.getInstance(baseLogo);
                logoCell = new PdfPCell(img, false);
            } catch (BadElementException e) {
                LOG.log(Level.WARNING,
                        "No se pudo instanciar la imagen del logo", e);
                logoCell = new PdfPCell(new Phrase(""));
            }
        } else {
            logoCell = new PdfPCell(new Phrase(""));
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(5);
        header.addCell(logoCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.addElement(new Paragraph(COMPANY_NAME, new Font(Font.HELVETICA, 14, Font.BOLD)));
        infoCell.addElement(new Paragraph("RUC " + COMPANY_RUC));
        infoCell.addElement(new Paragraph(COMPANY_ADDRESS));
        infoCell.addElement(new Paragraph("Tel: " + COMPANY_PHONE));
        header.addCell(infoCell);

        doc.add(header);
        doc.add(new Paragraph(" "));
    }

    /**
     * Inicia el documento con la cabecera estándar y un título centrado.
     *
     * @param doc   documento donde escribir
     * @param title texto del título
     */
    public static void startDocument(Document doc, String title) throws DocumentException {
        addStandardHeader(doc);
        doc.add(createTitle(title));
        doc.add(new Paragraph(" "));
    }

    /** Crea un párrafo de título en negrita y centrado. */
    static Paragraph createTitle(String text) {
        Paragraph p = new Paragraph(text, PdfDocumentHelper.titleFont());
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    /** Crea una celda de encabezado para títulos de columnas. */
    public static PdfPCell headerCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 12, Font.BOLD)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    /**
     * Crea una tabla con productos, cantidad, precio unitario y subtotal.
     *
     * @param detalles los ítems de la transacción
     * @return la {@link PdfPTable} poblada
     */
    static PdfPTable productTable(List<DetalleTransaccion> detalles) {
        PdfPTable tbl = new PdfPTable(new float[] {4, 1, 1, 1});
        tbl.setWidthPercentage(100);
        tbl.addCell(headerCell("Producto"));
        tbl.addCell(headerCell("Cant."));
        tbl.addCell(headerCell("P.Unit"));
        tbl.addCell(headerCell("Subtotal"));
        if (detalles != null) {
            NumberFormat num = NUMBER_FMT.get();
            for (DetalleTransaccion d : detalles) {
                tbl.addCell(d.getProducto().getNombre());

                PdfPCell ccant = new PdfPCell(new Phrase(num.format(d.getCantidad())));
                ccant.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tbl.addCell(ccant);

                PdfPCell cpre = new PdfPCell(new Phrase(MoneyUtils.format(d.getPrecioUnitario())));
                cpre.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tbl.addCell(cpre);

                PdfPCell csub = new PdfPCell(new Phrase(MoneyUtils.format(d.getSubtotal())));
                csub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tbl.addCell(csub);
            }
        }
        return tbl;
    }

    @PreDestroy
    void clearCache() {
        LOGO_REF.set(null);
    }
}

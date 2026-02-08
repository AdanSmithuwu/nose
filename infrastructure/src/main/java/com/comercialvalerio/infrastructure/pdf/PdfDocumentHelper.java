package com.comercialvalerio.infrastructure.pdf;

import java.io.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;

/** Métodos de utilidad para crear documentos OpenPDF con fuentes estándar. */
public final class PdfDocumentHelper {
    private PdfDocumentHelper() {
    }

    /** Instancias de fuente en caché para evitar crearlas repetidamente. */
    public static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    public static final Font NORMAL_FONT = new Font(Font.HELVETICA, 12);
    public static final Font SMALL_ITALIC_FONT = new Font(Font.HELVETICA, 10, Font.ITALIC);

    /**
     * Crea un nuevo {@link Document}, configura un {@link PdfWriter} para
     * escribir en el flujo indicado y abre el documento.
     *
     * @param out flujo que recibirá los bytes del PDF
     * @return un {@link Document} abierto y listo para escribir
     * @throws DocumentException si el documento no puede inicializarse
     */
    public static Document openDocument(ByteArrayOutputStream out) throws DocumentException {
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        writer.setCloseStream(false);
        doc.open();
        return doc;
    }

    /** Fuente estándar de títulos usada en los PDFs. */
    public static Font titleFont() {
        return TITLE_FONT;
    }

    /** Fuente para texto normal. */
    public static Font normalFont() {
        return NORMAL_FONT;
    }

    /** Fuente pequeña e inclinada para pies de página o notas. */
    public static Font smallItalicFont() {
        return SMALL_ITALIC_FONT;
    }
}

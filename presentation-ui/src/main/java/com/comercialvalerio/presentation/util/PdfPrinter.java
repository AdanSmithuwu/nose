package com.comercialvalerio.presentation.util;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Utilidad para imprimir documentos PDF usando los servicios de impresión de la plataforma.
 */
public final class PdfPrinter {
    interface DesktopBridge {
        boolean isPrintSupported();
        void print(File file) throws IOException;
    }

    private static final DesktopBridge DEFAULT_BRIDGE = new DesktopBridge() {
        @Override
        public boolean isPrintSupported() {
            return Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.PRINT);
        }

        @Override
        public void print(File file) throws IOException {
            Desktop.getDesktop().print(file);
        }
    };

    private static final DesktopBridge DESKTOP_BRIDGE = DEFAULT_BRIDGE;

    private PdfPrinter() {}

    /**
     * Muestra un diálogo de impresión y envía los bytes PDF al servicio seleccionado.
     *
     * @param pdfBytes datos del PDF
     * @throws PrintException si la impresión falla
     */
    public static void print(byte[] pdfBytes) throws PrintException {
        if (pdfBytes == null || pdfBytes.length == 0) return;

        // Preferir el servicio de impresión de Windows cuando esté disponible
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("windows") && DESKTOP_BRIDGE.isPrintSupported()) {
            Path temp = null;
            try {
                temp = Files.createTempFile("cv-print", ".pdf");
                Files.write(temp, pdfBytes);
                File tempFile = temp.toFile();
                tempFile.deleteOnExit();
                DESKTOP_BRIDGE.print(tempFile);
                return;
            } catch (IOException e) {
                // continuar con el servicio de impresión genérico
            } finally {
                if (temp != null) {
                    try {
                        Files.deleteIfExists(temp);
                    } catch (IOException ignore) {
                        // ignorado
                    }
                }
            }
        }

        DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();

        if ((services == null || services.length == 0) && defaultService == null) {
            throw new PrintException("No hay servicios de impresi\u00f3n disponibles");
        }
        if (services == null || services.length == 0) {
            services = new PrintService[]{defaultService};
        }

        PrintService service;
        try {
            service = ServiceUI.printDialog(null, 200, 200, services, defaultService, flavor, attrs);
        } catch (HeadlessException | IllegalArgumentException e) {
            throw new PrintException("No se pudo abrir el di\u00e1logo de impresi\u00f3n", e);
        }
        if (service == null) {
            return; // usuario canceló
        }

        DocPrintJob job = service.createPrintJob();
        try (ByteArrayInputStream in = new ByteArrayInputStream(pdfBytes)) {
            Doc doc = new SimpleDoc(in, flavor, null);
            job.print(doc, attrs);
        } catch (PrintException e) {
            throw e;
        } catch (IOException e) {
            throw new PrintException("Error enviando a la impresora", e);
        }
    }
}

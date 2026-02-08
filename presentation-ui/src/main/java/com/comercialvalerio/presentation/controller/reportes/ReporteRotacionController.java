package com.comercialvalerio.presentation.controller.reportes;

import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.reportes.FormReporteRotacion;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.ui.base.TableUtils;

import javax.print.PrintException;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para {@link FormReporteRotacion}.
 */
public class ReporteRotacionController {

    private final FormReporteRotacion view;
    private static final Logger LOG =
            Logger.getLogger(ReporteRotacionController.class.getName());

    public ReporteRotacionController(FormReporteRotacion view) {
        this.view = view;
    }

    /**
     * Intenta obtener la fecha del selector o parsea el texto ingresado.
     * Devuelve {@code null} si el contenido no es válido.
     */
    private java.time.LocalDate parseDate(
            com.comercialvalerio.presentation.ui.base.DatePickerField sp) {
        java.time.LocalDate d = sp.getDate();
        if (d == null) {
            try {
                d = java.time.LocalDate.parse(sp.getText(),
                        java.time.format.DateTimeFormatter.ofPattern(
                                com.comercialvalerio.presentation.ui.util.DateFormatUtils.getShortPattern()));
            } catch (Exception ignore) {
                // texto no válido
            }
        }
        return d;
    }

    /** Carga los datos de rotación en la tabla. */
    public void loadData() {
        final java.time.LocalDate desde = view.getSpDesde().getDate() == null ?
                java.time.LocalDate.now() : view.getSpDesde().getDate();
        final java.time.LocalDate hasta = (view.getSpHasta().getDate() == null ?
                java.time.LocalDate.now() : view.getSpHasta().getDate()).plusDays(1);
        final Integer top = ((Integer) view.getSpTop().getValue()) == 0 ? null : (Integer) view.getSpTop().getValue();
        AsyncTasks.busy(view, () -> UiContext.reporteSvc().datosRotacion(desde, hasta, top), lista -> {
            TableUtils.clearModel(view.getModel());
            for (var r : lista) {
                view.getModel().addRow(
                        new Object[]{r.posicion(), r.producto(), r.categoria(), r.unidades(), r.importe()});
            }
            TableUtils.packColumns(view.getTable());
            TableUtils.updateEmptyView(
                    view.getScroll(),
                    view.getTable(),
                    view.getLblEmpty());
            view.updateButtons();
        });
    }

    /** Genera el PDF, lo guarda y abre el archivo. */
    public void generar() {
        java.time.LocalDate desde = parseDate(view.getSpDesde());
        java.time.LocalDate hasta = parseDate(view.getSpHasta());
        if (desde == null || hasta == null) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Seleccione fechas válidas",
                    "Fechas obligatorias",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasta.isBefore(desde)) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango inválido",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        hasta = hasta.plusDays(1);
        final Integer top = ((Integer) view.getSpTop().getValue()) == 0 ? null : (Integer) view.getSpTop().getValue();
        final java.time.LocalDate fDesde = desde;
        final java.time.LocalDate fHasta = hasta;
        AsyncTasks.busy(view, () -> {
            var rep = UiContext.reporteSvc().guardarRotacion(fDesde, fHasta, top);
            return Base64.getDecoder().decode(rep.pdfBase64());
        }, pdf -> {
            File dir = UserPrefs.getPdfDirectory();
            JFileChooser fc = dir != null ? new JFileChooser(dir) : new JFileChooser();
            fc.setSelectedFile(new File("reporte-rotacion.pdf"));
            if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    Files.write(fc.getSelectedFile().toPath(), pdf);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Error al guardar archivo", ex);
                    ErrorHandler.handle(new IllegalStateException("Error al guardar archivo", ex));
                }
            }
            view.getBtnPrint().setEnabled(true);
        });
    }

    /** Imprime el último reporte generado. */
    public void printPdf() {
        java.time.LocalDate desde = parseDate(view.getSpDesde());
        java.time.LocalDate hasta = parseDate(view.getSpHasta());
        if (desde == null || hasta == null) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Seleccione fechas válidas",
                    "Fechas obligatorias",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasta.isBefore(desde)) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango inválido",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        hasta = hasta.plusDays(1);
        final Integer top = ((Integer) view.getSpTop().getValue()) == 0 ? null : (Integer) view.getSpTop().getValue();
        final java.time.LocalDate fDesde = desde;
        final java.time.LocalDate fHasta = hasta;
        AsyncTasks.busy(view, () -> {
            var rep = UiContext.reporteSvc().guardarRotacion(fDesde, fHasta, top);
            return Base64.getDecoder().decode(rep.pdfBase64());
        }, pdf -> {
            try {
                PdfPrinter.print(pdf);
                view.getBtnPrint().setEnabled(true);
            } catch (PrintException ex) {
                LOG.log(Level.SEVERE, "Error al imprimir el reporte", ex);
                ErrorHandler.handle(new IllegalStateException(
                        "Error al imprimir el reporte", ex));
            }
        });
    }
}

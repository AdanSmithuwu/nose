package com.comercialvalerio.presentation.controller.reportes;

import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.reportes.FormReporteDiario;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import javax.swing.JOptionPane;

import javax.print.PrintException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para {@link FormReporteDiario}.
 */
public class ReporteDiarioController {

    private final FormReporteDiario view;
    private byte[] lastPdf;
    private LocalDate lastDate;
    private static final Logger LOG =
            Logger.getLogger(ReporteDiarioController.class.getName());

    public ReporteDiarioController(FormReporteDiario view) {
        this.view = view;
    }

    /** Indica si hay un PDF generado para la fecha actual. */
    public boolean isPdfAvailable() {
        LocalDate f = view.getSpFecha().getDate();
        return f != null && lastPdf != null && f.equals(lastDate);
    }

    /** Carga los datos de resumen para la fecha seleccionada. */
    public void loadData() {
        final LocalDate f = view.getSpFecha().getDate() == null ?
                LocalDate.now() : view.getSpFecha().getDate();
        view.getSpFecha().setDate(f);
        AsyncTasks.busy(view, () -> {
            var datos = UiContext.reporteSvc().datosDiario(f);
            var rep = UiContext.reporteSvc().buscarDiario(f);
            return new Object[]{datos, rep};
        }, obj -> {
            var dto = (com.comercialvalerio.application.dto.report.ResumenDiarioDto) obj[0];
            var rep = (com.comercialvalerio.application.dto.ReporteDto) obj[1];
            TableUtils.clearModel(view.getModel());
            view.getModel().addRow(new Object[]{
                    f.toString(), dto.numVentas(), dto.numPedidos(),
                    dto.montoBruto(), dto.montoNeto()});
            TableUtils.packColumns(view.getTable());
            TableUtils.updateEmptyView(view.getScroll(), view.getTable(), view.getLblEmpty());

            TableUtils.clearModel(view.getModelPagos());
            for (var p : dto.pagos()) {
                view.getModelPagos().addRow(new Object[]{p.metodo(), p.monto()});
            }
            TableUtils.packColumns(view.getTblPagos());
            TableUtils.updateEmptyView(view.getSpPagos(), view.getTblPagos(), view.getLblEmptyPagos());
            boolean existe = rep != null;
            view.getBtnGenerar().setText(existe ? "Actualizar" : "Generar");
            lastPdf = existe ? rep.pdf() : null;
            lastDate = existe ? f : null;
            view.updateButtons();
        });
    }

    /** Genera el reporte PDF y abre el archivo. */
    public void generar() {
        LocalDate f = view.getSpFecha().getDate();
        if (f == null) {
            String txt = view.getSpFecha().getText();
            try {
                f = java.time.LocalDate.parse(txt,
                        java.time.format.DateTimeFormatter.ofPattern(
                                com.comercialvalerio.presentation.ui.util.DateFormatUtils.getShortPattern()));
            } catch (Exception ignore) {
                // texto no válido
            }
        }
        if (f == null) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione una fecha válida",
                    "Fecha obligatoria",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        final LocalDate fecha = f;
        AsyncTasks.busy(view, () -> {
            var rep = UiContext.reporteSvc().guardarDiario(fecha);
            lastPdf = Base64.getDecoder().decode(rep.pdfBase64());
            lastDate = fecha;
            return null;
        }, r -> {
            view.getBtnPdf().setEnabled(true);
            view.getBtnPrint().setEnabled(true);
            view.getBtnGenerar().setText("Actualizar");
        });
    }

    /** Abre el archivo PDF generado previamente. */
    public void exportPdf() {
        if (lastPdf == null) {
            JOptionPane.showMessageDialog(view,
                    "Genere el reporte primero",
                    "Reporte no generado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        AsyncTasks.busy(view, () -> {
            if (lastPdf == null) {
                var rep = UiContext.reporteSvc().generarDiario(view.getSpFecha().getDate());
                lastPdf = Base64.getDecoder().decode(rep.pdfBase64());
                lastDate = view.getSpFecha().getDate();
            }
            return lastPdf;
        }, pdf -> {
            java.io.File dir = com.comercialvalerio.presentation.ui.util.UserPrefs.getPdfDirectory();
            javax.swing.JFileChooser fc = dir != null ? new javax.swing.JFileChooser(dir)
                    : new javax.swing.JFileChooser();
            fc.setSelectedFile(new java.io.File("reporte-diario.pdf"));
            if (fc.showSaveDialog(view) == javax.swing.JFileChooser.APPROVE_OPTION) {
                com.comercialvalerio.presentation.ui.util.UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    java.nio.file.Files.write(fc.getSelectedFile().toPath(), pdf);
                } catch (java.io.IOException ex) {
                    LOG.log(Level.SEVERE, "Error al guardar archivo", ex);
                    ErrorHandler.handle(new IllegalStateException("Error al guardar archivo", ex));
                }
            }
        });
    }

    /** Envía el último PDF generado a la impresora. */
    public void printPdf() {
        if (lastPdf == null) {
            JOptionPane.showMessageDialog(view,
                    "Genere el reporte primero",
                    "Reporte no generado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        AsyncTasks.busy(view, () -> {
            return lastPdf;
        }, pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                LOG.log(Level.SEVERE, "Error al imprimir el reporte", ex);
                ErrorHandler.handle(new IllegalStateException(
                        "Error al imprimir el reporte", ex));
            }
        });
    }
}

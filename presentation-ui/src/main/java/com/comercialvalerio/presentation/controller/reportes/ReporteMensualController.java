package com.comercialvalerio.presentation.controller.reportes;

import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.ui.reportes.FormReporteMensual;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.application.dto.report.ResumenMensualDto;

import javax.print.PrintException;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para {@link FormReporteMensual}.
 */
public class ReporteMensualController {

    private final FormReporteMensual view;
    private byte[] lastPdf;
    private static final Logger LOG =
            Logger.getLogger(ReporteMensualController.class.getName());

    public ReporteMensualController(FormReporteMensual view) {
        this.view = view;
    }

    /** Carga los datos de resumen mensual en la tabla. */
    public void loadData() {
        int anio = (Integer) view.getSpAnio().getValue();
        int mes  = (Integer) view.getSpMes().getValue();
        AsyncTasks.busy(view, () -> {
            var datos = UiContext.reporteSvc().datosMensual(anio, mes);
            ReporteDto rep = UiContext.reporteSvc().buscarMensual(anio, mes);
            return new Object[]{datos, rep};
        }, obj -> {
            ResumenMensualDto dto = (ResumenMensualDto) obj[0];
            ReporteDto rep = (ReporteDto) obj[1];
            TableUtils.clearModel(view.getModel());
            for (var d : dto.dias()) {
                LOG.log(Level.INFO,
                        "Valor fecha sin formatear: {0} ({1})",
                        new Object[]{d.fecha(),
                                d.fecha() == null ? "null"
                                        : d.fecha().getClass().getName()});
                String fecha = DateFormatUtils.formatServer(d.fecha());
                view.getModel().addRow(
                        new Object[]{fecha, d.numTransacciones(), d.monto()});
            }
            TableUtils.packColumns(view.getTable());
            TableUtils.updateEmptyView(
                    view.getScroll(),
                    view.getTable(),
                    view.getLblEmpty());

            TableUtils.clearModel(view.getModelCategoria());
            for (var c : dto.categorias()) {
                view.getModelCategoria().addRow(
                        new Object[]{c.categoria(), c.numTransacciones(),
                                NumberUtils.formatMinScale(c.ingresosCategoria(), 2)});
            }
            TableUtils.packColumns(view.getTblCategoria());
            TableUtils.updateEmptyView(
                    view.getSpCategoria(),
                    view.getTblCategoria(),
                    view.getLblEmptyCategoria());


            java.text.NumberFormat moneda = java.text.NumberFormat
                    .getCurrencyInstance(java.util.Locale.forLanguageTag("es-PE"));
            view.getLblMinorista().setText(
                    dto.numTransMinorista() + " / " + moneda.format(dto.montoMinorista()));
            view.getLblEspecial().setText(
                    dto.numTransEspecial() + " / " + moneda.format(dto.montoEspecial()));
            view.getLblDomicilio().setText(
                    dto.numPedidosDomicilio() + " / " + moneda.format(dto.montoPedidosDomicilio()));

            boolean existe = rep != null;
            view.getBtnGenerar().setText(existe ? "Actualizar" : "Generar");
            lastPdf = existe ? rep.pdf() : null;
            view.updateButtons(existe);
        });
    }

    /** Permite guardar el PDF generado previamente. */
    public void exportPdf() {
        int anio = (Integer) view.getSpAnio().getValue();
        int mes  = (Integer) view.getSpMes().getValue();
        AsyncTasks.busy(view, () -> {
            if (lastPdf == null) {
                var rep = UiContext.reporteSvc().generarMensual(anio, mes, true);
                lastPdf = Base64.getDecoder().decode(rep.pdfBase64());
            }
            return lastPdf;
        }, pdf -> {
            java.io.File dir = com.comercialvalerio.presentation.ui.util.UserPrefs.getPdfDirectory();
            javax.swing.JFileChooser fc = dir != null ? new javax.swing.JFileChooser(dir)
                    : new javax.swing.JFileChooser();
            fc.setSelectedFile(new java.io.File("reporte-mensual.pdf"));
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

    /** Genera o actualiza el PDF y habilita las acciones. */
    public void generar() {
        int anio = (Integer) view.getSpAnio().getValue();
        int mes  = (Integer) view.getSpMes().getValue();
        AsyncTasks.busy(view, () -> {
            var rep = UiContext.reporteSvc().guardarMensual(anio, mes, true);
            lastPdf = Base64.getDecoder().decode(rep.pdfBase64());
            return null;
        }, v -> {
            view.getBtnPdf().setEnabled(true);
            view.getBtnPrint().setEnabled(true);
            view.getBtnGenerar().setText("Actualizar");
            view.updateButtons(true);
        });
    }

    /** Imprime el último reporte generado. */
    public void printPdf() {
        int anio = (Integer) view.getSpAnio().getValue();
        int mes  = (Integer) view.getSpMes().getValue();
        AsyncTasks.busy(view, () -> {
            if (lastPdf == null) {
                var rep = UiContext.reporteSvc().generarMensual(anio, mes, true);
                lastPdf = Base64.getDecoder().decode(rep.pdfBase64());
            }
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

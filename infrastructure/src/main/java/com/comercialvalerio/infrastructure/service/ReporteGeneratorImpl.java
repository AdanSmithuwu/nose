package com.comercialvalerio.infrastructure.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ArrayList;

import com.comercialvalerio.domain.service.ReporteGenerator;
import com.comercialvalerio.infrastructure.transaction.TransactionManager;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import com.comercialvalerio.infrastructure.pdf.PdfDocumentHelper;
import com.comercialvalerio.domain.exception.PdfGenerationException;
import com.comercialvalerio.infrastructure.pdf.PdfUtilBase;
import com.comercialvalerio.common.MoneyUtils;
import com.comercialvalerio.infrastructure.service.report.PagoMetodoRow;
import com.comercialvalerio.infrastructure.service.report.TransaccionDiaRow;
import com.comercialvalerio.infrastructure.service.report.ResumenCategoriaRow;
import com.comercialvalerio.infrastructure.service.report.ResumenModalidadRow;
import com.comercialvalerio.infrastructure.service.report.ResumenDiaRow;
import com.comercialvalerio.infrastructure.service.report.RotacionRow;
import com.comercialvalerio.infrastructure.service.report.DiarioData;
import com.comercialvalerio.infrastructure.service.report.MensualData;
import com.comercialvalerio.infrastructure.service.report.PdfTableUtils;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import java.util.List;
import jakarta.persistence.StoredProcedureQuery;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementación simple de {@link ReporteGenerator} que crea PDFs de texto
 * utilizando la librería OpenPDF.
 */
@ApplicationScoped
public class ReporteGeneratorImpl implements ReporteGenerator {

    private static com.lowagie.text.Image buildChart(
            org.jfree.data.category.CategoryDataset data)
            throws BadElementException, IOException {
        var chart = org.jfree.chart.ChartFactory.createBarChart(
                "Ingresos diarios", "Día", "Monto", data,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                false, false, false);
        // imagen moderada para que no sobresalga del PDF
        java.awt.Image chartImg = chart.createBufferedImage(600, 400);
        com.lowagie.text.Image chartPdf =
                com.lowagie.text.Image.getInstance(chartImg, null);
        chartPdf.setAlignment(Element.ALIGN_CENTER);
        chartPdf.scaleToFit(500, 300);
        return chartPdf;
    }

    @Override
    public byte[] generarReporteDiario(LocalDate fecha) {
        if (fecha == null)
            throw new IllegalArgumentException("La fecha es obligatoria");
        DiarioData data = TransactionManager.runWithSession(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Reporte.generarDiario");
            sp.setParameter("fecha", java.sql.Date.valueOf(fecha));
            List<Object[]> rows = BaseRepository.resultList(sp, Object[].class);
            Object[] row = rows.isEmpty() ? null : rows.get(0);

            ResumenDiaRow resumen = row == null
                    ? new ResumenDiaRow(0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO)
                    : new ResumenDiaRow(
                            ((Number) row[1]).longValue(),
                            ((Number) row[2]).longValue(),
                            (BigDecimal) row[3],
                            (BigDecimal) row[4]);

            var pagos = row == null
                    ? java.util.List.<PagoMetodoRow>of()
                    : java.util.List.of(
                            new PagoMetodoRow("Efectivo", (BigDecimal) row[5]),
                            new PagoMetodoRow("Billetera Digital", (BigDecimal) row[6]));

            return new DiarioData(resumen, pagos);
        });

        ResumenDiaRow resumen = data.resumen();
        java.util.List<PagoMetodoRow> pagos = data.pagos();
        long numVentas = resumen.numTransacciones();
        long numPedidos = resumen.numPedidos();
        BigDecimal bruto = resumen.totalBruto();
        BigDecimal neto = resumen.totalNeto();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document doc = PdfDocumentHelper.openDocument(out)) {
            PdfUtilBase.startDocument(doc, "Reporte diario");
            doc.add(new Paragraph("Fecha: " + fecha.toString()));
            doc.add(new Paragraph(" "));

            doc.add(PdfTableUtils.buildResumenTable(numVentas, numPedidos, bruto, neto));

            doc.add(new Paragraph(" "));
            doc.add(PdfTableUtils.buildPagosTable(pagos));
        } catch (DocumentException ex) {
            throw new PdfGenerationException("Error generando reporte", ex);
        }
        return out.toByteArray();
    }

    @Override
    public byte[] generarReporteMensual(int anio, int mes, boolean incluirResumen) {
        if (mes < 1 || mes > 12)
            throw new IllegalArgumentException("mes debe estar entre 1 y 12");
        LocalDate d = LocalDate.of(anio, mes, 1);

        MensualData listas = TransactionManager.runWithSession(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Reporte.generarMensual");
            sp.setParameter("anio", anio);
            sp.setParameter("mes", mes);
            sp.setParameter("conResumen", incluirResumen);
            boolean more = sp.execute();

            java.util.List<TransaccionDiaRow> dias = new ArrayList<>();
            java.util.List<ResumenCategoriaRow> categorias = new ArrayList<>();
            ResumenModalidadRow resumen = null;

            if (more) {
                List<Object[]> rows = BaseRepository.resultList(sp, Object[].class);
                for (Object[] r : rows) {
                    dias.add(new TransaccionDiaRow(
                            ((java.sql.Date) r[0]).toLocalDate(),
                            ((Number) r[1]).longValue(),
                            (java.math.BigDecimal) r[2]));
                }
                more = sp.hasMoreResults();
            }

            if (more) {
                List<Object[]> rows = BaseRepository.resultList(sp, Object[].class);
                for (Object[] r : rows) {
                    categorias.add(new ResumenCategoriaRow(
                            (String) r[0],
                            ((Number) r[1]).longValue(),
                            (java.math.BigDecimal) r[2]));
                }
                more = sp.hasMoreResults();
            }

            if (incluirResumen && more) {
                List<Object[]> rows = BaseRepository.resultList(sp, Object[].class);
                if (!rows.isEmpty()) {
                    Object[] r = rows.get(0);
                    resumen = new ResumenModalidadRow(
                            ((Number) r[0]).longValue(), (java.math.BigDecimal) r[1],
                            ((Number) r[2]).longValue(), (java.math.BigDecimal) r[3],
                            ((Number) r[4]).longValue(), (java.math.BigDecimal) r[5]);
                }
            }

            return new MensualData(dias, categorias, resumen);
        });

        var dias = listas.dias();
        var categorias = listas.categorias();
        ResumenModalidadRow resumen = listas.resumen();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document doc = PdfDocumentHelper.openDocument(out)) {
            PdfUtilBase.startDocument(doc, "Reporte mensual");
            String periodo = d.getMonth().getDisplayName(TextStyle.FULL,
                    Locale.of("es", "ES")) + " " + anio;
            doc.add(new Paragraph("Periodo: " + periodo));
            doc.add(new Paragraph(" "));
            PdfPTable tbl = new PdfPTable(3);
            tbl.setWidthPercentage(100);
            tbl.addCell(PdfUtilBase.headerCell("Día"));
            tbl.addCell(PdfUtilBase.headerCell("Transacciones"));
            tbl.addCell(PdfUtilBase.headerCell("Monto"));
            var ingresosDataset = new org.jfree.data.category.DefaultCategoryDataset();
            for (TransaccionDiaRow r : dias) {
                LocalDate dia = r.dia();
                tbl.addCell(dia.toString());
                tbl.addCell(Long.toString(r.numTransacciones()));
                tbl.addCell(r.ingresos().toString());
                ingresosDataset.addValue(r.ingresos(), "Ingresos", Integer.toString(dia.getDayOfMonth()));
            }
            doc.add(tbl);

            doc.add(buildChart(ingresosDataset));
            doc.add(new Paragraph(" "));

            PdfPTable catTbl = new PdfPTable(3);
            catTbl.setWidthPercentage(100);
            catTbl.addCell(PdfUtilBase.headerCell("Categoría"));
            catTbl.addCell(PdfUtilBase.headerCell("Transacciones"));
            catTbl.addCell(PdfUtilBase.headerCell("Ingresos"));
            for (ResumenCategoriaRow c : categorias) {
                catTbl.addCell(c.categoria());
                catTbl.addCell(Long.toString(c.numTransacciones()));
                catTbl.addCell(c.ingresos().toString());
            }
            doc.add(catTbl);

            if (incluirResumen && resumen != null) {
                doc.add(new Paragraph(" "));
                Paragraph subtitulo = new Paragraph("Resumen por modalidad",
                        PdfDocumentHelper.titleFont());
                subtitulo.setAlignment(Element.ALIGN_CENTER);
                doc.add(subtitulo);
                doc.add(new Paragraph(" "));
                PdfPTable t = new PdfPTable(3);
                t.setWidthPercentage(80);
                t.addCell(PdfUtilBase.headerCell("Ventas completadas"));
                t.addCell(PdfUtilBase.headerCell("Pedidos entregados"));
                t.addCell(PdfUtilBase.headerCell("Pedidos especiales"));
                t.addCell(resumen.numTransMinorista() + " - "
                        + MoneyUtils.format(resumen.montoMinorista()));
                t.addCell(resumen.numPedidosDomicilio() + " - "
                        + MoneyUtils.format(resumen.montoPedidosDomicilio()));
                t.addCell(resumen.numTransEspecial() + " - "
                        + MoneyUtils.format(resumen.montoEspecial()));
                doc.add(t);
            }
        } catch (DocumentException | IOException ex) {
            throw new PdfGenerationException("Error generando reporte", ex);
        }
        return out.toByteArray();
    }

    @Override
    public byte[] generarReporteRotacion(LocalDate desde, LocalDate hasta, Integer top) {
        if (desde == null || hasta == null)
            throw new IllegalArgumentException("Las fechas 'desde' y 'hasta' son obligatorias");
        if (hasta.isBefore(desde))
            throw new IllegalArgumentException("El rango 'desde' no puede ser posterior a 'hasta'");

        java.util.List<RotacionRow> datos = TransactionManager.runWithSession(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Reporte.generarRotacion");
            sp.setParameter("desde", desde.atStartOfDay());
            sp.setParameter("hasta", hasta.atStartOfDay());
            sp.setParameter("top", top);
            List<Object[]> rows = BaseRepository.resultList(sp, Object[].class);
            return rows.stream()
                    .map(r -> new RotacionRow(
                            ((Number) r[0]).intValue(),
                            (String) r[2],
                            (String) r[3],
                            (BigDecimal) r[4],
                            (BigDecimal) r[5]))
                    .toList();
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document doc = PdfDocumentHelper.openDocument(out)) {
            PdfUtilBase.startDocument(doc, "Reporte rotacion");
            String periodo = "Desde " + desde.toString() + " hasta " + hasta.minusDays(1).toString();
            doc.add(new Paragraph("Periodo: " + periodo));
            doc.add(new Paragraph(" "));
            PdfPTable tbl = new PdfPTable(5);
            tbl.setWidthPercentage(100);
            tbl.addCell(PdfUtilBase.headerCell("#"));
            tbl.addCell(PdfUtilBase.headerCell("Producto"));
            tbl.addCell(PdfUtilBase.headerCell("Categoría"));
            tbl.addCell(PdfUtilBase.headerCell("Unidades"));
            tbl.addCell(PdfUtilBase.headerCell("Importe total"));
            for (RotacionRow r : datos) {
                tbl.addCell(Integer.toString(r.posicion()));
                tbl.addCell(r.producto());
                tbl.addCell(r.categoria());
                tbl.addCell(r.unidades().toString());
                tbl.addCell(r.importe().toString());
            }
            doc.add(tbl);
        } catch (DocumentException ex) {
            throw new PdfGenerationException("Error generando reporte", ex);
        }
        return out.toByteArray();
    }

}

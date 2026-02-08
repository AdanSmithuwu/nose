package com.comercialvalerio.presentation.controller.dashboard;

import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.dashboard.FormDashboard;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.util.NumberUtils;

import java.util.Objects;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Controlador para {@link FormDashboard}. */
public class DashboardController {

    private static final BigDecimal META_VENTAS = new BigDecimal("10000");
    private static final BigDecimal META_PEDIDOS = new BigDecimal("100");
    private static final Logger LOG =
            Logger.getLogger(DashboardController.class.getName());

    private final FormDashboard view;

    public DashboardController(FormDashboard view) {
        this.view = view;
    }

    /** Carga los valores de métricas de forma asíncrona. */
    public void loadMetrics() {
        AsyncTasks.busy(view, UiContext.dashboardSvc()::indicadores, datos -> {
            NumberFormat moneda = NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-PE"));

            int progVentas = datos.totalVentas()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(META_VENTAS, 0, RoundingMode.DOWN)
                    .intValue();

            int progPed = BigDecimal.valueOf(datos.totalPedidos())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(META_PEDIDOS, 0, RoundingMode.DOWN)
                    .intValue();

            progVentas = Math.max(0, Math.min(100, progVentas));
            progPed = Math.max(0, Math.min(100, progPed));

            view.getLblVentasValue().setText(moneda.format(datos.totalVentas()));
            view.getBarVentas().setValue(progVentas);
            view.getBarVentas().setString(progVentas + "%");
            view.getLblVentasFoot().setText("Meta mensual: " +
                    moneda.format(META_VENTAS) + " (" + progVentas + "%)");

            view.getLblPedidosValue().setText("N\u00ba " + datos.totalPedidos());
            view.getBarPedidos().setValue(progPed);
            view.getBarPedidos().setString(progPed + "%");
            view.getLblPedidosFoot().setText("Meta mensual: " +
                    META_PEDIDOS + " (" + progPed + "%)");
        });
    }

    /** Carga los datos de la tabla de más vendidos. */
    public void loadBestSellers() {
        AsyncTasks.busy(view.getSpBestSeller(),
                () -> UiContext.productoSvc().listarMasVendidos(5),
                lista -> {
                    TableUtils.clearModel(view.getBestSellerModel());
                    for (var p : lista) {
                        view.getBestSellerModel().addRow(new Object[]{
                                p.nombre(),
                                NumberUtils.formatPlain(p.unidadesVendidas())});
                    }
                    TableUtils.packColumns(view.getTblBestSeller());
                    TableUtils.updateEmptyView(view.getSpBestSeller(),
                            view.getTblBestSeller(), view.getLblEmptyBest());
                });
    }

    /** Carga los datos de la tabla de clientes frecuentes. */
    public void loadClientes() {
        AsyncTasks.busy(view.getSpClientes(), () -> {
            var base = UiContext.dashboardSvc().clientesFrecuentes(5);
            try {
                var p = UiContext.parametroSistemaSvc().obtener("ID_CLIENTE_GENERICO");
                int idGen = p.valor().intValue();
                return base.stream()
                        .filter(c -> !Objects.equals(c.idCliente(), idGen))
                        .toList();
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "No se pudo obtener ID_CLIENTE_GENERICO", ex);
                return base;
            }
        }, lista -> {
            TableUtils.clearModel(view.getClientesModel());
            for (var c : lista) {
                view.getClientesModel().addRow(new Object[]{
                        c.nombre(), c.numCompras()});
            }
            TableUtils.packColumns(view.getTblClientes());
            TableUtils.updateEmptyView(view.getSpClientes(),
                    view.getTblClientes(), view.getLblEmptyClientes());
        });
    }

    /** Carga los datos de la tabla de pedidos pendientes. */
    public void loadPendientes() {
        AsyncTasks.busy(view.getSpPendientes(), UiContext.pedidoSvc()::listarPendientes,
                lista -> {
                    TableUtils.clearModel(view.getPendientesModel());
                    for (var p : lista) {
                        Object f = p.fecha();
                        String fecha = DateFormatUtils.formatServer(f);
                        view.getPendientesModel().addRow(new Object[]{
                                p.idTransaccion(), p.clienteNombre(), fecha});
                    }
                    TableUtils.packColumns(view.getTblPendientes());
                    TableUtils.updateEmptyView(view.getSpPendientes(),
                            view.getTblPendientes(), view.getLblEmptyPendientes());
                });
    }

    /** Recarga todos los datos del panel. */
    public void refresh() {
        loadMetrics();
        loadBestSellers();
        loadClientes();
        loadPendientes();
    }
}

package com.comercialvalerio.presentation.controller.pedidos;

import com.comercialvalerio.application.dto.DetalleCreateDto;
import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.pedidos.DlgPedidoEditar;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.util.PriceUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import com.comercialvalerio.application.dto.TipoPedido;
import jakarta.ws.rs.WebApplicationException;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador usado por {@link DlgPedidoEditar}. */
public class PedidoEditarController {

    private final DlgPedidoEditar view;
    private final PedidoDto pedido;
    private final List<DetalleCreateDto> detalles = new ArrayList<>();
    private List<ProductoDto> productos = List.of();
    private final Map<Integer, ProductoDto> productoCache = new HashMap<>();
    /** Stock original de los productos listados. */
    private final Map<Integer, BigDecimal> stockInicial = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(PedidoEditarController.class.getName());
    private final boolean domicilio;
    private final int minHilo;
    private final BigDecimal cargoReparto;
    private final BigDecimal descValeGas;

    public PedidoEditarController(DlgPedidoEditar view, PedidoDto pedido) {
        this.view = view;
        this.pedido = pedido;
        this.domicilio = TipoPedido.DOMICILIO.equals(pedido.tipoPedido());
        int m = DbConstraints.MIN_CANTIDAD_MAYORISTA_HILO;
        BigDecimal cargo = BigDecimal.ZERO;
        BigDecimal desc  = BigDecimal.ZERO;
        try {
            var p = UiContext.parametroSistemaSvc().obtener("MIN_CANTIDAD_MAYORISTA_HILO");
            m = p.valor().intValue();
            cargo = UiContext.parametroSistemaSvc().obtener("CARGO_REPARTO").valor();
            desc  = UiContext.parametroSistemaSvc().obtener("DESCUENTO_VALE_GAS").valor();
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener parámetros del sistema", ex);
            ErrorHandler.handle(ex);
            // valores predeterminados
        }
        this.minHilo = m;
        this.cargoReparto = cargo;
        this.descValeGas  = desc;
    }

    /** Carga productos según el texto de búsqueda. */
    public void cargarProductos() {
        SwingUtilities.invokeLater(() -> {
            try {
                String q = view.getTxtBuscar().getText().trim();
                productoCache.clear();
                stockInicial.clear();
                var lista = domicilio
                        ? UiContext.productoSvc().listarParaPedido(q.isBlank() ? null : q,
                                TipoPedido.DOMICILIO)
                        : UiContext.productoSvc().listarParaPedido(q.isBlank() ? null : q,
                                TipoPedido.ESPECIAL);
                productos = lista;
                DefaultTableModel m = new DefaultTableModel(
                        new String[]{"ID","Producto","Stock","Precio"},0);
                for (var p : lista) {
                    productoCache.put(p.idProducto(), p);
                    stockInicial.put(p.idProducto(), p.stockActual());
                    if (p.paraPedido() && "Activo".equalsIgnoreCase(p.estado())) {
                        var precio = domicilio
                                ? p.precioUnitario()
                                : (p.precioMayorista() != null
                                    ? p.precioMayorista()
                                    : p.precioUnitario());
                        m.addRow(new Object[]{p.idProducto(), p.nombre(), fmt(p.stockActual()), precio});
                    }
                }
                view.getTblStock().setModel(m);
                if (view.getTblStock().getColumnCount() > 0) {
                    view.getTblStock().getColumnModel().removeColumn(
                            view.getTblStock().getColumnModel().getColumn(0));
                }
                TableRowSorter<DefaultTableModel> sorterStock = new TableRowSorter<>(m);
                sorterStock.setSortable(0, false);
                TableUtils.setNumericComparators(sorterStock, 2, 3);
                view.getTblStock().setRowSorter(sorterStock);
                TableUtils.packColumns(view.getTblStock());
                TableUtils.updateEmptyView(
                        view.getSpStock(),
                        view.getTblStock(),
                        view.getLblEmptyStock());
            } catch (WebApplicationException ex) {
                LOG.log(Level.SEVERE, "Error al cargar productos", ex);
                ErrorHandler.handle(ex);
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al cargar productos", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Carga los detalles existentes del pedido en la tabla. */
    public void cargarDetalles() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<DetalleDto> det = UiContext.detalleSvc().listar(pedido.idTransaccion());
                detalles.clear();
                view.getLblTotalOvillos().setText("0");
                for (DetalleDto d : det) {
                    detalles.add(new DetalleCreateDto(
                            d.idProducto(), d.idTallaStock(), d.cantidad(), d.precioUnitario()));
                }
                view.getChkValeGas().setSelected(pedido.usaValeGas());
                refrescarTabla();
            } catch (WebApplicationException ex) {
                LOG.log(Level.SEVERE, "Error al cargar detalles", ex);
                ErrorHandler.handle(ex);
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al cargar detalles", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    public void agregarDetalle() {
        int row = view.getTblStock().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un producto",
                    "Producto no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblStock().convertRowIndexToModel(row);
        Integer prodId = (Integer) view.getTblStock().getModel().getValueAt(modelRow,0);
        ProductoDto prod = productos.stream()
                .filter(p -> p.idProducto().equals(prodId))
                .findFirst().orElse(null);
        if (prod == null) return;
        if (!domicilio && !esHilo(prod)) {
            JOptionPane.showMessageDialog(view,
                    "Solo puede añadir ovillos de hilo",
                    "Producto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BigDecimal cantidad;
        String txt = view.getTxtCantidad().getText().trim();
        if (txt.isBlank()) {
            if (domicilio) cantidad = BigDecimal.ONE;
            else if (esAguaOGas(prod)) cantidad = BigDecimal.ONE;
            else if (esHilo(prod)) cantidad = BigDecimal.ONE;
            else {
                JOptionPane.showMessageDialog(view,
                        "Cantidad inválida",
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            try { cantidad = new BigDecimal(txt); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view,
                        "Cantidad inválida",
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                ErrorHandler.handle(ex);
                return;
            }
        }
        if (cantidad.scale() > 0) {
            JOptionPane.showMessageDialog(view,
                    "Cantidad debe ser un número entero",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cantidad.compareTo(BigDecimal.ONE) < 0) {
            JOptionPane.showMessageDialog(view,
                    "Cantidad debe ser mayor que 0",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cantidad.compareTo(BigDecimal.valueOf(DbConstraints.MAX_CANTIDAD)) > 0) {
            JOptionPane.showMessageDialog(view,
                    "Cantidad máxima " + DbConstraints.MAX_CANTIDAD,
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((domicilio || esAguaOGas(prod)) && prod.stockActual() != null
                && cantidad.compareTo(prod.stockActual()) > 0) {
            JOptionPane.showMessageDialog(view,
                    "Stock insuficiente",
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal precio = calcularPrecio(prod, cantidad);

        for (int i = 0; i < detalles.size(); i++) {
            var d = detalles.get(i);
            if (d.idProducto().equals(prodId)) {
                BigDecimal nueva = d.cantidad().add(cantidad);
                if (nueva.compareTo(BigDecimal.valueOf(DbConstraints.MAX_CANTIDAD)) > 0) {
                    JOptionPane.showMessageDialog(view,
                            "Cantidad máxima " + DbConstraints.MAX_CANTIDAD,
                            "Dato inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                precio = calcularPrecio(prod, nueva);
                detalles.set(i, new DetalleCreateDto(prodId, null, nueva, precio));
                refrescarTabla();
                updateStock(modelRow, cantidad.negate());
                return;
            }
        }

        precio = calcularPrecio(prod, cantidad);
        detalles.add(new DetalleCreateDto(prodId, null, cantidad, precio));
        refrescarTabla();
        updateStock(modelRow, cantidad.negate());
    }

    public void quitarDetalle() {
        int row = view.getTblAdded().getSelectedRow();
        if (row < 0 || row >= detalles.size()) return;
        DetalleCreateDto d = detalles.remove(row);
        restoreStock(d.idProducto(), d.cantidad());
        refrescarTabla();
    }

    private void refrescarTabla() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID","Producto","Cantidad","P.Unit","Total"},0);
        BigDecimal sub = BigDecimal.ZERO;
        int totalHilos = 0;
        for (DetalleCreateDto d : detalles) {
            ProductoDto prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            BigDecimal tot = d.cantidad().multiply(d.precioUnitario());
            sub = sub.add(tot);
            m.addRow(new Object[]{
                    d.idProducto(),
                    prod.nombre(),
                    NumberUtils.formatPlain(d.cantidad()),
                    NumberUtils.formatScale(d.precioUnitario(), DbConstraints.PRECIO_SCALE),
                    NumberUtils.formatScale(tot, DbConstraints.PRECIO_SCALE)
            });
            if (esHilo(prod)) totalHilos += d.cantidad().intValue();
        }
        view.getTblAdded().setModel(m);
        if (view.getTblAdded().getColumnCount() > 0) {
            view.getTblAdded().getColumnModel().removeColumn(
                    view.getTblAdded().getColumnModel().getColumn(0));
        }
        TableRowSorter<DefaultTableModel> sorterAdd = new TableRowSorter<>(m);
        sorterAdd.setSortable(0, false);
        TableUtils.setNumericComparators(sorterAdd, 2, 3, 4);
        view.getTblAdded().setRowSorter(sorterAdd);
        TableUtils.packColumns(view.getTblAdded());
        TableUtils.updateEmptyView(
                view.getSpAdded(),
                view.getTblAdded(),
                view.getLblEmptyAdded());
        boolean hasGas = false;
        for (DetalleCreateDto d : detalles) {
            var prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            if (esGas(prod)) { hasGas = true; break; }
        }
        view.getChkValeGas().setVisible(hasGas);
        if (!hasGas) view.getChkValeGas().setSelected(false);

        BigDecimal cargo = domicilio ? cargoReparto : BigDecimal.ZERO;
        view.getLblSubTotal().setText(CurrencyUtils.format(sub));
        view.getLblCargo().setText(CurrencyUtils.format(cargo));
        view.getLblTotalOvillos().setText(String.valueOf(totalHilos));
        BigDecimal total = sub.add(cargo);
        if (view.getChkValeGas().isSelected()) {
            total = total.subtract(descValeGas);
        }
        view.getLblTotal().setText(CurrencyUtils.format(total));
    }

    /** Método público para actualizar totales desde la vista. */
    public void refrescarTotales() { refrescarTabla(); }

    private boolean esAguaOGas(ProductoDto p) {
        String n = p.nombre().toLowerCase(Locale.ROOT);
        return n.contains("agua") || n.contains("gas");
    }
    private boolean esGas(ProductoDto p) {
        return p.nombre().toLowerCase(Locale.ROOT).contains("gas");
    }
    private boolean esHilo(ProductoDto p) { return p.nombre().toLowerCase(Locale.ROOT).contains("ovillo de hilo"); }

    private BigDecimal calcularPrecio(ProductoDto prod, BigDecimal cantidad) {
        return domicilio
                ? PriceUtils.precioParaCantidad(prod, cantidad)
                : PriceUtils.precioMayorista(prod);
    }

    /** Envía la solicitud de actualización al servicio REST. */
    public void guardar() {
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Agregue al menos un producto",
                    "Pedido sin productos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int totalHilos = 0;
        for (DetalleCreateDto d : detalles) {
            var prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            if (esHilo(prod)) totalHilos += d.cantidad().intValue();
        }
        if (!domicilio && totalHilos < minHilo) {
            JOptionPane.showMessageDialog(view,
                    "Debe alcanzar el mínimo mayorista",
                    "Cantidad insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal sub = BigDecimal.ZERO;
            for (DetalleCreateDto d : detalles) {
                sub = sub.add(d.cantidad().multiply(d.precioUnitario()));
            }
            sub = sub.setScale(DbConstraints.PRECIO_SCALE, java.math.RoundingMode.HALF_UP);
            BigDecimal desc = view.getChkValeGas().isSelected() ? descValeGas : BigDecimal.ZERO;
            desc = desc.setScale(DbConstraints.PRECIO_SCALE, java.math.RoundingMode.HALF_UP);
            BigDecimal cargo = domicilio ? cargoReparto : BigDecimal.ZERO;
            cargo = cargo.setScale(DbConstraints.PRECIO_SCALE, java.math.RoundingMode.HALF_UP);
            BigDecimal total = sub.add(cargo).subtract(desc)
                                 .setScale(DbConstraints.PRECIO_SCALE,
                                           java.math.RoundingMode.HALF_UP);
            PedidoCreateDto dto = new PedidoCreateDto(
                    sub, desc, cargo, total,
                    null,
                    pedido.empleadoId(), pedido.clienteId(),
                    pedido.direccionEntrega(), pedido.tipoPedido(),
                    view.getChkValeGas().isSelected(),
                    detalles
            );
            UiContext.pedidoSvc().actualizar(pedido.idTransaccion(), dto);
            JOptionPane.showMessageDialog(view,
                    domicilio ? "Pedido actualizado" : "Pedido Especial actualizado",
                    "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);
            view.dispose();
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al actualizar pedido", ex);
            ErrorHandler.handle(ex);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al actualizar pedido", ex);
            ErrorHandler.handle(ex);
        }
    }

    private String fmt(BigDecimal b) { return NumberUtils.formatPlain(b); }

    private void updateStock(int modelRow, BigDecimal delta) {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        Object val = tm.getValueAt(modelRow, 2);
        if (val == null || val.toString().isBlank()) return;
        try {
            BigDecimal s = new BigDecimal(val.toString()).add(delta);
            if (s.compareTo(BigDecimal.ZERO) < 0) s = BigDecimal.ZERO;
            if (!domicilio) {
                Integer id = (Integer) tm.getValueAt(modelRow, 0);
                BigDecimal max = stockInicial.get(id);
                if (max != null && s.compareTo(max) > 0) s = max;
            }
            tm.setValueAt(NumberUtils.formatPlain(s), modelRow, 2);
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Stock inválido", ex);
        }
    }

    private void restoreStock(Integer prodId, BigDecimal cantidad) {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        for (int i = 0; i < tm.getRowCount(); i++) {
            Integer id = (Integer) tm.getValueAt(i, 0);
            if (id != null && id.equals(prodId)) {
                updateStock(i, cantidad);
                break;
            }
        }
    }

}

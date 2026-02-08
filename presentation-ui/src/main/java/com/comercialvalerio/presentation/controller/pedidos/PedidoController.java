package com.comercialvalerio.presentation.controller.pedidos;

import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.DetalleCreateDto;
import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.ui.clientes.DlgClienteNuevo;
import com.comercialvalerio.presentation.ui.common.DlgObservacion;
import com.comercialvalerio.presentation.ui.pedidos.FormPedido;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.TableModelUtils;
import com.comercialvalerio.presentation.util.PriceUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import java.util.Locale;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.application.dto.TipoPedido;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador para {@link FormPedido}. */
public class PedidoController {

    private static final Logger LOG = Logger.getLogger(PedidoController.class.getName());

    protected final FormPedido view;
    protected final boolean domicilio;
    protected final List<DetalleCreateDto> detalles = new ArrayList<>();
    protected List<ClienteDto> clientes = List.of();
    protected String observacion;
    protected final Map<Integer, ProductoDto> productoCache = new HashMap<>();
    /** Stock original de los productos listados, usado solo en pedidos especiales. */
    protected final Map<Integer, java.math.BigDecimal> stockInicial = new HashMap<>();
    protected final int minHilo;
    protected final BigDecimal cargoReparto;
    protected final BigDecimal descValeGas;

    public PedidoController(FormPedido view) { this(view, false); }

    public PedidoController(FormPedido view, boolean domicilio) {
        this.view = view;
        this.domicilio = domicilio;
        int m = DbConstraints.MIN_CANTIDAD_MAYORISTA_HILO;
        BigDecimal cargo = BigDecimal.ZERO;
        BigDecimal desc  = BigDecimal.ZERO;
        try {
            var p = UiContext.parametroSistemaSvc().obtener("MIN_CANTIDAD_MAYORISTA_HILO");
            m = p.valor().intValue();
            cargo = UiContext.parametroSistemaSvc().obtener("CARGO_REPARTO").valor();
            desc  = UiContext.parametroSistemaSvc().obtener("DESCUENTO_VALE_GAS").valor();
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Error cargando parámetros del sistema", ex);
            ErrorHandler.handle(ex);
        }
        this.minHilo     = m;
        this.cargoReparto = cargo;
        this.descValeGas  = desc;
    }

    /** Indica si debe aplicarse el cargo de reparto. */
    protected boolean aplicaCargo() { return domicilio; }

    /** Carga la lista de clientes en la tabla y limpia la selección. */
    public void cargarClientes() {
        AsyncTasks.busy(view, () -> {
            clientes = UiContext.clienteSvc().listarActivos().stream()
                    .filter(c -> !"00000000".equals(c.dni()))
                    .toList();
            DefaultTableModel m = TableModelUtils.createModel(
                    view.getTblClientes(),
                    new String[]{"ID","Nombre","Teléfono","Dirección"},
                    new int[]{}, 0);
            for (ClienteDto c : clientes) {
                m.addRow(new Object[]{c.idPersona(), c.nombreCompleto(), c.telefono(), c.direccion()});
            }
            return m;
        }, m -> {
            TableUtils.packColumns(view.getTblClientes());
            TableUtils.updateEmptyView(
                    view.getSpClientes(),
                    view.getTblClientes(),
                    view.getLblEmptyClientes());
        });
    }

    /** Carga la lista de productos en la tabla de stock. */
    public void cargarProductos() {
        AsyncTasks.busy(view, () -> {
            String q = view.getTxtBuscar().getText().trim();
            productoCache.clear();
            stockInicial.clear();
            var lista = domicilio
                    ? UiContext.productoSvc().listarParaPedido(q.isBlank() ? null : q,
                            TipoPedido.DOMICILIO)
                    : UiContext.productoSvc().listarParaPedido(q.isBlank() ? null : q,
                            TipoPedido.ESPECIAL);
            DefaultTableModel m = TableModelUtils.createModel(
                    view.getTblStock(),
                    new String[]{"ID","Producto","Stock","Precio"},
                    new int[]{2,3}, 0);
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
            return m;
        }, m -> {
            TableUtils.packColumns(view.getTblStock());
            TableUtils.updateEmptyView(
                    view.getSpStock(),
                    view.getTblStock(),
                    view.getLblEmptyStock());
            aplicarStockLocal();
        });
    }

    /** Agrega el producto seleccionado al pedido usando la cantidad y opciones de la vista. */
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
        ProductoDto prod = productoCache.computeIfAbsent(
                prodId, id -> UiContext.productoSvc().obtener(id));
        if (!esHilo(prod)) {
            JOptionPane.showMessageDialog(view,
                    "Solo puede añadir ovillos de hilo",
                    "Producto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BigDecimal cantidad;
        String txt = view.getTxtCantidad().getText().trim();
        if (txt.isBlank()) {
            try {
                cantidad = cantidadPorDefecto(prod);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(view,
                        "Cantidad inválida",
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                ErrorHandler.handle(ex);
                return;
            }
        } else {
            try { cantidad = new BigDecimal(txt); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view,
                        "Cantidad inválida",
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                ErrorHandler.handle(new IllegalArgumentException("Cantidad inválida", ex));
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
        if (esAguaOGas(prod) && prod.stockActual() != null && cantidad.compareTo(prod.stockActual()) > 0) {
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

    void refrescarTabla() {
        DefaultTableModel m = TableModelUtils.createModel(
                view.getTblAdded(),
                new String[]{"ID","Producto","Cantidad","P.Unit","Total"},
                new int[]{2,3,4}, 0);
        BigDecimal sub = BigDecimal.ZERO;
        int totalHilos = 0;
        for (DetalleCreateDto d : detalles) {
            var prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            BigDecimal tot = d.cantidad().multiply(d.precioUnitario());
            sub = sub.add(tot);
            m.addRow(new Object[]{d.idProducto(), prod.nombre(), d.cantidad(), d.precioUnitario(), tot});
            if (esHilo(prod)) totalHilos += d.cantidad().intValue();
        }
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
        if (!hasGas) {
            view.getChkValeGas().setSelected(false);
        }

        BigDecimal cargo = aplicaCargo() ? cargoReparto : BigDecimal.ZERO;
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

    public void quitarDetalle() {
        int row = view.getTblAdded().getSelectedRow();
        if (row < 0 || row >= detalles.size()) return;
        DetalleCreateDto d = detalles.remove(row);
        restoreStock(d.idProducto(), d.cantidad());
        refrescarTabla();
    }

    public void cancelar() {
        detalles.clear();
        productoCache.clear();
        TableUtils.clearModel((DefaultTableModel) view.getTblAdded().getModel());
        TableUtils.updateEmptyView(
                view.getSpAdded(),
                view.getTblAdded(),
                view.getLblEmptyAdded());
        view.getTxtBuscar().setText("");
        view.getTxtCantidad().setText("");
        view.getTblStock().clearSelection();
        view.getTblClientes().clearSelection();
        view.getTxtCliNom().setText("");
        view.getTxtCliTel().setText("");
        view.getTxtCliDir().setText("");
        observacion = null;
        view.getChkValeGas().setSelected(false);
        view.getChkValeGas().setVisible(false);
        cargarProductos();
        refrescarTabla();
    }

    /**
     * Limpia el formulario tras registrar el pedido.
     */
    protected void limpiarPostCrear() {
        cancelar();
    }

    public void seleccionarProducto() {
        int row = view.getTblStock().getSelectedRow();
        if (row < 0) return;
        int modelRow2 = view.getTblStock().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblStock().getModel().getValueAt(modelRow2,0);
        ProductoDto p = productoCache.computeIfAbsent(
                id, i -> UiContext.productoSvc().obtener(i));
        view.getTxtCantidad().setText(prefillCantidad(p));
    }

    private boolean esAguaOGas(ProductoDto p) {
        String n = p.nombre().toLowerCase(Locale.ROOT);
        return n.contains("agua") || n.contains("gas");
    }

    private boolean esGas(ProductoDto p) {
        return p.nombre().toLowerCase(Locale.ROOT).contains("gas");
    }

    private boolean esHilo(ProductoDto p) {
        return p.nombre().toLowerCase(Locale.ROOT).contains("ovillo de hilo");
    }

    /**
     * Calcula el precio unitario a usar según el producto y la cantidad.
     * La implementación base considera el mínimo mayorista configurado.
     */
    protected BigDecimal calcularPrecio(ProductoDto prod, BigDecimal cantidad) {
        return PriceUtils.precioParaCantidad(prod, cantidad);
    }

    /** Devuelve la cantidad por defecto cuando no se ingresa valor. */
    protected BigDecimal cantidadPorDefecto(ProductoDto p) {
        if (esAguaOGas(p)) return BigDecimal.ONE;
        if (esHilo(p)) return new BigDecimal(minHilo);
        throw new IllegalArgumentException("No default quantity");
    }

    /** Valor usado para rellenar el campo cantidad al seleccionar un producto. */
    protected String prefillCantidad(ProductoDto p) {
        if (esAguaOGas(p)) return "1";
        if (esHilo(p)) return String.valueOf(minHilo);
        return "";
    }

    /** Crea el pedido enviando una lista de pagos vacía. */
    public void crear() {
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Agregue al menos un producto",
                    "Pedido sin productos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rowCli = view.getTblClientes().getSelectedRow();
        if (rowCli < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un cliente",
                    "Cliente no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRowCli = view.getTblClientes().convertRowIndexToModel(rowCli);
        Integer idCli = (Integer) view.getTblClientes().getModel().getValueAt(modelRowCli, 0);
        ClienteDto cli = clientes.stream()
                .filter(c -> c.idPersona().equals(idCli))
                .findFirst().orElse(null);
        if (cli == null) {
            JOptionPane.showMessageDialog(view,
                    "Cliente inválido",
                    "Cliente inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dir = cli.direccion();
        if (dir == null || dir.isBlank()) {
            JOptionPane.showMessageDialog(view,
                    "El cliente no tiene dirección registrada",
                    "Cliente sin dirección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalHilos = 0;
        for (DetalleCreateDto d : detalles) {
            var prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            if (esHilo(prod)) totalHilos += d.cantidad().intValue();
        }
        if (totalHilos < minHilo) {
            JOptionPane.showMessageDialog(view,
                    "Debe alcanzar el mínimo mayorista",
                    "Cantidad insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AsyncTasks.busy(view, () -> {
            Integer emp = UiContext.getUsuarioActual() == null ? null
                    : UiContext.getUsuarioActual().idPersona();
            BigDecimal sub = BigDecimal.ZERO;
            for (DetalleCreateDto d : detalles) {
                sub = sub.add(d.cantidad().multiply(d.precioUnitario()));
            }
            sub = sub.setScale(DbConstraints.PRECIO_SCALE,
                               java.math.RoundingMode.HALF_UP);
            BigDecimal desc = view.getChkValeGas().isSelected()
                    ? descValeGas
                    : BigDecimal.ZERO;
            desc = desc.setScale(DbConstraints.PRECIO_SCALE,
                                 java.math.RoundingMode.HALF_UP);
            BigDecimal cargo = aplicaCargo() ? cargoReparto : BigDecimal.ZERO;
            cargo = cargo.setScale(DbConstraints.PRECIO_SCALE,
                                   java.math.RoundingMode.HALF_UP);
            BigDecimal total = sub.add(cargo).subtract(desc)
                                 .setScale(DbConstraints.PRECIO_SCALE,
                                           java.math.RoundingMode.HALF_UP);
            TipoPedido tipo = domicilio
                    ? TipoPedido.DOMICILIO
                    : TipoPedido.ESPECIAL;
            PedidoCreateDto dto = new PedidoCreateDto(
                    sub, desc, cargo, total,
                    observacion, emp, cli.idPersona(),
                    dir, tipo,
                    view.getChkValeGas().isSelected(),
                    detalles
            );
            UiContext.pedidoSvc().crear(dto);
            return null;
        }, v -> {
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Pedido Especial registrado");
            limpiarPostCrear();
        });
    }

    /** Abre el diálogo para nuevo cliente y completa nombre y teléfono. */
    public void registrarCliente() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        DlgClienteNuevo dlg = new DlgClienteNuevo(owner);
        dlg.setVisible(true);
        cargarClientes();
    }

    /** Rellena los campos con los datos del cliente seleccionado. */
    public void seleccionarCliente() {
        int row = view.getTblClientes().getSelectedRow();
        if (row < 0) return;
        int modelRow3 = view.getTblClientes().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblClientes().getModel().getValueAt(modelRow3,0);
        ClienteDto c = clientes.stream()
                .filter(cl -> cl.idPersona().equals(id))
                .findFirst().orElse(null);
        if (c != null) {
            view.getTxtCliNom().setText(c.nombreCompleto());
            view.getTxtCliTel().setText(c.telefono());
            view.getTxtCliDir().setText(c.direccion());
        }
    }

    /** Abre un diálogo para ingresar una observación. */
    public void abrirObservacion() {
        Window owner = SwingUtilities.getWindowAncestor(view);
        DlgObservacion dlg = new DlgObservacion(owner);
        if (observacion != null) {
            dlg.getTxtObs().setText(observacion);
        }
        dlg.setVisible(true);
        observacion = dlg.getController().getObservacion();
    }

    private String fmt(BigDecimal b) {
        return NumberUtils.formatPlain(b);
    }

    /** Aplica las cantidades agregadas para mantener el stock visible. */
    private void aplicarStockLocal() {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        for (DetalleCreateDto d : detalles) {
            for (int i = 0; i < tm.getRowCount(); i++) {
                Integer pid = (Integer) tm.getValueAt(i, 0);
                if (!Objects.equals(pid, d.idProducto())) continue;
                Object val = tm.getValueAt(i, 2);
                if (val == null || val.toString().isBlank()) break;
                try {
                    BigDecimal s = new BigDecimal(val.toString()).subtract(d.cantidad());
                    if (s.compareTo(BigDecimal.ZERO) < 0) s = BigDecimal.ZERO;
                    if (!domicilio) {
                        BigDecimal max = stockInicial.get(pid);
                        if (max != null && s.compareTo(max) > 0) s = max;
                    }
                    tm.setValueAt(NumberUtils.formatPlain(s), i, 2);
                } catch (NumberFormatException ex) {
                    LOG.log(Level.WARNING, "Stock inválido", ex);
                }
                break;
            }
        }
    }

    private void updateStock(int modelRow, BigDecimal delta) {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        Object val = tm.getValueAt(modelRow, 2);
        if (val == null || val.toString().isBlank()) return;
        try {
            BigDecimal s = new BigDecimal(val.toString());
            s = s.add(delta);
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

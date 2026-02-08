package com.comercialvalerio.presentation.controller.ventas;

import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.DetalleCreateDto;
import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.ui.common.DlgObservacion;
import com.comercialvalerio.presentation.ui.ventas.FormVenta;
import com.comercialvalerio.presentation.ui.ventas.DlgComprobante;
import com.comercialvalerio.presentation.ui.clientes.DlgClienteNuevo;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.util.PriceUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import java.util.Locale;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.common.DbConstraints;
import jakarta.ws.rs.WebApplicationException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador para {@link FormVenta}. */
public class VentaController {

    private static final String GENERIC_DNI = "00000000";
    private static final Logger LOG = Logger.getLogger(VentaController.class.getName());

    private final FormVenta view;
    private final List<DetalleCreateDto> detalles = new ArrayList<>();
    private List<ClienteDto> clientes = List.of();
    private String observacion;
    private final Map<Integer, ProductoDto> productoCache = new HashMap<>();
    private final Map<Integer, TallaStockDto> tallaCache = new HashMap<>();
    private final int maxEspecial;

    public VentaController(FormVenta view) {
        this.view = view;
        int m = DbConstraints.MIN_CANTIDAD_MAYORISTA_HILO;
        try {
            var p = UiContext.parametroSistemaSvc().obtener("MIN_CANTIDAD_MAYORISTA_HILO");
            m = p.valor().intValue();
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Error cargando MIN_CANTIDAD_MAYORISTA_HILO", ex);
            ErrorHandler.handle(ex);
        }
        this.maxEspecial = m - 1;
    }

    /** Carga productos con tallas y presentaciones en la tabla de stock. */
    public void cargarProductos() {
        String q = view.getTxtBuscar().getText().trim();
        productoCache.clear();
        tallaCache.clear();
        AsyncTasks.busy(view, () -> {
            var lista = UiContext.productoSvc().listarParaVenta();
            if (!q.isBlank()) {
                String pat = q.toUpperCase(Locale.ROOT);
                lista = lista.stream()
                        .filter(p -> p.nombre().toUpperCase(Locale.ROOT).contains(pat))
                        .toList();
            }
            DefaultTableModel m = new DefaultTableModel(
                    new String[]{"ID","TALLA","PRES","Producto","Stock","Precio"},0);
            for (var p : lista) {
                var tallas = p.tallas();
                var pres   = p.presentaciones();
                if (!tallas.isEmpty()) {
                    for (var t : tallas) {
                        m.addRow(new Object[]{
                                p.idProducto(), t.idTallaStock(), null,
                                p.nombre()+" "+t.talla(), fmt(t.stock()), p.precioUnitario()
                        });
                    }
                } else if (!pres.isEmpty()) {
                    for (var pr : pres) {
                        BigDecimal stock = p.stockActual() == null ? null
                                : p.stockActual().divide(pr.cantidad(), 3, RoundingMode.FLOOR);
                        String nom = p.nombre()+" ("+pr.cantidad()+" "+p.unidadMedida()+")";
                        m.addRow(new Object[]{
                                p.idProducto(), null, pr.cantidad(),
                                nom, fmt(stock), pr.precio()
                        });
                    }
                } else {
                    m.addRow(new Object[]{
                            p.idProducto(), null, null,
                            p.nombre(), fmt(p.stockActual()), p.precioUnitario()
                    });
                }
            }
            return m;
        }, model -> {
            view.getTblStock().setModel(model);
            var cm = view.getTblStock().getColumnModel();
            while (cm.getColumnCount() > 3) cm.removeColumn(cm.getColumn(0));
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            sorter.setSortable(0, false);
            TableUtils.setNumericComparators(sorter, 4, 5);
            view.getTblStock().setRowSorter(sorter);
            TableUtils.packColumns(view.getTblStock());
            TableUtils.updateEmptyView(
                    view.getSpStock(),
                    view.getTblStock(),
                    view.getLblStockEmpty());
            aplicarStockLocal();
        });
    }

    /** Agrega el producto seleccionado a la venta usando la cantidad de la vista. */
    public void agregarDetalle() {
        int row = view.getTblStock().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un producto",
                    "Producto no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal cantidad;
        BigDecimal presentacion = null;
        try {
            String txt = view.getTxtCantidad().getText().trim();
            cantidad = txt.isBlank() ? BigDecimal.ONE : new BigDecimal(txt);
        } catch (NumberFormatException ex) {
            ErrorHandler.handle(new IllegalArgumentException("Cantidad inválida", ex));
            return;
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

        int modelRow = view.getTblStock().convertRowIndexToModel(row);
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        Integer idProd  = (Integer) tm.getValueAt(modelRow,0);
        Integer idTalla = (Integer) tm.getValueAt(modelRow,1);
        presentacion = (BigDecimal) tm.getValueAt(modelRow,2);
        String nombreProd = (String) tm.getValueAt(row,3);
        Object stockObj = tm.getValueAt(modelRow,4);
        BigDecimal stockDisponible = null;
        if (stockObj != null && !stockObj.toString().isBlank()) {
            try {
                stockDisponible = new BigDecimal(stockObj.toString());
            } catch (NumberFormatException ex) {
                stockDisponible = null;
                ErrorHandler.handle(new IllegalArgumentException("Stock invalido", ex));
            }
        }
        ProductoDto prod = productoCache.computeIfAbsent(
                idProd, i -> UiContext.productoSvc().obtener(i));
        boolean esEspecial = prod.paraPedido()
                && prod.tipoPedidoDefault() == TipoPedido.ESPECIAL;
        boolean esFraccionable = presentacion != null;

        BigDecimal cantidadReal = esFraccionable
                ? presentacion.multiply(cantidad)
                : cantidad;

        for (int i = 0; i < detalles.size(); i++) {
            var d = detalles.get(i);
            if (!esFraccionable
                    && d.idProducto().equals(idProd)
                    && java.util.Objects.equals(d.idTallaStock(), idTalla)) {
                BigDecimal nueva = d.cantidad().add(cantidadReal);
                if (stockDisponible != null && nueva.compareTo(stockDisponible) > 0) {
                    JOptionPane.showMessageDialog(view,
                            "Stock insuficiente",
                            "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (esEspecial && nueva.compareTo(BigDecimal.valueOf(maxEspecial)) > 0) {
                    JOptionPane.showMessageDialog(view,
                            "Cantidad máxima " + maxEspecial,
                            "Dato inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (nueva.compareTo(BigDecimal.valueOf(DbConstraints.MAX_CANTIDAD)) > 0) {
                    JOptionPane.showMessageDialog(view,
                            "Cantidad máxima " + DbConstraints.MAX_CANTIDAD,
                            "Dato inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                BigDecimal precio = esFraccionable
                        ? (BigDecimal) tm.getValueAt(modelRow,5)
                        : PriceUtils.precioParaCantidad(prod, nueva);
                detalles.set(i, new DetalleCreateDto(idProd, idTalla, nueva, precio));
                refreshDetalle();
                updateStock(modelRow, cantidad.negate());
                return;
            }
        }

        if (esEspecial && cantidad.compareTo(BigDecimal.valueOf(maxEspecial)) > 0) {
            JOptionPane.showMessageDialog(view,
                    "Cantidad máxima " + maxEspecial,
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (stockDisponible != null && cantidad.compareTo(stockDisponible) > 0) {
            JOptionPane.showMessageDialog(view,
                    "Stock insuficiente",
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal precio = esFraccionable
                ? (BigDecimal) tm.getValueAt(modelRow,5)
                : PriceUtils.precioParaCantidad(prod, cantidadReal);
        detalles.add(new DetalleCreateDto(idProd, idTalla, cantidadReal, precio));
        refreshDetalle();
        updateStock(modelRow, cantidad.negate());
    }

    /** Quita el elemento seleccionado de la tabla y actualiza los totales. */
    public void quitarDetalle() {
        int row = view.getTblAdded().getSelectedRow();
        if (row < 0) return;
        detalles.remove(row);
        refreshDetalle();
    }

    /** Carga clientes en la tabla. */
    public void cargarClientes() {
        AsyncTasks.busy(view, () -> {
            clientes = UiContext.clienteSvc().listarActivos();
            DefaultTableModel m = new DefaultTableModel(
                    new String[]{"ID","Nombre","Teléfono"},0);
            for (ClienteDto c : clientes) {
                m.addRow(new Object[]{c.idPersona(), c.nombreCompleto(), c.telefono()});
            }
            return m;
        }, m -> {
            view.getTblClientes().setModel(m);
            if (view.getTblClientes().getColumnCount() > 0) {
                view.getTblClientes().getColumnModel().removeColumn(
                        view.getTblClientes().getColumnModel().getColumn(0));
            }
            TableRowSorter<DefaultTableModel> sorterCli = new TableRowSorter<>(m);
            sorterCli.setSortable(0, false);
            view.getTblClientes().setRowSorter(sorterCli);
            TableUtils.packColumns(view.getTblClientes());
            TableUtils.updateEmptyView(
                    view.getSpClientes(),
                    view.getTblClientes(),
                    view.getLblClientesEmpty());
            if (m.getRowCount() > 0) {
                view.getTblClientes().setRowSelectionInterval(0, 0);
                seleccionarCliente();
            }
        });
    }

    /** Rellena los campos con los datos del cliente seleccionado. */
    public void seleccionarCliente() {
        int row = view.getTblClientes().getSelectedRow();
        if (row < 0) return;
        Integer id = (Integer) view.getTblClientes().getModel().getValueAt(row,0);
        ClienteDto c = clientes.stream()
                .filter(cl -> cl.idPersona().equals(id))
                .findFirst().orElse(null);
        if (c != null) {
            view.getTxtCliNom().setText(c.nombreCompleto());
            view.getTxtCliTel().setText(c.telefono());
        }
    }

    /** Abre un diálogo para capturar una observación opcional. */
    public void abrirObservacion() {
        java.awt.Window owner = SwingUtilities.getWindowAncestor(view);
        DlgObservacion dlg = new DlgObservacion(owner);
        if (observacion != null) {
            dlg.getTxtObs().setText(observacion);
        }
        dlg.setVisible(true);
        observacion = dlg.getController().getObservacion();
    }

    /** Clears all fields and tables. */
    public void cancelar() {
        detalles.clear();
        observacion = null;
        productoCache.clear();
        tallaCache.clear();
        cargarProductos();
        TableUtils.clearModel((DefaultTableModel) view.getTblAdded().getModel());
        TableUtils.updateEmptyView(
                view.getSpAdded(),
                view.getTblAdded(),
                view.getLblAddedEmpty());
        view.getLblSubTotal().setText(CurrencyUtils.format(BigDecimal.ZERO));
        view.getLblTotal().setText(CurrencyUtils.format(BigDecimal.ZERO));
        view.getTxtCantidad().setText("");
        view.getTxtBuscar().setText("");
        view.getTxtCliNom().setText("");
        view.getTxtCliTel().setText("");
        if (!clientes.isEmpty()) {
            Integer idGenerico = null;
            try {
                var p = UiContext.parametroSistemaSvc().obtener("ID_CLIENTE_GENERICO");
                idGenerico = p.valor().intValue();
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "No se pudo obtener ID_CLIENTE_GENERICO", ex);
                ErrorHandler.handle(ex);
            }

            int index = -1;
            for (int i = 0; i < clientes.size(); i++) {
                ClienteDto c = clientes.get(i);
                if ((idGenerico != null && Objects.equals(c.idPersona(), idGenerico))
                        || GENERIC_DNI.equals(c.dni())) {
                    index = i;
                    break;
                }
            }
            if (index < 0) index = 0;
            view.getTblClientes().setRowSelectionInterval(index, index);
            seleccionarCliente();
        }
        view.getChkDigital().setSelected(false);
        view.getChkEfectivo().setSelected(false);
        view.getTxtDigital().setText("");
        view.getTxtDigital().setEnabled(false);
        view.getTxtEfectivo().setText("");
        view.getTxtEfectivo().setEnabled(false);
        updatePagoFields();
    }

    private void refreshDetalle() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID","Producto","Cantidad","P.Unit","Total"},0);
        BigDecimal sub = BigDecimal.ZERO;
        for (DetalleCreateDto d : detalles) {
            var prod = productoCache.computeIfAbsent(
                    d.idProducto(), id -> UiContext.productoSvc().obtener(id));
            String nom = prod.nombre();
            if (d.idTallaStock() != null) {
                var ts = tallaCache.computeIfAbsent(
                        d.idTallaStock(), id -> UiContext.tallaStockSvc().obtener(id));
                nom += " " + ts.talla();
            }
            BigDecimal tot = d.cantidad().multiply(d.precioUnitario());
            sub = sub.add(tot);
            m.addRow(new Object[]{d.idProducto(), nom, d.cantidad(), d.precioUnitario(), tot});
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
                view.getLblAddedEmpty());
        view.getLblSubTotal().setText(CurrencyUtils.format(sub));
        view.getLblTotal().setText(CurrencyUtils.format(sub));
        updatePagoFields();
    }

    /** Construye y envía el VentaCreateDto usando un proxy REST. */
    public void crear() {
        if (detalles.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Agregue al menos un producto",
                    "Venta sin productos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PagoCreateDto> pagos = new ArrayList<>();
        if (view.getChkDigital().isSelected()) {
            String txt = view.getTxtDigital().getText().trim();
            if (!txt.isBlank()) {
                pagos.add(new PagoCreateDto(2, new BigDecimal(txt)));
            }
        }
        if (view.getChkEfectivo().isSelected()) {
            String txt = view.getTxtEfectivo().getText().trim();
            if (!txt.isBlank()) {
                pagos.add(new PagoCreateDto(1, new BigDecimal(txt)));
            }
        }
        if (pagos.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Ingrese un pago",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rowCli = view.getTblClientes().getSelectedRow();
        BigDecimal[] sub = new BigDecimal[1];
        AsyncTasks.busy(view, () -> {
            sub[0] = BigDecimal.ZERO;
            for (DetalleCreateDto d : detalles) {
                sub[0] = sub[0].add(d.cantidad().multiply(d.precioUnitario()));
            }
            sub[0] = sub[0].setScale(DbConstraints.PRECIO_SCALE,
                                   java.math.RoundingMode.HALF_UP);
            BigDecimal pagoTotal = BigDecimal.ZERO;
            for (PagoCreateDto p : pagos) {
                pagoTotal = pagoTotal.add(p.monto());
            }
            pagoTotal = pagoTotal.setScale(DbConstraints.PRECIO_SCALE,
                                         java.math.RoundingMode.HALF_UP);
            if (pagoTotal.compareTo(sub[0]) != 0) {
                throw new IllegalArgumentException("Los pagos deben sumar el total de la venta");
            }
            Integer idEmpleado = UiContext.getUsuarioActual() == null ? null
                    : UiContext.getUsuarioActual().idPersona();
            Integer idCliente = null;
            if (rowCli >= 0) {
                idCliente = (Integer) view.getTblClientes().getModel().getValueAt(rowCli,0);
            } else if (!clientes.isEmpty()) {
                idCliente = clientes.get(0).idPersona();
            }
            VentaCreateDto dto = new VentaCreateDto(
                    sub[0], BigDecimal.ZERO, BigDecimal.ZERO, sub[0],
                    observacion, idEmpleado, idCliente,
                    detalles, pagos, null
            );
            return UiContext.ventaSvc().crear(dto);
        }, venta -> {
            java.awt.Window owner = SwingUtilities.getWindowAncestor(view);
            DlgComprobante dlg = new DlgComprobante(owner);
            dlg.setSubTotal(CurrencyUtils.format(sub[0]));
            dlg.setTotal(CurrencyUtils.format(sub[0]));

            ClienteDto cli = null;
            if (rowCli >= 0) {
                Integer idc = (Integer) view.getTblClientes().getModel().getValueAt(rowCli,0);
                cli = clientes.stream().filter(c -> c.idPersona().equals(idc)).findFirst().orElse(null);
            } else if (!clientes.isEmpty()) {
                cli = clientes.get(0);
            }
            if (cli != null) {
                String tel = cli.telefono() == null ? "" : cli.telefono();
                dlg.getTxtTelefono().setText(tel);
                boolean hasTel = !tel.isBlank();
                dlg.getTxtTelefono().setVisible(!hasTel);
                if (!hasTel) {
                    dlg.getTxtTelefono().setEditable(false);
                }
                boolean generico = GENERIC_DNI.equals(cli.dni());
                dlg.getChkWhatsApp().setEnabled(!generico);
                if (generico) {
                    dlg.getChkWhatsApp().setSelected(false);
                } else if (hasTel) {
                    dlg.getChkWhatsApp().setSelected(true);
                }
            }
            dlg.getBtnConfirmar().addActionListener(ev ->
                confirmarComprobante(venta.idTransaccion(), dlg)
            );
            dlg.getBtnImprimir().addActionListener(ev ->
                AsyncTasks.busy(view,
                    () -> UiContext.comprobanteSvc().descargarPdf(venta.idTransaccion()),
                    pdf -> {
                        try {
                            PdfPrinter.print(pdf);
                        } catch (javax.print.PrintException ex) {
                            ErrorHandler.handle(ex);
                        }
                        dlg.dispose();
                    })
            );
            dlg.getBtnDescargar().addActionListener(ev ->
                AsyncTasks.busy(view,
                    () -> UiContext.comprobanteSvc().obtenerPdf(venta.idTransaccion()),
                    dto -> {
                        java.io.File dir = UserPrefs.getPdfDirectory();
                        javax.swing.JFileChooser fc =
                                dir != null ? new javax.swing.JFileChooser(dir) : new javax.swing.JFileChooser();
                        fc.setSelectedFile(new java.io.File(dto.nombreArchivo()));
                        if (fc.showSaveDialog(dlg) == javax.swing.JFileChooser.APPROVE_OPTION) {
                            UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                            try {
                                java.nio.file.Files.write(fc.getSelectedFile().toPath(), dto.pdf());
                            } catch (java.io.IOException ex) {
                                LOG.log(Level.SEVERE, "Error al guardar archivo", ex);
                                ErrorHandler.handle(new IllegalStateException("Error al guardar archivo", ex));
                            }
                        }
                        dlg.dispose();
                    })
            );
            dlg.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    raven.toast.Notifications.getInstance()
                            .show(raven.toast.Notifications.Type.SUCCESS,
                                    "Venta registrada");
                    cancelar();
                }
            });
            dlg.setVisible(true);
        });
    }

    void confirmarComprobante(Integer id, DlgComprobante dlg) {
        dlg.getBtnConfirmar().setEnabled(false);
        try {
            UiContext.comprobanteSvc().generar(id);
            if (dlg.isEnviarWhatsApp()) {
                String tel = dlg.getTelefono();
                AsyncTasks.busy(view,
                        () -> {
                            UiContext.comprobanteSvc().enviarWhatsApp(id, new TelefonoDto(tel));
                            return null;
                        },
                        r -> dlg.disableWhatsAppOption(),
                        ErrorHandler::handle);
            }
            dlg.getBtnImprimir().setEnabled(true);
            dlg.getBtnDescargar().setEnabled(true);
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al generar comprobante", ex);
            ErrorHandler.handle(ex);
            dlg.getBtnConfirmar().setEnabled(true);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al generar comprobante", ex);
            ErrorHandler.handle(ex);
            dlg.getBtnConfirmar().setEnabled(true);
        }
    }

    /** Abre el diálogo para nuevo cliente y recarga la tabla. */
    public void registrarCliente() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        DlgClienteNuevo dlg = new DlgClienteNuevo(owner);
        dlg.setVisible(true);
        cargarClientes();
    }

    /** Actualiza los campos de pago según los métodos seleccionados. */
    public void updatePagoFields() {
        boolean digital = view.getChkDigital().isSelected();
        boolean efectivo = view.getChkEfectivo().isSelected();
        BigDecimal total = parseMoney(view.getLblTotal().getText());
        if (digital && !efectivo) {
            view.getTxtDigital().setText(total.toPlainString());
            view.getTxtDigital().setEnabled(false);
            view.getTxtEfectivo().setText("");
            view.getTxtEfectivo().setEnabled(false);
        } else if (!digital && efectivo) {
            view.getTxtEfectivo().setText(total.toPlainString());
            view.getTxtEfectivo().setEnabled(false);
            view.getTxtDigital().setText("");
            view.getTxtDigital().setEnabled(false);
        } else {
            view.getTxtDigital().setEnabled(digital);
            if (!digital) view.getTxtDigital().setText("");
            view.getTxtEfectivo().setEnabled(efectivo);
            if (!efectivo) view.getTxtEfectivo().setText("");
        }
    }

    private BigDecimal parseMoney(String text) {
        if (text == null) return BigDecimal.ZERO;
        String t = text.replace("S/", "").replace(" ", "")
                .replace(",", "").trim();
        if (t.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException ex) {
            ErrorHandler.handle(new IllegalArgumentException("Monto invalido", ex));
            return BigDecimal.ZERO;
        }
    }

    private String fmt(BigDecimal b) {
        return NumberUtils.formatPlain(b);
    }

    /** Aplica las cantidades agregadas para mantener el stock actualizado. */
    private void aplicarStockLocal() {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        for (DetalleCreateDto d : detalles) {
            for (int i = 0; i < tm.getRowCount(); i++) {
                Integer pid = (Integer) tm.getValueAt(i, 0);
                Integer tid = (Integer) tm.getValueAt(i, 1);
                if (Objects.equals(pid, d.idProducto()) && Objects.equals(tid, d.idTallaStock())) {
                    Object val = tm.getValueAt(i, 4);
                    if (val == null || val.toString().isBlank()) break;
                    try {
                        BigDecimal s = new BigDecimal(val.toString()).subtract(d.cantidad());
                        if (s.compareTo(BigDecimal.ZERO) < 0) s = BigDecimal.ZERO;
                        tm.setValueAt(NumberUtils.formatPlain(s), i, 4);
                    } catch (NumberFormatException ex) {
                        LOG.log(Level.WARNING, "Stock inválido", ex);
                    }
                    break;
                }
            }
        }
    }

    private void updateStock(int modelRow, BigDecimal delta) {
        DefaultTableModel tm = (DefaultTableModel) view.getTblStock().getModel();
        Object val = tm.getValueAt(modelRow, 4);
        if (val == null || val.toString().isBlank()) return;
        try {
            BigDecimal s = new BigDecimal(val.toString()).add(delta);
            if (s.compareTo(BigDecimal.ZERO) < 0) s = BigDecimal.ZERO;
            tm.setValueAt(NumberUtils.formatPlain(s), modelRow, 4);
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Stock inválido", ex);
        }
    }
}

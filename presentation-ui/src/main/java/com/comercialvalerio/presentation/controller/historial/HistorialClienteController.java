package com.comercialvalerio.presentation.controller.historial;

import java.util.List;
import java.time.OffsetDateTime;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;

import javax.print.PrintException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.HistorialDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.historial.FormHistorialCliente;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.util.NameUtils;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.VentaDto;
import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.presentation.ui.pedidos.DlgPedidoDetalle;
import com.comercialvalerio.presentation.ui.ventas.DlgVentaDetalle;
import com.comercialvalerio.presentation.ui.base.DatePickerField;

import jakarta.ws.rs.WebApplicationException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Window;
import java.math.BigDecimal;

/**
 * Controlador para {@link FormHistorialCliente}.
 * Maneja la carga del historial y acciones relacionadas.
 */
public class HistorialClienteController {

    private final FormHistorialCliente view;
    private ClienteDto clienteActual;
    private java.util.List<CategoriaDto> categorias = java.util.List.of();
    private java.util.List<ProductoDto> productos = java.util.List.of();
    private static final Logger LOG =
            Logger.getLogger(HistorialClienteController.class.getName());
    private Runnable updateButtonsCallback;

    public HistorialClienteController(FormHistorialCliente view) {
        this.view = view;
    }

    /**
     * Establece una acción a ejecutar para actualizar los botones del diálogo
     * contenedor después de llenar la tabla.
     */
    public void setUpdateButtonsCallback(Runnable callback) {
        this.updateButtonsCallback = callback;
    }

    /** Carga las categorías en el cuadro combinado. */
    public void cargarCategorias() {
        AsyncTasks.busy(view,
                () -> UiContext.categoriaSvc().listar(),
                list -> {
                    categorias = list;
                    javax.swing.JComboBox<String> combo = view.getCboCategoria();
                    combo.removeAllItems();
                    combo.addItem("Todas");
                    for (CategoriaDto c : list) {
                        combo.addItem(c.nombre());
                    }
                    cargarProductos();
                },
                ErrorHandler::handle);
    }

    /** Carga productos según la categoría seleccionada. */
    public void cargarProductos() {
        int idx = view.getCboCategoria().getSelectedIndex();
        Integer catId = null;
        if (idx > 0 && idx - 1 < categorias.size()) {
            catId = categorias.get(idx - 1).idCategoria();
        }
        Integer finalCat = catId;
        AsyncTasks.busy(view,
                () -> UiContext.productoSvc().listar(null, finalCat, null, null, null),
                list -> {
                    productos = list;
                    javax.swing.JComboBox<String> combo = view.getCboProducto();
                    combo.removeAllItems();
                    combo.addItem("Todos");
                    for (ProductoDto p : list) {
                        combo.addItem(p.nombre());
                    }
                    refresh();
                },
                ErrorHandler::handle);
    }

    /** Carga el historial del cliente indicado en la tabla. */
    public void cargarHistorialDe(ClienteDto cliente) {
        if (cliente == null) {
            JOptionPane.showMessageDialog(view,
                    "Cliente no encontrado",
                    "Cliente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.time.LocalDate d1 = view.getSpDesde().getDate();
        java.time.LocalDate d2 = view.getSpHasta().getDate();
        if (d1 != null && d2 != null && d2.isBefore(d1)) {
            JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
            view.getSpHasta().setDate(null);
            return;
        }
        clienteActual = cliente;
        java.time.LocalDateTime desde = getDate(view.getSpDesde());
        java.time.LocalDateTime hasta = getDate(view.getSpHasta());
        java.time.LocalDateTime hastaInc = hasta == null ? null : hasta.plusDays(1);
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
            view.getSpHasta().setDate(null);
            return;
        }
        AsyncTasks.busy(view,
                () -> UiContext.historialSvc().historialPorCliente(
                        cliente.idPersona(),
                        desde,
                        hastaInc,
                        getSelectedCategoriaId(),
                        getSelectedProductoId()),
                this::fillTable);
    }

    /** Recarga la tabla usando el último cliente cargado. */
    public void refresh() {
        if (clienteActual != null) {
            cargarHistorialDe(clienteActual);
        }
    }

    /** Imprime el comprobante de la fila seleccionada si está disponible. */
    public void reimprimirComprobante() {
        Integer id = selectedId();
        if (id == null) return;
        if (!DialogUtils.confirmAction(view, "Imprimir comprobante?")) return;
        AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().descargarPdf(id), pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                ErrorHandler.handle(new IllegalStateException("Error al imprimir", ex));
            }
        });
    }

    /** Descarga el comprobante PDF de la fila seleccionada. */
    public void descargarComprobante() {
        Integer id = selectedId();
        if (id == null) return;
        if (!DialogUtils.confirmAction(view, "Descargar comprobante?")) return;
        AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().obtenerPdf(id), dto -> {
            java.io.File dir = UserPrefs.getPdfDirectory();
            JFileChooser fc = dir != null ? new JFileChooser(dir) : new JFileChooser();
            fc.setSelectedFile(new java.io.File(dto.nombreArchivo()));
            if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    java.nio.file.Files.write(fc.getSelectedFile().toPath(), dto.pdf());
                } catch (java.io.IOException ex) {
                    ErrorHandler.handle(new IllegalStateException("Error al guardar", ex));
                }
            }
        });
    }

    /** Descarga el PDF de la orden de compra de la fila seleccionada. */
    public void descargarOrden() {
        Integer id = selectedId();
        if (id == null) return;
        if (!DialogUtils.confirmAction(view, "Descargar orden?")) return;
        AsyncTasks.busy(view, () -> UiContext.pedidoSvc().obtenerOrden(id), dto -> {
            java.io.File dir = UserPrefs.getPdfDirectory();
            JFileChooser fc = dir != null ? new JFileChooser(dir) : new JFileChooser();
            fc.setSelectedFile(new java.io.File(dto.nombreArchivo()));
            if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    java.nio.file.Files.write(fc.getSelectedFile().toPath(), dto.pdf());
                } catch (java.io.IOException ex) {
                    ErrorHandler.handle(new IllegalStateException("Error al guardar", ex));
                }
            }
        }, ex -> {
            if (ex instanceof WebApplicationException wex &&
                    wex.getResponse() != null && wex.getResponse().getStatus() == 404) {
                JOptionPane.showMessageDialog(view,
                        "Orden de compra no encontrada",
                        "Orden", JOptionPane.WARNING_MESSAGE);
            } else {
                ErrorHandler.handle(ex);
            }
        });
    }


    /** Muestra el detalle completo de la transacción seleccionada. */
    public void mostrarDetalle() {
        int row = view.getTable().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTable().convertRowIndexToModel(row);
        Integer id = (Integer) view.getModel().getValueAt(modelRow, 0);
        Object tipoObj = view.getModel().getValueAt(modelRow, 6);
        String tipo = tipoObj == null ? "" : tipoObj.toString();
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(view);
        if ("Venta".equalsIgnoreCase(tipo)) {
            mostrarDetalleVenta(id, owner);
        } else {
            mostrarDetallePedido(id, owner);
        }
    }

    private void mostrarDetalleVenta(Integer id, Window owner) {
        VentaDto v = null;
        try {
            v = UiContext.ventaSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener venta", ex);
            ErrorHandler.handle(ex);
        }
        if (v == null) {
            JOptionPane.showMessageDialog(view,
                    "Transacción no encontrada",
                    "Detalle", JOptionPane.WARNING_MESSAGE);
            refresh();
            return;
        }
        DlgVentaDetalle dlg = new DlgVentaDetalle(owner);
        dlg.getLblId().setText(String.valueOf(v.idTransaccion()));
        Object f = v.fecha();
        dlg.getLblFecha().setText(DateFormatUtils.formatServer(f));
        String empNombre = v.empleadoUsuario();
        if (v.empleadoId() != null) {
            try {
                EmpleadoDto ed = UiContext.empleadoSvc().obtener(v.empleadoId());
                empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Error al obtener empleado", ex);
                ErrorHandler.handle(ex);
            }
        }
        dlg.getLblEmpleado().setText(empNombre);
        String cliNombre = v.clienteNombre();
        if (v.clienteId() != null) {
            try {
                ClienteDto cd = UiContext.clienteSvc().obtener(v.clienteId());
                cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Error al obtener cliente", ex);
                ErrorHandler.handle(ex);
            }
        }
        dlg.getLblCliente().setText(cliNombre);
        dlg.getLblTotal().setText(String.valueOf(v.totalNeto()));
        dlg.getLblEstado().setText(v.estado());
        dlg.getLblObs().setText(v.observacion() == null ? "" : v.observacion());
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(id);
            boolean showTalla = det.stream().anyMatch(d -> d.talla() != null);
            DefaultTableModel m = showTalla
                    ? new DefaultTableModel(new String[]{"Producto","Talla","Cant","P.Unit","Sub"},0)
                    : new DefaultTableModel(new String[]{"Producto","Cant","P.Unit","Sub"},0);
            for (DetalleDto d : det) {
                if (showTalla) {
                    m.addRow(new Object[]{d.productoNombre(), d.talla(), d.cantidad(), d.precioUnitario(), d.subtotal()});
                } else {
                    m.addRow(new Object[]{d.productoNombre(), d.cantidad(), d.precioUnitario(), d.subtotal()});
                }
            }
            dlg.getTblDetalles().setModel(m);
            TableUtils.packColumns(dlg.getTblDetalles());
            TableUtils.updateEmptyView(
                    dlg.getSpDetalles(),
                    dlg.getTblDetalles(),
                    dlg.getLblEmptyDetalles());
            List<PagoDto> pagos = UiContext.pagoSvc().listar(id);
            DefaultTableModel mp = new DefaultTableModel(new String[]{"Método","Monto"},0);
            for (PagoDto p : pagos) {
                mp.addRow(new Object[]{p.metodoNombre(), p.monto()});
            }
            dlg.getTblPagos().setModel(mp);
            TableUtils.packColumns(dlg.getTblPagos());
            if (pagos.isEmpty() && "En Proceso".equals(v.estado())) {
                dlg.getLblEmptyPagos().setText("Aún no hay pagos registrados");
            } else {
                dlg.getLblEmptyPagos().setText("Sin datos");
            }
            TableUtils.updateEmptyView(
                    dlg.getSpPagos(),
                    dlg.getTblPagos(),
                    dlg.getLblEmptyPagos());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cargar detalle de venta", ex);
            ErrorHandler.handle(ex);
        }
        dlg.setVisible(true);
    }

    private void mostrarDetallePedido(Integer id, Window owner) {
        PedidoDto ped = null;
        try {
            ped = UiContext.pedidoSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener pedido", ex);
            ErrorHandler.handle(ex);
        }
        if (ped == null) {
            JOptionPane.showMessageDialog(view,
                    "Transacción no encontrada",
                    "Detalle", JOptionPane.WARNING_MESSAGE);
            refresh();
            return;
        }
        DlgPedidoDetalle dlg = new DlgPedidoDetalle(owner);
        dlg.getLblId().setText(String.valueOf(ped.idTransaccion()));
        Object f1 = ped.fecha();
        OffsetDateTime odt1 = DateFormatUtils.parseOffsetDateTime(f1);
        dlg.getLblFecha().setText(odt1 == null ? "‐" : DateFormatUtils.formatServer(odt1));
        Object f2 = ped.fechaHoraEntrega();
        OffsetDateTime odt2 = DateFormatUtils.parseOffsetDateTime(f2);
        dlg.getLblEntrega().setText(odt2 == null ? "‐" : DateFormatUtils.formatServer(odt2));
        dlg.getLblDireccion().setText(ped.direccionEntrega());
        dlg.getLblTipo().setText(ped.tipoPedido().getNombre());
        dlg.getLblValeGas().setText(ped.usaValeGas() ? "Sí" : "No");
        String empNombre = ped.empleadoUsuario();
        if (ped.empleadoId() != null) {
            try {
                EmpleadoDto ed = UiContext.empleadoSvc().obtener(ped.empleadoId());
                empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Error al obtener empleado", ex);
                ErrorHandler.handle(ex);
            }
        }
        dlg.getLblEmpleado().setText(empNombre);
        String cliNombre = ped.clienteNombre();
        if (ped.clienteId() != null) {
            try {
                ClienteDto cd = UiContext.clienteSvc().obtener(ped.clienteId());
                cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Error al obtener cliente", ex);
                ErrorHandler.handle(ex);
            }
        }
        dlg.getLblCliente().setText(cliNombre);
        dlg.getLblTotal().setText(String.valueOf(ped.totalNeto()));
        dlg.getLblEstado().setText(ped.estado());
        dlg.getLblComentario().setText(ped.comentarioCancelacion() == null ? "" : ped.comentarioCancelacion());
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(id);
            boolean showTalla = det.stream().anyMatch(d -> d.talla() != null);
            DefaultTableModel m = showTalla
                    ? new DefaultTableModel(new String[]{"Producto","Talla","Cant","P.Unit","Sub"},0)
                    : new DefaultTableModel(new String[]{"Producto","Cant","P.Unit","Sub"},0);
            for (DetalleDto d : det) {
                if (showTalla) {
                    m.addRow(new Object[]{d.productoNombre(), d.talla(), d.cantidad(), d.precioUnitario(), d.subtotal()});
                } else {
                    m.addRow(new Object[]{d.productoNombre(), d.cantidad(), d.precioUnitario(), d.subtotal()});
                }
            }
            dlg.getTblDetalles().setModel(m);
            TableUtils.packColumns(dlg.getTblDetalles());
            TableUtils.updateEmptyView(
                    dlg.getSpDetalles(),
                    dlg.getTblDetalles(),
                    dlg.getLblEmptyDetalles());
            List<PagoDto> pagos = UiContext.pagoSvc().listar(id);
            DefaultTableModel mp = new DefaultTableModel(new String[]{"Método","Monto"},0);
            for (PagoDto p : pagos) {
                mp.addRow(new Object[]{p.metodoNombre(), p.monto()});
            }
            dlg.getTblPagos().setModel(mp);
            TableUtils.packColumns(dlg.getTblPagos());
            if (pagos.isEmpty() && "En Proceso".equals(ped.estado())) {
                dlg.getLblEmptyPagos().setText("Aún no hay pagos registrados");
            } else {
                dlg.getLblEmptyPagos().setText("Sin datos");
            }
            TableUtils.updateEmptyView(
                    dlg.getSpPagos(),
                    dlg.getTblPagos(),
                    dlg.getLblEmptyPagos());
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al cargar detalle de pedido", ex);
            ErrorHandler.handle(ex);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cargar detalle de pedido", ex);
            ErrorHandler.handle(ex);
        }
        dlg.setVisible(true);
    }

    /** Imprime la orden de compra si está disponible. */
    public void reimprimirOrden() {
        Integer id = selectedId();
        if (id == null) return;
        AsyncTasks.busy(view, () -> UiContext.pedidoSvc().descargarOrden(id), pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                ErrorHandler.handle(new IllegalStateException("Error al imprimir la orden", ex));
            }
        }, ex -> {
            if (ex instanceof WebApplicationException wex &&
                    wex.getResponse() != null && wex.getResponse().getStatus() == 404) {
                JOptionPane.showMessageDialog(view,
                        "Orden de compra no encontrada",
                        "Orden", JOptionPane.WARNING_MESSAGE);
            } else {
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Envía la orden de compra por WhatsApp. */
    public void enviarOrdenWhatsApp() {
        Integer id = selectedId();
        if (id == null) return;
        PedidoDto ped = null;
        try {
            ped = UiContext.pedidoSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener pedido", ex);
            ErrorHandler.handle(ex);
        }
        String telefono = null;
        if (ped != null && ped.clienteId() != null) {
            try {
                ClienteDto cli = UiContext.clienteSvc().obtener(ped.clienteId());
                telefono = cli.telefono();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al obtener cliente", ex);
                ErrorHandler.handle(ex);
            }
        }
        if (telefono == null || telefono.isBlank()) {
            telefono = JOptionPane.showInputDialog(
                    view,
                    "Teléfono para WhatsApp:",
                    "WhatsApp",
                    JOptionPane.QUESTION_MESSAGE);
            if (telefono == null || telefono.isBlank()) return;
        }
        if (!DialogUtils.confirmAction(view,
                "¿Enviar orden por WhatsApp?")) return;
        String finalTel = PhoneUtils.stripToDigits(telefono);
        String telFinal = finalTel;
        AsyncTasks.busy(view, () -> {
            UiContext.pedidoSvc().enviarOrdenWhatsApp(id, new TelefonoDto(telFinal));
            return null;
        }, r -> JOptionPane.showMessageDialog(view,
                "Enviado a " + telFinal,
                "WhatsApp", JOptionPane.INFORMATION_MESSAGE));
    }


    /** Envía el comprobante por WhatsApp. */
    public void enviarComprobanteWhatsApp() {
        Integer id = selectedId();
        if (id == null) return;
        String tel = clienteActual != null ? clienteActual.telefono() : null;
        if (tel == null || tel.isBlank()) {
            tel = JOptionPane.showInputDialog(
                    view,
                    "Teléfono para WhatsApp:",
                    "WhatsApp",
                    JOptionPane.QUESTION_MESSAGE);
            if (tel == null || tel.isBlank()) return;
        }
        if (!DialogUtils.confirmAction(view, "¿Enviar por WhatsApp?")) return;
        String finalTel = PhoneUtils.stripToDigits(tel);
        AsyncTasks.busy(
                view,
                () -> {
                    UiContext.comprobanteSvc().enviarWhatsApp(id, new TelefonoDto(finalTel));
                    return null;
                },
                r -> JOptionPane.showMessageDialog(
                        view,
                        "Enviado a " + finalTel,
                        "WhatsApp",
                        JOptionPane.INFORMATION_MESSAGE),
                ErrorHandler::handle);
    }

    private Integer selectedId() {
        int row = view.getTable().getSelectedRow();
        if (row < 0) return null;
        int modelRow = view.getTable().convertRowIndexToModel(row);
        return (Integer) view.getModel().getValueAt(modelRow, 0);
    }

    private Integer getSelectedProductoId() {
        int idx = view.getCboProducto().getSelectedIndex();
        if (idx > 0 && idx - 1 < productos.size()) {
            return productos.get(idx - 1).idProducto();
        }
        return null;
    }

    private Integer getSelectedCategoriaId() {
        int idx = view.getCboCategoria().getSelectedIndex();
        if (idx > 0 && idx - 1 < categorias.size()) {
            return categorias.get(idx - 1).idCategoria();
        }
        return null;
    }

    private java.time.LocalDateTime getDate(DatePickerField sp) {
        java.time.LocalDate d = sp.getDate();
        if (d == null) return null;
        return d.atStartOfDay();
    }

    private void fillTable(List<HistorialDto> lista) {
        DefaultTableModel m = view.getModel();
        TableUtils.clearModel(m);
        for (HistorialDto h : lista) {
            Object f = h.fecha();
            String fecha = DateFormatUtils.formatServer(f);
            m.addRow(new Object[]{
                    h.idTransaccion(),
                    fecha,
                    h.totalNeto(),
                    h.descuento(),
                    h.cargo(),
                    h.estado(),
                    h.tipo()
            });
        }
        view.getTable().setModel(m);
        if (view.getTable().getColumnModel().getColumnCount() == m.getColumnCount()) {
            view.getTable().getColumnModel()
                    .removeColumn(view.getTable().getColumnModel().getColumn(0));
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        TableUtils.setNumericComparators(sorter, 2, 3, 4);
        view.getTable().setRowSorter(sorter);
        TableUtils.packColumns(view.getTable());
        TableUtils.updateEmptyView(
                view.getScroll(),
                view.getTable(),
                view.getLblEmpty());
        if (updateButtonsCallback != null) {
            updateButtonsCallback.run();
        }
    }

}

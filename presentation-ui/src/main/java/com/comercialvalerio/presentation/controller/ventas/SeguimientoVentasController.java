package com.comercialvalerio.presentation.controller.ventas;

import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.VentaDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import java.time.OffsetDateTime;
import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.common.DlgMotivoCancelacion;
import com.comercialvalerio.presentation.ui.ventas.FormSeguimientoVentas;
import com.comercialvalerio.presentation.ui.ventas.DlgVentaDetalle;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.util.NameUtils;
import com.comercialvalerio.presentation.core.AsyncTasks;
import jakarta.ws.rs.WebApplicationException;

import javax.print.PrintException;

import javax.swing.JFileChooser;
import com.comercialvalerio.presentation.ui.util.UserPrefs;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Window;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Controlador para {@link FormSeguimientoVentas}. */
public class SeguimientoVentasController {

    private final FormSeguimientoVentas view;
    private static final Logger LOG = Logger.getLogger(SeguimientoVentasController.class.getName());
    private List<CategoriaDto> categorias = List.of();
    private List<ProductoDto>  productos  = List.of();
    private java.util.Map<Integer, EmpleadoDto> empleadoMap = java.util.Map.of();
    private java.util.Map<Integer, ClienteDto>  clienteMap  = java.util.Map.of();
    /** Identificador de la última actualización solicitada. */
    private volatile long refreshVersion = 0L;

    public SeguimientoVentasController(FormSeguimientoVentas view) {
        this.view = view;
    }

    /** Carga las categorías en el cuadro combinado. */
    public void cargarCategorias() {
        AsyncTasks.busy(view,
                () -> UiContext.categoriaSvc().listar(),
                list -> {
                    categorias = list;
                    JComboBox<String> combo = view.getCboCategoria();
                    combo.removeAllItems();
                    combo.addItem("Todas");
                    for (CategoriaDto c : list) {
                        combo.addItem(c.nombre());
                    }
                    cargarProductos();
                },
                ex -> {
                    LOG.log(Level.SEVERE, "Error al cargar categorías", ex);
                    ErrorHandler.handle(ex);
                });
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
                    JComboBox<String> combo = view.getCboProducto();
                    combo.removeAllItems();
                    combo.addItem("Todos");
                    for (ProductoDto p : list) {
                        combo.addItem(p.nombre());
                    }
                    refreshTable();
                },
                ex -> {
                    LOG.log(Level.SEVERE, "Error al cargar productos", ex);
                    ErrorHandler.handle(ex);
                });
    }

    /** Actualiza la tabla de ventas aplicando el filtro de producto si está seleccionado. */
    public void refreshTable() {
        long version = ++refreshVersion;
        LocalDateTime desde = getDate(view.getSpDesde());
        LocalDateTime hasta = getDate(view.getSpHasta());
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
            view.getSpHasta().setDate(null);
            return;
        }
        LocalDateTime hastaInc = hasta == null ? null : hasta.plusDays(1);
        Integer prodId = getSelectedProductId();
        AsyncTasks.busy(view, () -> {
            if (empleadoMap.isEmpty()) {
                var emps = UiContext.empleadoSvc().listar();
                empleadoMap = emps.stream()
                        .collect(Collectors.toMap(EmpleadoDto::idPersona, e -> e));
            }
            if (clienteMap.isEmpty()) {
                var clis = UiContext.clienteSvc().listar();
                clienteMap = clis.stream()
                        .collect(Collectors.toMap(ClienteDto::idPersona, c -> c));
            }
            List<VentaDto> lista;
            if (desde == null && hasta == null) {
                lista = UiContext.ventaSvc().listar();
            } else {
                lista = UiContext.ventaSvc().listarPorRango(desde, hastaInc);
            }
            if (prodId != null) {
                lista = lista.stream()
                        .filter(v -> ventaTieneProducto(v.idTransaccion(), prodId))
                        .toList();
            }
            return lista;
        }, ventas -> {
            if (version != refreshVersion) {
                return; // descartar resultados obsoletos
            }
            DefaultTableModel m = new DefaultTableModel(
                    new String[]{"ID","Fecha","Cliente","Empleado","Total","Estado"},0);
            for (VentaDto v : ventas) {
                Object f = v.fecha();
                String fecha = DateFormatUtils.formatServer(f);
                String cliNombre = v.clienteNombre();
                ClienteDto cd = clienteMap.get(v.clienteId());
                if (cd != null) {
                    cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
                }
                String empNombre = v.empleadoUsuario();
                EmpleadoDto ed = empleadoMap.get(v.empleadoId());
                if (ed != null) {
                    empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
                }
                m.addRow(new Object[]{
                        v.idTransaccion(),
                        fecha,
                        cliNombre,
                        empNombre,
                        v.totalNeto(),
                        v.estado()
                });
            }
            view.getTblVentas().setModel(m);
            view.getTblVentas().getColumnModel().removeColumn(
                    view.getTblVentas().getColumnModel().getColumn(0));
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
            sorter.setSortable(0, false);
            TableUtils.setNumericComparators(sorter, 4);
            view.getTblVentas().setRowSorter(sorter);
            TableUtils.packColumns(view.getTblVentas());
            TableUtils.updateEmptyView(
                    view.getSpVentas(),
                    view.getTblVentas(),
                    view.getLblEmpty());
            view.updateButtons();
        }, ex -> {
            LOG.log(Level.SEVERE, "Error al refrescar ventas", ex);
            ErrorHandler.handle(ex);
        });
    }

    private Integer getSelectedProductId() {
        int idx = view.getCboProducto().getSelectedIndex();
        if (idx > 0 && idx - 1 < productos.size()) {
            return productos.get(idx - 1).idProducto();
        }
        return null;
    }

    private boolean ventaTieneProducto(Integer idVenta, Integer idProd) {
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(idVenta);
            for (DetalleDto d : det) {
                if (idProd.equals(d.idProducto())) {
                    return true;
                }
            }
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al verificar producto en venta", ex);
            ErrorHandler.handle(ex);
        }
        return false;
    }

    /** Muestra los detalles completos de la venta seleccionada. */
    public void mostrarDetalle() {
        int row = view.getTblVentas().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblVentas().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblVentas().getModel().getValueAt(modelRow, 0);
        Window owner = SwingUtilities.getWindowAncestor(view);
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
            refreshTable();
            return;
        }
        DlgVentaDetalle dlg = new DlgVentaDetalle(owner);
        dlg.getLblId().setText(String.valueOf(v.idTransaccion()));
        Object f = v.fecha();
        dlg.getLblFecha().setText(DateFormatUtils.formatServer(f));
        String empNombre = v.empleadoUsuario();
        EmpleadoDto ed = empleadoMap.get(v.empleadoId());
        if (ed != null) {
            empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
        }
        dlg.getLblEmpleado().setText(empNombre);
        String cliNombre = v.clienteNombre();
        ClienteDto cd = clienteMap.get(v.clienteId());
        if (cd != null) {
            cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
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

    /** Cancela la venta seleccionada solicitando un motivo. */
    public void cancelarVenta() {
        int row = view.getTblVentas().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione una venta",
                    "Venta no seleccionada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblVentas().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblVentas().getModel().getValueAt(modelRow, 0);
        Window owner = SwingUtilities.getWindowAncestor(view);
        DlgMotivoCancelacion dlg = new DlgMotivoCancelacion(owner);
        dlg.setVisible(true);
        String motivo = dlg.getController().getMotivo();
        if (motivo == null) return;
        try {
            UiContext.ventaSvc().cancelar(id, new MotivoDto(motivo));
            refreshTable();
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cancelar venta", ex);
            ErrorHandler.handle(ex);
        }
    }

    /** Reimprime el comprobante de la venta seleccionada si está disponible. */
    public void reimprimirComprobante() {
        int row = view.getTblVentas().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblVentas().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblVentas().getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().descargarPdf(id), pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                ErrorHandler.handle(ex);
            }
        }, ex -> {
            if (ex instanceof WebApplicationException wex &&
                    wex.getResponse() != null && wex.getResponse().getStatus() == 404) {
                JOptionPane.showMessageDialog(view,
                        "No hay comprobante disponible",
                        "Comprobante", JOptionPane.WARNING_MESSAGE);
            } else {
                LOG.log(Level.SEVERE, "Error al descargar comprobante", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Descarga el comprobante PDF de la venta seleccionada. */
    public void descargarComprobante() {
        int row = view.getTblVentas().getSelectedRow();
        if (row < 0) return;
        if (!DialogUtils.confirmAction(view, "Descargar comprobante?")) return;
        int modelRow = view.getTblVentas().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblVentas().getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().obtenerPdf(id), dto -> {
            java.io.File dir = UserPrefs.getPdfDirectory();
            JFileChooser fc = dir != null ? new JFileChooser(dir) : new JFileChooser();
            fc.setSelectedFile(new java.io.File(dto.nombreArchivo()));
            if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    java.nio.file.Files.write(fc.getSelectedFile().toPath(), dto.pdf());
                } catch (java.io.IOException ex) {
                    LOG.log(Level.SEVERE, "Error al guardar archivo", ex);
                    ErrorHandler.handle(new IllegalStateException("Error al guardar archivo", ex));
                }
            }
        });
    }

    /** Envía el comprobante PDF por WhatsApp al cliente. */
    public void enviarComprobanteWhatsApp() {
        int row = view.getTblVentas().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblVentas().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblVentas().getModel().getValueAt(modelRow, 0);
        VentaDto venta = null;
        try {
            venta = UiContext.ventaSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener venta", ex);
            ErrorHandler.handle(ex);
        }
        String telefono = null;
        if (venta != null && venta.clienteId() != null) {
            try {
                ClienteDto cli = UiContext.clienteSvc().obtener(venta.clienteId());
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
                "¿Enviar comprobante por WhatsApp?")) return;
        String finalTel = PhoneUtils.stripToDigits(telefono);
        AsyncTasks.busy(view, () -> {
            UiContext.comprobanteSvc().enviarWhatsApp(id, new TelefonoDto(finalTel));
            return null;
        }, r -> JOptionPane.showMessageDialog(view,
                "Enviado a " + finalTel,
                "WhatsApp", JOptionPane.INFORMATION_MESSAGE),
                ErrorHandler::handle);
    }

    private LocalDateTime getDate(DatePickerField sp) {
        LocalDate d = sp.getDate();
        if (d == null) {
            return null;
        }
        return d.atStartOfDay();
    }

}

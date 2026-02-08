package com.comercialvalerio.presentation.controller.pedidos;

import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintException;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.dto.DetalleDto;
import com.comercialvalerio.application.dto.PagoCreateDto;
import com.comercialvalerio.application.dto.PagoDto;
import com.comercialvalerio.application.dto.PedidoDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.TelefonoDto;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.common.DlgMotivoCancelacion;
import com.comercialvalerio.presentation.ui.pedidos.DlgComprobantePedido;
import com.comercialvalerio.presentation.ui.pedidos.DlgPagoPedido;
import com.comercialvalerio.presentation.ui.pedidos.DlgPedidoDetalle;
import com.comercialvalerio.presentation.ui.pedidos.DlgPedidoEditar;
import com.comercialvalerio.presentation.ui.pedidos.FormSeguimientoPedidos;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.UserPrefs;
import com.comercialvalerio.presentation.util.PdfPrinter;
import com.comercialvalerio.presentation.util.NameUtils;
import com.comercialvalerio.common.DbConstraints;

import jakarta.ws.rs.WebApplicationException;

/** Controlador para {@link FormSeguimientoPedidos}. */
public class SeguimientoPedidosController {

    private final FormSeguimientoPedidos view;
    private static final Logger LOG = Logger.getLogger(SeguimientoPedidosController.class.getName());
    /** Identificador de la última actualización solicitada. */
    private volatile long refreshVersion = 0L;
    private static final String GENERIC_DNI = "00000000";
    private List<CategoriaDto> categorias = List.of();
    private List<ProductoDto>  productos  = List.of();
    private List<PedidoDto>    pedidos    = List.of();
    private java.util.Map<Integer, EmpleadoDto> empleadoMap = java.util.Map.of();
    private java.util.Map<Integer, ClienteDto>  clienteMap  = java.util.Map.of();

    public SeguimientoPedidosController(FormSeguimientoPedidos view) {
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

    /** Actualiza la tabla de pedidos aplicando el filtro de producto si está seleccionado. */
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
            if (desde == null && hasta == null) {
                pedidos = UiContext.pedidoSvc().listar();
            } else {
                pedidos = UiContext.pedidoSvc().listarPorRango(desde, hastaInc);
            }
            Integer prodId = getSelectedProductId();
            Integer catId = getSelectedCategoryId();
            Set<Integer> catProdIds;
            if (prodId == null && catId != null) {
                catProdIds = productos.stream()
                        .map(ProductoDto::idProducto)
                        .collect(Collectors.toSet());
            } else {
                catProdIds = Collections.emptySet();
            }
            return pedidos.stream()
                    .filter(p -> prodId == null || pedidoTieneProducto(p.idTransaccion(), prodId))
                    .filter(p -> prodId != null || catId == null || pedidoTieneCategoria(p.idTransaccion(), catProdIds))
                    .toList();
        }, list -> {
            if (version != refreshVersion) {
                return; // descartar resultados obsoletos
            }
            DefaultTableModel m = new DefaultTableModel(
                    new String[]{"ID","Fecha","F.Entrega","Cliente","Empleado","Total","Estado","Tipo"},0);
            for (PedidoDto p : list) {
                Object f1 = p.fecha();
                OffsetDateTime odt1 = DateFormatUtils.parseOffsetDateTime(f1);
                String fecha = odt1 == null ? "‐" : DateFormatUtils.formatServer(odt1);
                Object f2 = p.fechaHoraEntrega();
                OffsetDateTime odt2 = DateFormatUtils.parseOffsetDateTime(f2);

                String fechaEnt = odt2 == null ? "‐" : DateFormatUtils.formatServer(odt2);
                String cliNombre = p.clienteNombre();
                ClienteDto cd = clienteMap.get(p.clienteId());
                if (cd != null) {
                    cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
                }
                String empNombre = p.empleadoUsuario();
                EmpleadoDto ed = empleadoMap.get(p.empleadoId());
                if (ed != null) {
                    empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
                }

                m.addRow(new Object[]{
                        p.idTransaccion(),
                        fecha,
                        fechaEnt,
                        cliNombre,
                        empNombre,
                        p.totalNeto(),
                        p.estado(),
                        p.tipoPedido().getNombre()
                });
            }
            view.getTblPedidos().setModel(m);
            if (view.getTblPedidos().getColumnCount() > 0) {
                view.getTblPedidos().getColumnModel()
                        .removeColumn(view.getTblPedidos().getColumnModel().getColumn(0));
            }
            TableUtils.packColumns(view.getTblPedidos());
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
            sorter.setSortable(0, false);
            TableUtils.setNumericComparators(sorter, 5);
            view.getTblPedidos().setRowSorter(sorter);
            TableUtils.updateEmptyView(
                    view.getSpPedidos(),
                    view.getTblPedidos(),
                    view.getLblEmpty());
            view.updateButtons();
        }, ex -> {
            LOG.log(Level.SEVERE, "Error al listar pedidos", ex);
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

    private boolean pedidoTieneProducto(Integer idPedido, Integer idProd) {
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(idPedido);
            for (DetalleDto d : det) {
                if (idProd.equals(d.idProducto())) {
                    return true;
                }
            }
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al verificar producto en pedido", ex);
            ErrorHandler.handle(ex);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al verificar producto en pedido", ex);
            ErrorHandler.handle(ex);
        }
        return false;
    }

    private Integer getSelectedCategoryId() {
        int idx = view.getCboCategoria().getSelectedIndex();
        if (idx > 0 && idx - 1 < categorias.size()) {
            return categorias.get(idx - 1).idCategoria();
        }
        return null;
    }

    private boolean pedidoTieneCategoria(Integer idPedido, Set<Integer> prodIds) {
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(idPedido);
            for (DetalleDto d : det) {
                if (prodIds.contains(d.idProducto())) {
                    return true;
                }
            }
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al verificar categoría en pedido", ex);
            ErrorHandler.handle(ex);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al verificar categoría en pedido", ex);
            ErrorHandler.handle(ex);
        }
        return false;
    }

    /** Muestra los detalles completos del pedido seleccionado. */
    public void mostrarDetalle() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
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
            refreshTable();
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(view);
        DlgPedidoDetalle dlg = new DlgPedidoDetalle(owner);
        dlg.getLblId().setText(String.valueOf(ped.idTransaccion()));
        Object f1 = ped.fecha();
        OffsetDateTime odt1 = DateFormatUtils.parseOffsetDateTime(f1);
        dlg.getLblFecha().setText(
                odt1 == null ? "‐" : DateFormatUtils.formatServer(odt1));
        Object f2 = ped.fechaHoraEntrega();
        OffsetDateTime odt2 = DateFormatUtils.parseOffsetDateTime(f2);
        dlg.getLblEntrega().setText(
                odt2 == null ? "‐" : DateFormatUtils.formatServer(odt2));
        dlg.getLblDireccion().setText(ped.direccionEntrega());
        dlg.getLblTipo().setText(ped.tipoPedido().getNombre());
        dlg.getLblValeGas().setText(ped.usaValeGas() ? "Sí" : "No");
        String empNombre = ped.empleadoUsuario();
        EmpleadoDto ed = empleadoMap.get(ped.empleadoId());
        if (ed != null) {
            empNombre = NameUtils.formatNombreCorto(ed.nombres(), ed.apellidos());
        }
        dlg.getLblEmpleado().setText(empNombre);
        String cliNombre = ped.clienteNombre();
        ClienteDto cd = clienteMap.get(ped.clienteId());
        if (cd != null) {
            cliNombre = NameUtils.formatNombreCorto(cd.nombres(), cd.apellidos());
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

    /** Abre un diálogo para editar el pedido seleccionado. */
    public void editarPedido() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un pedido",
                    "Pedido no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        PedidoDto ped = null;
        try {
            ped = UiContext.pedidoSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al obtener pedido", ex);
            ErrorHandler.handle(ex);
        }
        if (ped == null) return;
        Window owner = SwingUtilities.getWindowAncestor(view);
        boolean dom = TipoPedido.DOMICILIO.equals(ped.tipoPedido());
        DlgPedidoEditar dlg = new DlgPedidoEditar(owner, dom);
        PedidoEditarController ctrl = new PedidoEditarController(dlg, ped);
        ctrl.cargarProductos();
        ctrl.cargarDetalles();
        DocumentListeners.attachDebounced(dlg.getTxtBuscar(), ctrl::cargarProductos);
        dlg.addSearchActionListener(e -> ctrl.cargarProductos());
        dlg.getBtnAdd().addActionListener(e -> ctrl.agregarDetalle());
        dlg.getBtnRemove().addActionListener(e -> ctrl.quitarDetalle());
        dlg.getChkValeGas().addActionListener(e -> ctrl.refrescarTotales());
        dlg.getBtnGuardar().addActionListener(e -> { ctrl.guardar(); refreshTable(); });
        dlg.setVisible(true);
    }

    /** Marca el pedido seleccionado como entregado vía REST. */
    public void marcarEntregado() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un pedido",
                    "Pedido no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Object est = view.getTblPedidos().getModel().getValueAt(modelRow, 6);
        if (est == null || !"En Proceso".equalsIgnoreCase(est.toString())) {
            JOptionPane.showMessageDialog(view,
                    "Sólo pedidos en 'En Proceso' pueden entregarse",
                    "Estado no válido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        try {
            List<String> faltantes = UiContext.pedidoSvc().verificarStockEntrega(id);
            if (!faltantes.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                        "Stock insuficiente para: " + String.join(", ", faltantes),
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error verificando stock", ex);
            ErrorHandler.handle(ex);
            return;
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error verificando stock", ex);
            ErrorHandler.handle(ex);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(view);
        BigDecimal total = BigDecimal.ZERO;
        Object val = view.getTblPedidos().getModel().getValueAt(modelRow, 5);
        if (val instanceof BigDecimal b) total = b;
        else if (val != null) total = new BigDecimal(val.toString());
        DlgPagoPedido dlg = new DlgPagoPedido(owner, total);
        dlg.getBtnGuardar().addActionListener(ev -> {
            List<PagoCreateDto> pagos = new ArrayList<>();
            if (dlg.getChkDigital().isSelected()) {
                String txt = dlg.getTxtDigital().getText().trim();
                if (!txt.isBlank()) {
                    pagos.add(new PagoCreateDto(2, new java.math.BigDecimal(txt)));
                }
            }
            if (dlg.getChkEfectivo().isSelected()) {
                String txt = dlg.getTxtEfectivo().getText().trim();
                if (!txt.isBlank()) {
                    pagos.add(new PagoCreateDto(1, new java.math.BigDecimal(txt)));
                }
            }
            if (pagos.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Ingrese un pago",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                UiContext.pedidoSvc().marcarEntregado(id, pagos);
                ((DefaultTableModel) view.getTblPedidos().getModel())
                        .setValueAt("Entregada", modelRow, 6);
                view.updateButtons();
                refreshTable();
                dlg.dispose();
                mostrarDlgComprobante(id);
            } catch (WebApplicationException ex) {
                LOG.log(Level.SEVERE, "Error al marcar entregado", ex);
                ErrorHandler.handle(ex);
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al marcar entregado", ex);
                ErrorHandler.handle(ex);
            }
        });
        dlg.setVisible(true);
    }

    private void mostrarDlgComprobante(Integer id) {
        Window owner = SwingUtilities.getWindowAncestor(view);
        DlgComprobantePedido dlg = new DlgComprobantePedido(owner);

        PedidoDto ped = null;
        try {
            ped = UiContext.pedidoSvc().obtener(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Error al obtener pedido", ex);
            ErrorHandler.handle(ex);
        }

        BigDecimal sub = BigDecimal.ZERO;
        try {
            List<DetalleDto> det = UiContext.detalleSvc().listar(id);
            for (DetalleDto d : det) {
                if (d.subtotal() != null) sub = sub.add(d.subtotal());
            }
            sub = sub.setScale(DbConstraints.PRECIO_SCALE,
                    java.math.RoundingMode.HALF_UP);
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Error al obtener detalles", ex);
            ErrorHandler.handle(ex);
        }

        BigDecimal cargo = BigDecimal.ZERO;
        BigDecimal desc  = BigDecimal.ZERO;
        if (ped != null) {
            try {
                if (TipoPedido.DOMICILIO.equals(ped.tipoPedido())) {
                    cargo = UiContext.parametroSistemaSvc()
                            .obtener("CARGO_REPARTO").valor();
                }
                if (ped.usaValeGas()) {
                    desc = UiContext.parametroSistemaSvc()
                            .obtener("DESCUENTO_VALE_GAS").valor();
                }
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "Error cargando parámetros", ex);
                ErrorHandler.handle(ex);
            }
        }

        BigDecimal total = sub.add(cargo).subtract(desc);
        dlg.setSubTotal(CurrencyUtils.format(sub));
        dlg.setCargo(CurrencyUtils.format(cargo));
        dlg.setTotal(CurrencyUtils.format(total));
        ClienteDto cli = null;
        if (ped != null && ped.clienteId() != null) {
            try {
                cli = UiContext.clienteSvc().obtener(ped.clienteId());
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "No se pudo obtener cliente", ex);
                ErrorHandler.handle(ex);
            }
        }
        if (cli != null) {
            String tel = cli.telefono() == null ? "" : cli.telefono();
            dlg.getTxtTelefono().setText(tel);
            boolean hasTel = !tel.isBlank();
            dlg.getTxtTelefono().setVisible(!hasTel);
            boolean generico = GENERIC_DNI.equals(cli.dni());
            dlg.getChkWhatsApp().setEnabled(!generico);
            if (generico) {
                dlg.getChkWhatsApp().setSelected(false);
            } else if (hasTel) {
                dlg.getChkWhatsApp().setSelected(true);
            }
        }

        dlg.getBtnConfirmar().addActionListener(ev -> confirmarComprobante(id, dlg));

        dlg.getBtnImprimir().addActionListener(ev ->
            AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().descargarPdf(id), pdf -> {
                try {
                    PdfPrinter.print(pdf);
                } catch (PrintException ex) {
                    LOG.log(Level.SEVERE, "Error al imprimir comprobante", ex);
                    ErrorHandler.handle(new IllegalStateException(
                            "Error al imprimir comprobante", ex));
                }
            })
        );

        dlg.getBtnDescargar().addActionListener(ev ->
            AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().obtenerPdf(id), dto -> {
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
                        ErrorHandler.handle(new IllegalStateException(
                                "Error al guardar archivo", ex));
                    }
                }
            })
        );

        dlg.setVisible(true);
    }

    void confirmarComprobante(Integer id, DlgComprobantePedido dlg) {
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

    /** Cancela el pedido seleccionado solicitando un motivo. */
    public void cancelarPedido() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione un pedido",
                    "Pedido no seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Object est = view.getTblPedidos().getModel().getValueAt(modelRow, 6);
        if (est == null || !"En Proceso".equalsIgnoreCase(est.toString())) {
            JOptionPane.showMessageDialog(view,
                    "Sólo pedidos en 'En Proceso' pueden cancelarse",
                    "Estado no válido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        Window owner = SwingUtilities.getWindowAncestor(view);
        DlgMotivoCancelacion dlg = new DlgMotivoCancelacion(owner);
        dlg.setVisible(true);
        String motivo = dlg.getController().getMotivo();
        if (motivo == null) return;
        try {
            UiContext.pedidoSvc().cancelar(id, new MotivoDto(motivo));
            ((DefaultTableModel) view.getTblPedidos().getModel())
                    .setValueAt("Cancelada", modelRow, 6);
            view.updateButtons();
            refreshTable();
        } catch (WebApplicationException ex) {
            LOG.log(Level.SEVERE, "Error al cancelar pedido", ex);
            ErrorHandler.handle(ex);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cancelar pedido", ex);
            ErrorHandler.handle(ex);
        }
    }

    /** Imprime la orden de compra de la fila seleccionada si está disponible. */
    public void reimprimirOrden() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.pedidoSvc().descargarOrden(id), pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                LOG.log(Level.SEVERE, "Error al imprimir la orden", ex);
                ErrorHandler.handle(new IllegalStateException(
                        "Error al imprimir la orden", ex));
            }
        }, ex -> {
            if (ex instanceof WebApplicationException wex &&
                    wex.getResponse() != null && wex.getResponse().getStatus() == 404) {
                JOptionPane.showMessageDialog(view,
                        "Orden de compra no encontrada",
                        "Orden", JOptionPane.WARNING_MESSAGE);
            } else {
                LOG.log(Level.SEVERE, "Error al descargar orden", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Descarga el PDF de la orden de compra para la fila seleccionada. */
    public void descargarOrden() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) return;
        if (!DialogUtils.confirmAction(view, "Descargar orden?")) return;
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.pedidoSvc().obtenerOrden(id), dto -> {
            java.io.File dir = UserPrefs.getPdfDirectory();
            JFileChooser fc = dir != null ? new JFileChooser(dir) : new JFileChooser();
            fc.setSelectedFile(new java.io.File(dto.nombreArchivo()));
            if (fc.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                UserPrefs.setPdfDirectory(fc.getSelectedFile().getParentFile());
                try {
                    java.nio.file.Files.write(fc.getSelectedFile().toPath(), dto.pdf());
                } catch (java.io.IOException ex) {
                    LOG.log(Level.SEVERE, "Error al guardar archivo", ex);
                    ErrorHandler.handle(new IllegalStateException(
                            "Error al guardar archivo", ex));
                }
            }
        }, ex -> {
            if (ex instanceof WebApplicationException wex &&
                    wex.getResponse() != null && wex.getResponse().getStatus() == 404) {
                JOptionPane.showMessageDialog(view,
                        "Orden de compra no encontrada",
                        "Orden", JOptionPane.WARNING_MESSAGE);
            } else {
                LOG.log(Level.SEVERE, "Error al descargar orden", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Reimprime el comprobante del pedido seleccionado si está disponible. */
    public void reimprimirComprobante() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
        AsyncTasks.busy(view, () -> UiContext.comprobanteSvc().descargarPdf(id), pdf -> {
            try {
                PdfPrinter.print(pdf);
            } catch (PrintException ex) {
                LOG.log(Level.SEVERE, "Error al imprimir el comprobante", ex);
                ErrorHandler.handle(new IllegalStateException(
                        "Error al imprimir el comprobante", ex));
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

    /** Descarga el comprobante PDF para el pedido seleccionado. */
    public void descargarComprobante() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) return;
        if (!DialogUtils.confirmAction(view, "Descargar comprobante?")) return;
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
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
                    ErrorHandler.handle(new IllegalStateException(
                            "Error al guardar archivo", ex));
                }
            }
        });
    }

    /** Envía la orden de compra por WhatsApp. */
    public void enviarOrdenWhatsApp() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow, 0);
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
        AsyncTasks.busy(view, () -> {
            UiContext.pedidoSvc().enviarOrdenWhatsApp(id, new TelefonoDto(finalTel));
            return null;
        }, r -> JOptionPane.showMessageDialog(view,
                "Enviado a " + finalTel,
                "WhatsApp", JOptionPane.INFORMATION_MESSAGE));
    }

    /** Envía el comprobante por WhatsApp. */
    public void enviarComprobanteWhatsApp() {
        int row = view.getTblPedidos().getSelectedRow();
        if (row < 0) return;
        int modelRow2 = view.getTblPedidos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblPedidos().getModel().getValueAt(modelRow2, 0);
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

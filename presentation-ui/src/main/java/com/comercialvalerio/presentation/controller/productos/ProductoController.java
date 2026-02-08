package com.comercialvalerio.presentation.controller.productos;

import java.math.BigDecimal;
import java.util.List;
import com.comercialvalerio.common.DbConstraints;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.PresentacionCUDto;
import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.TallaStockCUDto;
import com.comercialvalerio.application.dto.TipoProductoDto;
import com.comercialvalerio.application.dto.TipoPedido;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.categorias.DlgCategoriaNueva;
import com.comercialvalerio.presentation.ui.productos.DlgProductoDetalle;
import com.comercialvalerio.presentation.ui.productos.DlgProductoEditar;
import com.comercialvalerio.presentation.ui.productos.DlgProductoNuevo;
import com.comercialvalerio.presentation.ui.productos.FormGestionProductos;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.application.dto.EstadoNombre;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.application.dto.RolNombre;
import com.comercialvalerio.presentation.util.NumberUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador para {@link FormGestionProductos}. */
public class ProductoController {

    private final FormGestionProductos view;
    private static final Logger LOG = Logger.getLogger(ProductoController.class.getName());
    private List<CategoriaDto> categorias = List.of();
    private List<TipoProductoDto> tipos   = List.of();
    private List<ProductoDto> productos  = List.of();
    private Integer tipoOriginal = null;
    private java.util.List<String> dependencias = java.util.List.of();

    private record EditData(
            ProductoDto dto,
            List<CategoriaDto> categorias,
            List<TipoProductoDto> tipos) {}

    private record ComboData(List<CategoriaDto> categorias,
            List<TipoProductoDto> tipos) {}

    public ProductoController(FormGestionProductos view) {
        this.view = view;
    }

    public java.util.List<String> getDependencias() { return dependencias; }

    public void cargarDependencias() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) {
            dependencias = java.util.List.of();
            view.updateButtons();
            return;
        }
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow, 0);
        try {
            dependencias = UiContext.productoSvc().obtenerDependencias(id);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al cargar dependencias", ex);
            ErrorHandler.handle(ex);
            dependencias = java.util.List.of();
        }
        view.updateButtons();
    }

    /** Carga la lista de categorías en el cuadro combinado. */
    public void cargarCategorias() {
        AsyncTasks.busy(view, () -> {
            categorias = UiContext.categoriaSvc().listar();
            return categorias;
        }, list -> {
            JComboBox<String> cb = view.getCboCategoria();
            cb.removeAllItems();
            cb.addItem("Todas");
            for (CategoriaDto c : list) {
                cb.addItem(c.nombre());
            }
        });
    }

    /** Carga la lista de tipos de producto. */
    public void cargarTipos() {
        AsyncTasks.busy(view, () -> {
            tipos = UiContext.tipoProductoSvc().listar();
            return tipos;
        }, list -> {
            JComboBox<String> cb = view.getCboTipo();
            cb.removeAllItems();
            cb.addItem("Todos");
            for (TipoProductoDto t : list) {
                cb.addItem(t.nombre());
            }
        });
    }

    /** Recarga los combos de categoría y tipo para el diálogo proporcionado. */
    public void reloadCombos(DlgProductoNuevo dlg) {
        Object catSel = dlg.getCboCategoria().getSelectedItem();
        Object tipoSel = dlg.getCboTipo().getSelectedItem();
        AsyncTasks.busy(dlg, () -> {
            List<CategoriaDto> cats = UiContext.categoriaSvc().listar();
            List<TipoProductoDto> tps = UiContext.tipoProductoSvc().listar();
            return new ComboData(cats, tps);
        }, data -> {
            categorias = data.categorias();
            tipos = data.tipos();
            JComboBox<CategoriaDto> catBox = dlg.getCboCategoria();
            catBox.removeAllItems();
            for (CategoriaDto c : categorias) catBox.addItem(c);
            if (catSel instanceof CategoriaDto)
                catBox.setSelectedItem(catSel);
            JComboBox<TipoProductoDto> tipoBox = dlg.getCboTipo();
            tipoBox.removeAllItems();
            for (TipoProductoDto t : tipos) tipoBox.addItem(t);
            if (tipoSel instanceof TipoProductoDto tSel) {
                tipoBox.setSelectedItem(tSel);
                dlg.showExtras(tSel.nombre());
            } else {
                dlg.showExtras(null);
            }
        });
    }

    /** Abre el diálogo de nueva categoría y recarga el combo de categorías. */
    public void nuevaCategoria() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        new DlgCategoriaNueva(owner).setVisible(true);
        cargarCategorias();
    }

    /** Recarga la tabla de productos aplicando los filtros actuales. */
    public void refresh() {
        int idx = view.getCboCategoria().getSelectedIndex();
        Integer catId = null;
        if (idx > 0 && idx - 1 < categorias.size()) {
            catId = categorias.get(idx - 1).idCategoria();
        }
        int idt = view.getCboTipo().getSelectedIndex();
        Integer tipoId = null;
        if (idt > 0 && idt - 1 < tipos.size()) {
            tipoId = tipos.get(idt - 1).idTipoProducto();
        }
        String filtro = view.getTxtBuscar().getText().trim();
        String estado = (String) view.getCboEstado().getSelectedItem();
        Integer finalCat = catId;
        Integer finalTipo = tipoId;
        AsyncTasks.busy(view, () -> {
            java.util.List<ProductoDto> base = UiContext.productoSvc().listar(
                    filtro.isBlank() ? null : filtro,
                    finalCat,
                    finalTipo,
                    null,
                    null);
            if (!"Todos".equalsIgnoreCase(estado)) {
                String es = estado;
                base = base.stream()
                        .filter(p -> es.equalsIgnoreCase(p.estado()))
                        .toList();
            }
            productos = base;
            return null;
        }, v -> {
            actualizarTabla();
        });
    }

    /** Searches products using current text and category filters. */
    public void buscar() {
        int idx = view.getCboCategoria().getSelectedIndex();
        Integer catId = null;
        if (idx > 0 && idx - 1 < categorias.size()) {
            catId = categorias.get(idx - 1).idCategoria();
        }
        int idt = view.getCboTipo().getSelectedIndex();
        Integer tipoId = null;
        if (idt > 0 && idt - 1 < tipos.size()) {
            tipoId = tipos.get(idt - 1).idTipoProducto();
        }
        String filtro = view.getTxtBuscar().getText().trim();
        String estado = (String) view.getCboEstado().getSelectedItem();
        Integer finalCat = catId;
        Integer finalTipo = tipoId;
        AsyncTasks.busy(view, () -> {
            java.util.List<ProductoDto> base = UiContext.productoSvc().listar(
                    filtro.isBlank() ? null : filtro,
                    finalCat,
                    finalTipo,
                    null,
                    null);
            if (!"Todos".equalsIgnoreCase(estado)) {
                String es = estado;
                base = base.stream()
                        .filter(p -> es.equalsIgnoreCase(p.estado()))
                        .toList();
            }
            productos = base;
            return null;
        }, v -> actualizarTabla());
    }

    /** Actualiza la tabla con la lista actual de productos. */
    private void actualizarTabla() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID","Producto","Categoría","Tipo","P.Unit","Stock","Estado","Stock bajo umbral"},0);
        boolean anyFractional = productos.stream()
                .anyMatch(p -> p.stockActual() != null
                        && p.stockActual().stripTrailingZeros().scale() > 0);
        for (ProductoDto p : productos) {
            String alerta = "";
            boolean umbral = p.stockActual() != null && p.umbral() != null
                    && p.stockActual().compareTo(p.umbral()) <= 0;
            if (EstadoNombre.INACTIVO_POR_UMBRAL.getNombre()
                    .equalsIgnoreCase(p.estado()) || umbral) {
                alerta = "Stock bajo umbral";
            }
            m.addRow(new Object[]{
                    p.idProducto(),
                    p.nombre(),
                    p.categoriaNombre(),
                    p.tipoProductoNombre(),
                    NumberUtils.formatScale(p.precioUnitario(),
                            DbConstraints.PRECIO_SCALE),
                    anyFractional
                            ? NumberUtils.formatScale(p.stockActual(), DbConstraints.STOCK_SCALE)
                            : NumberUtils.formatPlain(p.stockActual()),
                    p.estado(),
                    alerta
            });
        }
        JTable tbl = view.getTblProductos();
        tbl.setModel(m);
        if (tbl.getColumnCount() > 0) {
            tbl.getColumnModel().removeColumn(tbl.getColumnModel().getColumn(0));
        }
        if (m.getRowCount() == 0 && view.getCboTipo().getSelectedIndex() > 0) {
            view.getLblEmpty().setText("No se encontraron productos para este tipo");
        } else if (m.getRowCount() == 0) {
            view.getLblEmpty().setText("No se encontraron productos");
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        sorter.setSortable(0, false);
        TableUtils.setNumericComparators(sorter, 4, 5);
        tbl.setRowSorter(sorter);
        TableUtils.packColumns(tbl);
        view.getSpProductos().revalidate();
        TableUtils.updateEmptyView(
                view.getSpProductos(),
                view.getTblProductos(),
                view.getLblEmpty());
        view.updateButtons();
    }

    /** Abre un diálogo para crear un nuevo producto. */
    public void nuevo() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        DlgProductoNuevo dlg = new DlgProductoNuevo(owner);
        dlg.setController(this);
        AsyncTasks.busy(view, () -> {
            categorias = UiContext.categoriaSvc().listar();
            tipos = UiContext.tipoProductoSvc().listar();
            return null;
        }, v -> {
            dlg.getCboCategoria().removeAllItems();
            for (CategoriaDto c : categorias) {
                dlg.getCboCategoria().addItem(c);
            }
            dlg.getCboTipo().removeAllItems();
            TipoProductoDto unidadFija = null;
            for (TipoProductoDto t : tipos) {
                dlg.getCboTipo().addItem(t);
                if (unidadFija == null && "Unidad fija".equalsIgnoreCase(t.nombre())) {
                    unidadFija = t;
                }
            }
            if (unidadFija != null) {
                dlg.getCboTipo().setSelectedItem(unidadFija);
                dlg.showExtras(unidadFija.nombre());
            }
            dlg.getCboTipoPedido().setSelectedItem(TipoPedido.ESPECIAL);
        });

        dlg.getBtnGuardar().addActionListener(ev -> {
            TableUtils.stopEditing(dlg.getTblTallas());
            TableUtils.stopEditing(dlg.getTblPresentaciones());
            ProductoCUDto dto = buildDto(dlg);
            if (dto == null) return;
            AsyncTasks.busy(dlg, () -> {
                UiContext.productoSvc().crear(dto);
                return null;
            }, v -> {
                dlg.dispose();
                refresh();
                raven.toast.Notifications.getInstance()
                        .show(raven.toast.Notifications.Type.SUCCESS,
                                "Producto registrado");
            });
        });

        dlg.setVisible(true);
    }

    /** Abre el diálogo de edición para el producto seleccionado. */
    public void editarSeleccionado() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow,0);
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        AsyncTasks.busy(view, () -> {
            ProductoDto dto = UiContext.productoSvc().obtener(id);
            List<CategoriaDto> cats = UiContext.categoriaSvc().listar();
            List<TipoProductoDto> tps = UiContext.tipoProductoSvc().listar();
            return new EditData(dto, cats, tps);
        }, data -> {
            ProductoDto dto = data.dto();
            categorias = data.categorias();
            tipos = data.tipos();
            DlgProductoEditar dlg = new DlgProductoEditar(owner);
            dlg.setController(this);
            tipoOriginal = dto.tipoProductoId();

            dlg.getCboCategoria().removeAllItems();
            CategoriaDto catSel = null;
            for (CategoriaDto c : categorias) {
                dlg.getCboCategoria().addItem(c);
                if (c.idCategoria().equals(dto.categoriaId())) catSel = c;
            }
            if (catSel != null) dlg.getCboCategoria().setSelectedItem(catSel);

            dlg.getCboTipo().removeAllItems();
            TipoProductoDto tipoSel = null;
            for (TipoProductoDto t : tipos) {
                dlg.getCboTipo().addItem(t);
                if (t.idTipoProducto().equals(dto.tipoProductoId())) tipoSel = t;
            }
            boolean isFrac = false;
            if (tipoSel != null) {
                dlg.getCboTipo().setSelectedItem(tipoSel);
                dlg.showExtras(tipoSel.nombre());
                isFrac = "Fraccionable".equalsIgnoreCase(tipoSel.nombre());
            }

            dlg.getTxtNombre().setText(dto.nombre());
            dlg.getTxtDescripcion().setText(dto.descripcion());
            dlg.getTxtUnidad().setText(dto.unidadMedida());
            dlg.getTxtPrecioUnitario().setText(
                    dto.precioUnitario() == null ? "" : dto.precioUnitario().toString());
            dlg.getChkMayorista().setSelected(dto.mayorista());
            dlg.getChkParaPedido().setSelected(dto.paraPedido());
            dlg.getCboTipoPedido().setSelectedItem(dto.tipoPedidoDefault());
            if (dto.minMayorista() != null)
                dlg.getSpnMinMayorista().setValue(dto.minMayorista());
            if (dto.precioMayorista() != null)
                dlg.getTxtPrecioMayorista().setText(dto.precioMayorista().toString());
            dlg.getTxtStockActual().setText(
                    dto.stockActual() == null ? "" : dto.stockActual().toString());
            dlg.getTxtUmbral().setText(
                    dto.umbral().stripTrailingZeros().toPlainString());
            dlg.getCboTipo().setEnabled(false);
            // El stock inicial no debe mostrarse durante la edición
            dlg.getLblStockInicial().setVisible(false);
            dlg.getTxtStockActual().setVisible(false);

            if (tipoSel != null) {
                if ("Vestimenta".equalsIgnoreCase(tipoSel.nombre())) {
                    java.util.Set<Integer> bloqueadas = new java.util.HashSet<>();
                    UiContext.tallaStockSvc().listarTodosPorProducto(id)
                            .forEach(ts -> {
                                dlg.getModelTallas().addRow(new Object[]{ts.idTallaStock(), ts.talla(), ts.stock(), ts.estado()});
                                var deps = UiContext.tallaStockSvc().obtenerDependencias(ts.idTallaStock());
                                if (!deps.isEmpty()) bloqueadas.add(ts.idTallaStock());
                            });
                    dlg.setTallasBloqueadas(bloqueadas);
                } else if ("Fraccionable".equalsIgnoreCase(tipoSel.nombre())) {
                    UiContext.presentacionSvc().listarTodosPorProducto(id)
                        .forEach(pr -> dlg.getModelPresentaciones()
                            .addRow(new Object[]{pr.idPresentacion(),
                                    pr.cantidad(), pr.precio(), pr.estado()}));
                }
            }

            dlg.getBtnGuardar().addActionListener(ev -> {
                TableUtils.stopEditing(dlg.getTblTallas());
                TableUtils.stopEditing(dlg.getTblPresentaciones());
                ProductoCUDto chg = buildDto(dlg);
                if (chg == null) return;
                AsyncTasks.busy(view, () -> {
                    UiContext.productoSvc().actualizar(id, chg);
                    return null;
                }, v2 -> {
                    dlg.dispose();
                    refresh();
                    raven.toast.Notifications.getInstance()
                            .show(raven.toast.Notifications.Type.SUCCESS,
                                    "Producto actualizado");
                });
            });

            dlg.setVisible(true);
            tipoOriginal = null;
        });
    }

    /** Muestra tallas o presentaciones del producto seleccionado. */
    public void verDetalle() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow,0);
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(view);
        AsyncTasks.busy(view, () -> UiContext.productoSvc().obtener(id), dto -> {
            DlgProductoDetalle dlg = new DlgProductoDetalle(owner);
            dlg.getLblNombre().setText(dto.nombre());
            dlg.getLblCategoria().setText(dto.categoriaNombre());
            dlg.getLblTipo().setText(dto.tipoProductoNombre());
            dlg.getLblPrecioTxt().setVisible(false);
            dlg.getLblPrecio().setVisible(false);

            DefaultTableModel m;
            if ("Vestimenta".equalsIgnoreCase(dto.tipoProductoNombre())) {
                dlg.getLblTable().setText("Tallas");
                dlg.getLblPrecioTxt().setVisible(true);
                dlg.getLblPrecio().setText(NumberUtils.formatPlain(dto.precioUnitario()));
                dlg.getLblPrecio().setVisible(true);
                // las tallas comparten el mismo precio unitario
                m = new DefaultTableModel(
                        new String[]{"ID","Talla","Stock","Estado"},0);
                UiContext.tallaStockSvc().listarTodosPorProducto(id)
                        .forEach(t -> m.addRow(new Object[]{t.idTallaStock(),
                                t.talla(),
                                NumberUtils.formatInteger(t.stock()),
                                t.estado()}));
            } else if ("Fraccionable".equalsIgnoreCase(dto.tipoProductoNombre())) {
                dlg.getLblTable().setText("Presentaciones");
                m = new DefaultTableModel(
                        new String[]{"ID","Cantidad","Precio","Estado"},0);
                UiContext.presentacionSvc().listarTodosPorProducto(id)
                        .forEach(p -> m.addRow(new Object[]{p.idPresentacion(),
                                p.cantidad(), p.precio(), p.estado()}));
            } else {
                dlg.getLblTable().setText("Sin detalle");
                m = new DefaultTableModel();
            }
            dlg.getTblDatos().setModel(m);
            if (dlg.getTblDatos().getColumnCount() > 0)
                dlg.getTblDatos().getColumnModel().removeColumn(
                        dlg.getTblDatos().getColumnModel().getColumn(0));
            TableUtils.packColumns(dlg.getTblDatos());
            if (UiContext.getUsuarioActual()!=null) {
                boolean admin = RolNombre.fromNombre(
                        UiContext.getUsuarioActual().rolNombre()) == RolNombre.ADMINISTRADOR;
                if (admin) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem act = new JMenuItem("Activar");
                    JMenuItem des = new JMenuItem("Desactivar");
                    menu.add(act); menu.add(des);
                    dlg.getTblDatos().setComponentPopupMenu(menu);
                    act.addActionListener(e -> cambiarEstadoDetalle(dlg, true));
                    des.addActionListener(e -> cambiarEstadoDetalle(dlg, false));
                }
            }
            TableUtils.updateEmptyView(dlg.getSpDatos(), dlg.getTblDatos(), dlg.getLblEmpty());
            dlg.setVisible(true);
        });
    }

    /** Cambia el estado del producto seleccionado a Activo. */
    public void activarSeleccionado() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow,0);
        cambiarEstado(id, EstadoNombre.ACTIVO.getNombre());
    }


    private void cambiarEstado(Integer id, String nuevo) {
        String tipo = productos.stream()
                .filter(p -> p.idProducto().equals(id))
                .map(ProductoDto::tipoProductoNombre)
                .findFirst().orElse(null);
        AsyncTasks.busy(view, () -> {
            UiContext.productoSvc().cambiarEstado(id, new CambiarEstadoDto(nuevo));
            return null;
        }, v -> {
            refresh();
            if (EstadoNombre.INACTIVO.getNombre().equalsIgnoreCase(nuevo)) {
                String msg;
                if ("Vestimenta".equalsIgnoreCase(tipo)) {
                    msg = "Las tallas asociadas fueron marcadas como Inactivo";
                } else if ("Fraccionable".equalsIgnoreCase(tipo)) {
                    msg = "Las presentaciones asociadas fueron marcadas como Inactivo";
                } else {
                    msg = "Producto desactivado";
                }
                JOptionPane.showMessageDialog(view,
                        msg,
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void eliminar(Integer id) {
        AsyncTasks.busy(view, () -> {
            UiContext.productoSvc().eliminar(id);
            return null;
        }, v -> {
            refresh();
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Producto eliminado");
        });
    }

    /** Devuelve verdadero si el usuario actual tiene rol de administrador. */
    public boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre())
                        == RolNombre.ADMINISTRADOR;
    }

    /** Desactiva el producto seleccionado tras confirmación. */
    public void desactivarSeleccionado() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow,0);
        if (DialogUtils.confirmAction(view,
                "¿Desactivar el producto seleccionado?")) {
            cambiarEstado(id, EstadoNombre.INACTIVO.getNombre());
        }
    }

    /** Elimina el producto seleccionado tras confirmación. */
    public void eliminarSeleccionado() {
        int row = view.getTblProductos().getSelectedRow();
        if (row < 0) return;
        int modelRow = view.getTblProductos().convertRowIndexToModel(row);
        Integer id = (Integer) view.getTblProductos().getModel().getValueAt(modelRow,0);
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(view,
                    "Sólo un administrador puede eliminar productos",
                    "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!dependencias.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "No se puede eliminar, dependencias:\n- " + String.join("\n- ", dependencias),
                    "Dependencias", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (DialogUtils.confirmAction(view,
                "¿Eliminar permanentemente el producto seleccionado?\nEsta acción es irreversible")) {
            eliminar(id);
        }
    }

    private void cambiarEstadoDetalle(DlgProductoDetalle dlg, boolean activar) {
        int row = dlg.getTblDatos().getSelectedRow();
        if (row < 0) return;
        int modelRow = dlg.getTblDatos().convertRowIndexToModel(row);
        Integer id = (Integer) dlg.getTblDatos().getModel().getValueAt(modelRow,0);
        if (id == null) return;
        if (dlg.getLblTable().getText().startsWith("Talla")) {
            AsyncTasks.busy(dlg, () -> {
                if (activar) UiContext.tallaStockSvc().activar(id);
                else UiContext.tallaStockSvc().desactivar(id);
                return null;
            }, v -> {
                String est = activar?EstadoNombre.ACTIVO.getNombre():
                        EstadoNombre.INACTIVO.getNombre();
                dlg.getTblDatos().getModel().setValueAt(est, modelRow,3);
            });
        } else if (dlg.getLblTable().getText().startsWith("Present")) {
            AsyncTasks.busy(dlg, () -> {
                if (activar) UiContext.presentacionSvc().activar(id);
                else UiContext.presentacionSvc().desactivar(id);
                return null;
            }, v -> {
                String est = activar?EstadoNombre.ACTIVO.getNombre():
                        EstadoNombre.INACTIVO.getNombre();
                dlg.getTblDatos().getModel().setValueAt(est, modelRow,3);
            });
        }
    }

    /** Desactiva la talla seleccionada en el diálogo de edición. */
    public void desactivarTalla(DlgProductoNuevo dlg) {
        int row = dlg.getTblTallas().getSelectedRow();
        if (row < 0) return;
        int modelRow = dlg.getTblTallas().convertRowIndexToModel(row);
        Integer id = (Integer) dlg.getTblTallas().getModel().getValueAt(modelRow,0);
        if (id == null) return;
        AsyncTasks.busy(dlg, () -> {
            UiContext.tallaStockSvc().desactivar(id);
            return null;
        }, v -> dlg.getTblTallas().getModel().setValueAt(
                EstadoNombre.INACTIVO.getNombre(), modelRow,3));
    }

    /** Activa la talla seleccionada en el diálogo de edición. */
    public void activarTalla(DlgProductoNuevo dlg) {
        int row = dlg.getTblTallas().getSelectedRow();
        if (row < 0) return;
        int modelRow = dlg.getTblTallas().convertRowIndexToModel(row);
        Integer id = (Integer) dlg.getTblTallas().getModel().getValueAt(modelRow,0);
        if (id == null) return;
        AsyncTasks.busy(dlg, () -> {
            UiContext.tallaStockSvc().activar(id);
            return null;
        }, v -> dlg.getTblTallas().getModel().setValueAt(
                EstadoNombre.ACTIVO.getNombre(), modelRow,3));
    }

    /** Desactiva la presentación seleccionada en el diálogo de edición. */
    public void desactivarPresentacion(DlgProductoNuevo dlg) {
        int row = dlg.getTblPresentaciones().getSelectedRow();
        if (row < 0) return;
        int modelRow = dlg.getTblPresentaciones().convertRowIndexToModel(row);
        Integer id = (Integer) dlg.getTblPresentaciones().getModel().getValueAt(modelRow,0);
        if (id == null) return;
        AsyncTasks.busy(dlg, () -> {
            UiContext.presentacionSvc().desactivar(id);
            return null;
        }, v -> dlg.getTblPresentaciones().getModel().setValueAt(
                EstadoNombre.INACTIVO.getNombre(), modelRow,3));
    }

    /** Activa la presentación seleccionada en el diálogo de edición. */
    public void activarPresentacion(DlgProductoNuevo dlg) {
        int row = dlg.getTblPresentaciones().getSelectedRow();
        if (row < 0) return;
        int modelRow = dlg.getTblPresentaciones().convertRowIndexToModel(row);
        Integer id = (Integer) dlg.getTblPresentaciones().getModel().getValueAt(modelRow,0);
        if (id == null) return;
        AsyncTasks.busy(dlg, () -> {
            UiContext.presentacionSvc().activar(id);
            return null;
        }, v -> dlg.getTblPresentaciones().getModel().setValueAt(
                EstadoNombre.ACTIVO.getNombre(), modelRow,3));
    }

    /** Construye un DTO a partir de los campos del diálogo. Devuelve {@code null} si hay error de validación. */
    private ProductoCUDto buildDto(DlgProductoNuevo dlg) {
        String nombre = dlg.getTxtNombre().getText().trim();
        String desc = dlg.getTxtDescripcion().getText().trim();
        CategoriaDto cat = (CategoriaDto) dlg.getCboCategoria().getSelectedItem();
        TipoProductoDto tipo = (TipoProductoDto) dlg.getCboTipo().getSelectedItem();
        String unidad = dlg.getTxtUnidad().getText().trim();
        boolean editing = dlg instanceof DlgProductoEditar;

        boolean ok = true;
        boolean isVest = tipo != null && "Vestimenta".equalsIgnoreCase(tipo.nombre());
        boolean isFrac = tipo != null && "Fraccionable".equalsIgnoreCase(tipo.nombre());

        if (!verifyInput(dlg.getTxtNombre())) ok = false;
        if (!verifyInput(dlg.getCboCategoria())) ok = false;
        if (!verifyInput(dlg.getCboTipo())) ok = false;
        if (!verifyInput(dlg.getTxtUnidad())) ok = false;
        if (!isFrac && !verifyInput(dlg.getTxtPrecioUnitario())) ok = false;
        if (dlg.getChkMayorista().isSelected()
                && !verifyInput(dlg.getTxtPrecioMayorista())) ok = false;
        if (!editing && !isVest && !isFrac
                && !verifyInput(dlg.getTxtStockActual())) ok = false;
        if (!verifyInput(dlg.getTxtUmbral())) ok = false;
        if (!ok) {
            JOptionPane.showMessageDialog(dlg,
                    "Complete los datos requeridos",
                    "Datos incompletos", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        BigDecimal pUnit = null;
        if (!isFrac) {
            String pTxt = dlg.getTxtPrecioUnitario().getText().trim();
            if (!pTxt.isBlank()) {
                pUnit = parseDecimal(dlg.getTxtPrecioUnitario(), "Precio Unitario",
                        DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE, dlg);
                if (pUnit == null) return null;
            }
        }

        boolean mayorista = dlg.getChkMayorista().isSelected();
        boolean paraPed = dlg.getChkParaPedido().isSelected();
        TipoPedido tPedDef = paraPed
                ? (TipoPedido) dlg.getCboTipoPedido().getSelectedItem()
                : null;
        int minMay = ((Number) dlg.getSpnMinMayorista().getValue()).intValue();

        String pMayTxt = dlg.getTxtPrecioMayorista().getText().trim();
        BigDecimal pMay = pMayTxt.isBlank() ? BigDecimal.ZERO
                : parseDecimal(
                        dlg.getTxtPrecioMayorista(),
                        "Precio Mayorista",
                        DbConstraints.PRECIO_PRECISION,
                        DbConstraints.PRECIO_SCALE,
                        dlg);
        if (pMay == null) return null;

        if (mayorista && pUnit != null && pMay.compareTo(pUnit) >= 0) {
            JOptionPane.showMessageDialog(dlg,
                    "El precio mayorista debe ser menor al precio unitario",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal stock = null;
        if (!editing && !isVest) {
            String sTxt = dlg.getTxtStockActual().getText().trim();
            if (sTxt.isBlank()) {
                stock = BigDecimal.ZERO;
            } else {
                stock = parseDecimal(dlg.getTxtStockActual(), "Stock Inicial",
                        DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE, dlg);
                if (stock == null) return null;
            }
        }

        BigDecimal umbral = parseDecimal(dlg.getTxtUmbral(), "Umbral",
                DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE, dlg);
        if (umbral == null) return null;

        java.util.List<TallaStockCUDto> tallas = null;
        java.util.List<PresentacionCUDto> pres = null;
        if (tipo != null && "Vestimenta".equalsIgnoreCase(tipo.nombre())) {
            tallas = new ArrayList<>();
            var model = dlg.getModelTallas();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object t = model.getValueAt(i, 1);
                Object s = model.getValueAt(i, 2);
                if (t == null) continue;
                JTextField tmpField = new JTextField(String.valueOf(s));
                BigDecimal st = parseDecimal(tmpField, "Stock",
                        DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE, dlg);
                if (st == null) return null;
                Integer idTs = null;
                if (editing) {
                    Object idObj = model.getValueAt(i, 0);
                    if (idObj instanceof Integer) idTs = (Integer) idObj;
                }
                tallas.add(new TallaStockCUDto(idTs, t.toString(), st));
            }
        } else if (tipo != null && "Fraccionable".equalsIgnoreCase(tipo.nombre())) {
            pres = new ArrayList<>();
            var model = dlg.getModelPresentaciones();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object c = model.getValueAt(i, 1);
                Object p = model.getValueAt(i, 2);
                if (c == null) continue;
                JTextField tmpCant = new JTextField(String.valueOf(c));
                BigDecimal cant = parseDecimal(tmpCant, "Cantidad",
                        DbConstraints.CANTIDAD_PRECISION, DbConstraints.CANTIDAD_SCALE, dlg);
                if (cant == null) return null;
                JTextField tmpPrec = new JTextField(String.valueOf(p));
                BigDecimal prec = parseDecimal(tmpPrec, "Precio",
                        DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE, dlg);
                if (prec == null) return null;
                Integer idPr = null;
                if (editing) {
                    Object idObj = model.getValueAt(i, 0);
                    if (idObj instanceof Integer) idPr = (Integer) idObj;
                }
                pres.add(new PresentacionCUDto(idPr, cant, prec));
            }
        }

        return new ProductoCUDto(nombre, desc,
                cat==null?null:cat.idCategoria(),
                editing ? tipoOriginal : (tipo==null?null:tipo.idTipoProducto()),
                unidad, pUnit, mayorista, paraPed,
                tPedDef,
                minMay, pMay, stock, umbral,
                tallas, pres);
    }

    /** Verifica un componente usando su {@link InputVerifier} si existe. */
    private boolean verifyInput(javax.swing.JComponent comp) {
        var verifier = comp.getInputVerifier();
        return verifier == null || verifier.verify(comp);
    }

    /**
     * Analiza un valor decimal validando su precisión.
     *
     * @param fieldComp componente de texto origen
     * @param field     nombre legible del campo para mensajes de error
     * @param precision número máximo de dígitos permitidos
     * @param scale     número máximo de decimales permitidos
     * @param parent    diálogo padre para los mensajes
     */
    private BigDecimal parseDecimal(JTextField fieldComp, String field,
                                    int precision, int scale, JDialog parent) {
        String txt = fieldComp.getText().trim();
        if (txt.contains("-")) {
            fieldComp.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(parent,
                    field + " no puede ser negativo",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (!txt.matches("\\d+(\\.\\d+)?")) {
            fieldComp.putClientProperty(FlatClientProperties.OUTLINE, "error");
            JOptionPane.showMessageDialog(parent,
                    field + " debe ser numérico",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        fieldComp.putClientProperty(FlatClientProperties.OUTLINE, null);
        try {
            BigDecimal val = new BigDecimal(txt);
            if (val.signum() < 0) {
                fieldComp.putClientProperty(FlatClientProperties.OUTLINE, "error");
                JOptionPane.showMessageDialog(parent,
                        field + " no puede ser negativo",
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            int integerDigits = val.precision() - val.scale();
            int maxIntegers = precision - scale;
            if (integerDigits > maxIntegers) {
                fieldComp.putClientProperty(FlatClientProperties.OUTLINE, "error");
                JOptionPane.showMessageDialog(parent,
                        String.format("%s fuera de rango (%d,%d)",
                                field, precision, scale),
                        "Dato inválido", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            val = val.setScale(scale, java.math.RoundingMode.DOWN);
            return val;
        } catch (NumberFormatException ex) {
            fieldComp.putClientProperty(FlatClientProperties.OUTLINE, "error");
            ErrorHandler.handle(new IllegalArgumentException(field + " inválido", ex));
            return null;
        }
    }
}

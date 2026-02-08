package com.comercialvalerio.presentation.controller.productos;

import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.TipoMovimientoDto;
import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.application.dto.PresentacionDto;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.ui.common.DlgObservacion;
import com.comercialvalerio.presentation.ui.productos.FormGestionInventario;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.util.TableModelUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.application.dto.RolNombre;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador para {@link FormGestionInventario}. */
public class GestionInventarioController {

    private final FormGestionInventario view;
    private static final Logger LOG = Logger.getLogger(GestionInventarioController.class.getName());
    private List<CategoriaDto> categorias = List.of();
    private List<ProductoDto>  productos  = List.of();
    private List<TallaStockDto>   tallas = List.of();
    private List<PresentacionDto> presentaciones = List.of();

    /**
     * Restablece la vista limpiando la selección y ocultando controles
     * adicionales.
     */
    private void resetView() {
        SwingUtilities.invokeLater(() -> {
            view.getTblInventario().clearSelection();
            view.getSpnCantidad().setValue(1);
            view.getSpnVeces().setValue(1);
            cargarExtras();
            view.updateButtons();
        });
    }

    public GestionInventarioController(FormGestionInventario view) {
        this.view = view;
    }

    /** Carga las categorías en el cuadro combinado. */
    public void cargarCategorias() {
        // Cargar categorías en segundo plano para no bloquear el EDT
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

    /** Carga productos según la categoría seleccionada. */
    public void cargarProductos() {
        int idx = view.getCboCategoria().getSelectedIndex();
        Integer catId = null;
        if (idx > 0 && idx - 1 < categorias.size()) {
            catId = categorias.get(idx - 1).idCategoria();
        }
        Integer finalCat = catId;
        // Obtener productos en segundo plano mientras se muestra la capa de carga
        AsyncTasks.busy(view, () -> {
            productos = UiContext.productoSvc().listar(null, finalCat, null, null, null);
            return productos;
        }, list -> buscarProductos(view.getTxtBuscar().getText().trim()));
    }

    /** Filtra los productos cargados usando el texto indicado y refresca la tabla. */
    public void buscarProductos(String filtro) {
        SwingUtilities.invokeLater(() -> {
            try {
                List<ProductoDto> lista = productos;
                if (filtro != null && !filtro.isBlank()) {
                    String pat = filtro.toUpperCase(Locale.ROOT);
                    lista = productos.stream()
                            .filter(p -> p.nombre().toUpperCase(Locale.ROOT).contains(pat))
                            .toList();
                }
                refreshTable(lista);
                cargarExtras();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al buscar productos", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Populates table with given product list. */
    private void refreshTable(List<ProductoDto> lista) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] cols = {"ID","Producto","Unidad","Stock","Umbral"};
                DefaultTableModel m = TableModelUtils.createModel(
                        view.getTblInventario(), cols, new int[]{3,4}, 0);
                boolean fracStock = lista.stream()
                        .anyMatch(p -> p.stockActual() != null
                                && p.stockActual().stripTrailingZeros().scale() > 0);
                boolean fracUmbral = lista.stream()
                        .anyMatch(p -> p.umbral() != null
                                && p.umbral().stripTrailingZeros().scale() > 0);
                for (ProductoDto p : lista) {
                    String stock = fracStock
                            ? NumberUtils.formatScale(p.stockActual(), DbConstraints.STOCK_SCALE)
                            : NumberUtils.formatPlain(p.stockActual());
                    String umbral = fracUmbral
                            ? NumberUtils.formatScale(p.umbral(), DbConstraints.STOCK_SCALE)
                            : NumberUtils.formatPlain(p.umbral());
                    m.addRow(new Object[]{
                            p.idProducto(),
                            p.nombre(),
                            p.unidadMedida(),
                            stock,
                            umbral
                    });
                }
                JTable tbl = view.getTblInventario();
                tbl.clearSelection();
                TableUtils.packColumns(tbl);
                TableUtils.updateEmptyView(
                        view.getSpInventario(),
                        view.getTblInventario(),
                        view.getLblEmpty());
                view.updateButtons();
                cargarExtras();
            } catch (RuntimeException ex) {
                LOG.log(Level.SEVERE, "Error al refrescar tabla", ex);
                ErrorHandler.handle(ex);
            }
        });
    }

    /** Sobrecarga conveniente que usa la lista completa de productos. */
    public void refreshTable() {
        refreshTable(productos);
    }

    /** Carga tallas o presentaciones para el producto seleccionado. */
    public void cargarExtras() {
        JComboBox<String> cbTalla = view.getCboTalla();
        JComboBox<String> cbPres  = view.getCboPresentacion();
        cbTalla.setVisible(false);
        view.getLblTalla().setVisible(false);
        cbPres.setVisible(false);
        view.getLblPresentacion().setVisible(false);
        view.getSpnVeces().setVisible(false);
        view.getLblCantidad().setVisible(true);
        view.getSpnCantidad().setVisible(true);
        cbTalla.removeAllItems();
        cbPres.removeAllItems();
        tallas = List.of();
        presentaciones = List.of();
        int row = view.getTblInventario().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = view.getTblInventario().convertRowIndexToModel(row);
        Integer idProd = (Integer) view.getTblInventario().getModel().getValueAt(modelRow,0);
        ProductoDto prod = productos.stream()
                .filter(p -> p.idProducto().equals(idProd))
                .findFirst().orElse(null);
        if (prod == null) return;
        String tipo = prod.tipoProductoNombre();
        if ("Vestimenta".equalsIgnoreCase(tipo)) {
            AsyncTasks.busy(view, () -> {
                tallas = UiContext.tallaStockSvc().listarPorProducto(idProd);
                return tallas;
            }, list -> {
                for (TallaStockDto t : list) {
                    cbTalla.addItem(t.talla() + " (" + t.stock() + ")");
                }
                cbTalla.setVisible(true);
                view.getLblTalla().setVisible(true);
            });
        } else if ("Fraccionable".equalsIgnoreCase(tipo)) {
            AsyncTasks.busy(view, () -> {
                presentaciones = UiContext.presentacionSvc().listarPorProducto(idProd);
                return presentaciones;
            }, list -> {
                for (PresentacionDto p : list) {
                    cbPres.addItem(p.cantidad().toPlainString());
                }
                cbPres.setVisible(true);
                view.getLblPresentacion().setVisible(true);
                view.getLblCantidad().setVisible(true);
                view.getSpnCantidad().setVisible(true);
            });
        }
    }

    /** Registra un movimiento de entrada de stock. */
    public void ingresarStock() {
        realizarMovimiento("Entrada", false);
    }

    /** Registra un movimiento de ajuste de stock. */
    public void ajustarStock() {
        boolean admin = isAdmin();
        if (!admin) {
            JOptionPane.showMessageDialog(view,
                    "Solo un administrador puede ajustar el stock",
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        realizarMovimiento("Ajuste", true);
    }

    private void realizarMovimiento(String nombreTipo, boolean pedirMotivo) {
        int row = view.getTblInventario().getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow2 = view.getTblInventario().convertRowIndexToModel(row);
        final Integer idProducto = (Integer) view.getTblInventario().getModel().getValueAt(modelRow2, 0);
        Number val = (Number) view.getSpnCantidad().getValue();
        BigDecimal cantidad = new BigDecimal(val.toString());
        Integer idTallaStock = null;
        ProductoDto prod = productos.stream()
                .filter(p -> p.idProducto().equals(idProducto))
                .findFirst().orElse(null);
        if (prod != null) {
            if ("Vestimenta".equalsIgnoreCase(prod.tipoProductoNombre())) {
                int idx = view.getCboTalla().getSelectedIndex();
                if (idx >= 0 && idx < tallas.size()) {
                    idTallaStock = tallas.get(idx).idTallaStock();
                }
            } else if ("Fraccionable".equalsIgnoreCase(prod.tipoProductoNombre())) {
                int idx = view.getCboPresentacion().getSelectedIndex();
                if (idx >= 0 && idx < presentaciones.size()) {
                    PresentacionDto pr = presentaciones.get(idx);
                    int veces = ((Number) view.getSpnCantidad().getValue()).intValue();
                    cantidad = pr.cantidad().multiply(new BigDecimal(veces));
                }
            }
        }
        if (pedirMotivo && prod != null) {
            BigDecimal disponible = prod.stockActual();
            if ("Vestimenta".equalsIgnoreCase(prod.tipoProductoNombre())) {
                int idx = view.getCboTalla().getSelectedIndex();
                if (idx >= 0 && idx < tallas.size()) {
                    disponible = tallas.get(idx).stock();
                }
            }
            if (disponible != null && cantidad.compareTo(disponible) > 0) {
                JOptionPane.showMessageDialog(view,
                        "Stock insuficiente",
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String motivo = null;
        if (pedirMotivo) {
            Window owner = SwingUtilities.getWindowAncestor(view);
            DlgObservacion dlg = new DlgObservacion(owner);
            dlg.setVisible(true);
            motivo = dlg.getController().getObservacion();
            if (motivo == null) {
                return; // Operación cancelada por el usuario
            }
        }
        // Realizar movimiento de inventario de forma asíncrona
        final String finalMotivo = motivo;
        final Integer finalIdTallaStock = idTallaStock;
        final BigDecimal finalCantidad = cantidad;
        AsyncTasks.busy(view, () -> {
            TipoMovimientoDto tm = UiContext.tipoMovimientoSvc().buscarPorNombre(nombreTipo);
            Integer empId = UiContext.getUsuarioActual() == null ? null :
                    UiContext.getUsuarioActual().idPersona();
            if (empId == null) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(view,
                        "Debe iniciar sesión para registrar movimientos",
                        "Inicio de sesión requerido", JOptionPane.WARNING_MESSAGE));
                return null;
            }
            MovimientoInventarioCreateDto dto = new MovimientoInventarioCreateDto(
                    idProducto, finalIdTallaStock, tm.idTipoMovimiento(), finalCantidad, finalMotivo, empId);
            UiContext.movimientoInventarioSvc().registrar(dto);
            return null;
        }, v -> {
            resetView();
            cargarProductos();
        });
    }

    /** Devuelve {@code true} si el usuario actual tiene rol de administrador. */
    public boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre())
                        == RolNombre.ADMINISTRADOR;
    }
}

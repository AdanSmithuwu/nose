package com.comercialvalerio.presentation.controller.historial;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import com.comercialvalerio.application.dto.CategoriaDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.dto.MovimientoInventarioDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.TipoMovimientoDto;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.historial.FormHistorialInventario;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para {@link FormHistorialInventario}.
 * Maneja los combos de filtros y la actualización de la tabla.
 */
public class HistorialInventarioController {

    private final FormHistorialInventario view;
    private static final Logger LOG =
            Logger.getLogger(HistorialInventarioController.class.getName());
    private List<TipoMovimientoDto> movimientos = List.of();
    private List<CategoriaDto> categorias = List.of();
    private List<ProductoDto>  productos  = List.of();
    private List<EmpleadoDto>  empleados  = List.of();
    private Map<Integer, EmpleadoDto> empleadoMap = Map.of();
    private Map<Integer, ProductoDto> productoMap = Map.of();

    public HistorialInventarioController(FormHistorialInventario view) {
        this.view = view;
    }

    /** Recarga todos los combos y los datos de la tabla. */
    public void reload() {
        cargarMovimientos();
        cargarCategorias();
        cargarEmpleados();
        cargarFechas();
        cargarProductos();
    }

    /** Carga los tipos de movimiento en el combo. */
    public void cargarMovimientos() {
        // Cargar los tipos de movimiento de forma asíncrona
        AsyncTasks.busy(view, () -> {
            movimientos = UiContext.tipoMovimientoSvc().listar();
            return movimientos;
        }, list -> {
            JComboBox<String> cb = view.getCboMovimiento();
            cb.removeAllItems();
            cb.addItem("Todos");
            for (TipoMovimientoDto t : list) {
                cb.addItem(t.nombre());
            }
        });
    }

    /** Carga las categorías en el combo. */
    public void cargarCategorias() {
        // Obtener categorías de forma asíncrona
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
        // Cargar productos en segundo plano
        AsyncTasks.busy(view, () -> {
            productos = UiContext.productoSvc().listar(null, null, null, null, null);
            productoMap = productos.stream()
                    .collect(Collectors.toMap(ProductoDto::idProducto, p -> p));
            List<ProductoDto> lista = productos;
            if (finalCat != null) {
                lista = productos.stream()
                        .filter(p -> finalCat.equals(p.categoriaId()))
                        .toList();
            }
            return lista;
        }, lista -> {
            JComboBox<String> cb = view.getCboProducto();
            cb.removeAllItems();
            cb.addItem("Todos");
            for (ProductoDto p : lista) {
                cb.addItem(p.nombre());
            }
            refreshTable();
        });
    }

    /** Carga empleados en el combo. */
    public void cargarEmpleados() {
        // Cargar empleados de forma asíncrona
        AsyncTasks.busy(view, () -> {
            empleados = UiContext.empleadoSvc().listar();
            empleadoMap = empleados.stream()
                    .collect(Collectors.toMap(EmpleadoDto::idPersona, e -> e));
            return empleados;
        }, list -> {
            JComboBox<String> cb = view.getCboEmpleado();
            cb.removeAllItems();
            cb.addItem("Todos");
            for (EmpleadoDto e : list) {
                cb.addItem(formatEmpleadoLabel(e));
            }
        });
    }

    /** Llena el combo de rango de fechas con opciones estáticas. */
    public void cargarFechas() {
        JComboBox<String> cb = view.getCboFecha();
        cb.removeAllItems();
        cb.addItem("Hoy");
        cb.addItem("Esta semana");
        cb.addItem("Este mes");
        cb.addItem("Este año");
        cb.addItem("Rango personalizado");
        cb.addItem("Todo");
    }

    /** Recarga la tabla aplicando los filtros seleccionados. */
    public void refreshTable() {
        LocalDateTime[] range = getRangoFechas();
        if (range == null) {
            return;
        }
        // Actualizar datos de la tabla en segundo plano
        AsyncTasks.busy(view, () -> {
            Integer prodId = getSelectedProductoId();
            List<MovimientoInventarioDto> lista;
            if (prodId != null) {
                lista = UiContext.movimientoInventarioSvc().listarPorProducto(prodId);
            } else {
                lista = UiContext.movimientoInventarioSvc()
                        .listarPorRango(range[0], range[1]);
            }
            String[] cols = {"Movimiento","Producto","Talla","Cantidad","Motivo","Fecha","Empleado"};
            DefaultTableModel m = new DefaultTableModel(cols,0);
            Integer movId = getSelectedMovimientoId();
            Integer catId = getSelectedCategoriaId();
            Integer empId = getSelectedEmpleadoId();
                for (MovimientoInventarioDto mv : lista) {
                    if (movId != null && !movId.equals(mv.tipoMovId())) continue;
                    if (empId != null && !empId.equals(mv.empleadoId())) continue;
                    if (catId != null) {
                        ProductoDto p = productoMap.get(mv.productoId());
                        if (p == null || !catId.equals(p.categoriaId())) continue;
                    }
                    if (prodId == null) {
                        // filtro de rango ya aplicado en el servicio
                    } else {
                        var fh = mv.fechaHora();
                        if (fh == null) {
                            continue;
                        }
                        java.time.LocalDateTime fhLocal = fh.toLocalDateTime();
                        if (fhLocal.isBefore(range[0]) || fhLocal.isAfter(range[1]))
                            continue;
                    }
                    String empNombre = mv.empleadoUsuario();
                    EmpleadoDto ed = empleadoMap.get(mv.empleadoId());
                    if (ed != null) {
                        empNombre = formatEmpleadoLabel(ed);
                    }
                    Object fh = mv.fechaHora();
                    String fechaStr = DateFormatUtils.formatServer(fh);
                    m.addRow(new Object[]{
                            mv.tipoMovNombre(),
                            mv.productoNombre(),
                            mv.talla()==null?"":mv.talla(),
                            mv.cantidad(),
                            mv.motivo(),
                            fechaStr,
                            empNombre
                    });
                }
            return m;
        }, m -> {
            view.getTblHistorial().setModel(m);
            TableUtils.packColumns(view.getTblHistorial());
            TableUtils.updateEmptyView(
                    view.getSpHistorial(),
                    view.getTblHistorial(),
                    view.getLblEmpty());
        });
    }

    private Integer getSelectedMovimientoId() {
        int idx = view.getCboMovimiento().getSelectedIndex();
        if (idx > 0 && idx - 1 < movimientos.size()) {
            return movimientos.get(idx - 1).idTipoMovimiento();
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

    private Integer getSelectedProductoId() {
        int idx = view.getCboProducto().getSelectedIndex();
        List<ProductoDto> lista;
        Integer catId = getSelectedCategoriaId();
        if (catId == null) {
            lista = productos;
        } else {
            lista = productos.stream()
                    .filter(p -> catId.equals(p.categoriaId()))
                    .toList();
        }
        if (idx > 0 && idx - 1 < lista.size()) {
            return lista.get(idx - 1).idProducto();
        }
        return null;
    }

    private Integer getSelectedEmpleadoId() {
        int idx = view.getCboEmpleado().getSelectedIndex();
        if (idx > 0 && idx - 1 < empleados.size()) {
            return empleados.get(idx - 1).idPersona();
        }
        return null;
    }

    private LocalDateTime[] getRangoFechas() {
        int idx = view.getCboFecha().getSelectedIndex();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = now;
        switch (idx) {
            case 0 -> start = now.toLocalDate().atStartOfDay();
            case 1 -> start = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            case 2 -> start = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
            case 3 -> start = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();
            case 4 -> {
                var d1 = view.getSpDesde().getDate();
                var d2 = view.getSpHasta().getDate();
                if (d1 != null && d2 != null && d2.isBefore(d1)) {
                    JOptionPane.showMessageDialog(view,
                            "Fecha fin anterior a fecha inicio",
                            "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
                    view.getSpHasta().setDate(null);
                    return null;
                }
                if (d1 == null) d1 = LocalDate.now();
                if (d2 == null) d2 = LocalDate.now();
                start = d1.atStartOfDay();
                end = d2.plusDays(1).atStartOfDay();
            }
            default -> {
                start = LocalDate.of(2000,1,1).atStartOfDay();
                end = now.plusDays(1);
            }
        }
        return new LocalDateTime[]{start, end};
    }

    private String formatEmpleadoLabel(EmpleadoDto e) {
        return (e.nombres() + " " + e.apellidos()).trim();
    }
}

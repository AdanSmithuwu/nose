package com.comercialvalerio.presentation.controller.bitacora;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.comercialvalerio.application.dto.BitacoraLoginDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.bitacora.FormBitacoraLogin;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;

/** Controlador para {@link FormBitacoraLogin}. */
public class BitacoraLoginController {

    private final FormBitacoraLogin view;
    private static final Logger LOG =
            Logger.getLogger(BitacoraLoginController.class.getName());
    private List<EmpleadoDto> empleados = List.of();
    private Map<Integer, EmpleadoDto> empleadoMap = Map.of();

    public BitacoraLoginController(FormBitacoraLogin view) {
        this.view = view;
    }

    /** Carga empleados en el combo. */
    public void cargarEmpleados() {
        AsyncTasks.busy(view, () -> {
            empleados = UiContext.empleadoSvc().listar();
            empleadoMap = empleados.stream()
                    .collect(Collectors.toMap(EmpleadoDto::idPersona, e -> e));
            return empleados;
        }, list -> {
            JComboBox<EmpleadoDto> cb = view.getCboEmpleado();
            cb.removeAllItems();
            cb.addItem(null); // marcador de posición para "Todos"
            for (EmpleadoDto e : list) {
                cb.addItem(e);
            }
        });
    }

    /** Recarga la tabla de eventos aplicando filtros. */
    public void refreshTabla() {
        var d1 = view.getSpDesde().getDate();
        var d2 = view.getSpHasta().getDate();
        if (d1 != null && d2 != null && d2.isBefore(d1)) {
            JOptionPane.showMessageDialog(view,
                    "Fecha fin anterior a fecha inicio",
                    "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
            view.getSpHasta().setDate(null);
            return;
        }
        AsyncTasks.busy(view, () -> {
            LocalDateTime desde = getDesde();
            LocalDateTime hasta = getHasta().plusDays(1);
            Boolean res = switch (view.getCboResultado().getSelectedIndex()) {
                case 1 -> Boolean.TRUE;
                case 2 -> Boolean.FALSE;
                default -> null;
            };
            List<BitacoraLoginDto> lista = UiContext.bitacoraLoginSvc()
                    .listarPorRango(desde, hasta, res);
            Integer empId = getSelectedEmpleadoId();
            if (empId != null) {
                lista = lista.stream()
                        .filter(b -> empId.equals(b.empleadoId()))
                        .toList();
            }
            DefaultTableModel m = new DefaultTableModel(
                    new String[]{"Fecha","Empleado","Resultado"},0);
            for (BitacoraLoginDto b : lista) {
                String empNombre = b.empleadoUsuario();
                EmpleadoDto ed = empleadoMap.get(b.empleadoId());
                if (ed != null) {
                    empNombre = (ed.nombres()+" "+ed.apellidos()).trim();
                }
                OffsetDateTime odt = b.fechaEvento();
                String fecha = formatFecha(odt);
                m.addRow(new Object[]{
                        fecha,
                        empNombre,
                        b.exitoso()?"Éxito":"Fallo"
                });
            }
            return m;
        }, model -> {
            JTable tabla = view.getTblEventos();
            tabla.setModel(model);
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            sorter.setSortable(0, false);
            tabla.setRowSorter(sorter);
            TableUtils.packColumns(tabla);
            TableUtils.updateEmptyView(view.getSpEventos(), tabla, view.getLblEmpty());
        });
    }


    /** Obtiene la fecha inicial del filtro. */
    private LocalDateTime getDesde() {
        java.time.LocalDate d = view.getSpDesde().getDate();
        if (d == null) {
            d = java.time.LocalDate.of(2000, 1, 1);
        }
        return d.atStartOfDay();
    }

    /** Obtiene la fecha final del filtro. */
    private LocalDateTime getHasta() {
        java.time.LocalDate d = view.getSpHasta().getDate();
        if (d == null) {
            d = java.time.LocalDate.now();
        }
        return d.atStartOfDay();
    }

    private Integer getSelectedEmpleadoId() {
        int idx = view.getCboEmpleado().getSelectedIndex();
        if (idx > 0 && idx - 1 < empleados.size()) {
            return empleados.get(idx - 1).idPersona();
        }
        return null;
    }

    static String formatFecha(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }
        OffsetDateTime odt = DateFormatUtils.parseOffsetDateTime(value);
        return DateFormatUtils.formatServer(odt);
    }
}

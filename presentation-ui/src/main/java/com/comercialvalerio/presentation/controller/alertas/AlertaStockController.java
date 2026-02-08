package com.comercialvalerio.presentation.controller.alertas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.comercialvalerio.application.dto.AlertaStockDto;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.ErrorHandler;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.alertas.DlgAlertasStock;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.util.NumberUtils;
import com.comercialvalerio.presentation.ui.base.TableUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Controlador de {@link DlgAlertasStock}. */
public class AlertaStockController {

    private final DlgAlertasStock view;
    private static final Logger LOG =
            Logger.getLogger(AlertaStockController.class.getName());

    public AlertaStockController(DlgAlertasStock view) {
        this.view = view;
    }

    /** Carga las alertas pendientes desde el servicio REST en la tabla. */
    public void loadAlertas() {
        AsyncTasks.busy(view,
                () -> UiContext.alertaStockSvc().listarPendientes(),
                lista -> {
                    DefaultTableModel m = view.getModel();
                    TableUtils.clearModel(m);
                    for (AlertaStockDto a : lista) {
                        Object f = a.fechaAlerta();
                        String fecha = DateFormatUtils.formatServer(f);
                        String stock = NumberUtils.formatPlain(a.stockActual());
                        String umbral = NumberUtils.formatPlain(a.umbral());
                        m.addRow(new Object[]{
                                a.idAlerta(),
                                a.productoId(),
                                a.productoNombre(),
                                stock,
                                umbral,
                                fecha
                        });
                    }
                    view.packColumns();
                    if (view.getChkSeleccionarTodo().isSelected()) {
                        view.getTable().selectAll();
                    }
                    filtrar();
                });
    }

    /** Aplica el filtro de búsqueda actual a la tabla. */
    public void filtrar() {
        String text = view.getSearchField().getText().trim();
        RowFilter<DefaultTableModel, Object> rf = null;
        if (!text.isEmpty()) {
            rf = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 2);
        }
        TableRowSorter<DefaultTableModel> sorter =
                (TableRowSorter<DefaultTableModel>) view.getTable().getRowSorter();
        sorter.setRowFilter(rf);
        if (view.getTable().getRowCount() == 0) {
            view.showEmptyView();
        } else {
            view.showTable();
        }
    }

    /** Marca las alertas seleccionadas como procesadas. */
    public void procesar() {
        int[] rows;
        if (view.getChkSeleccionarTodo().isSelected()) {
            rows = new int[view.getTable().getRowCount()];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = i;
            }
        } else {
            rows = view.getTable().getSelectedRows();
        }
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(view,
                    "Seleccione una o más alertas o marque \"Seleccionar todo\" para procesar",
                    "Procesar alertas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!com.comercialvalerio.presentation.ui.util.DialogUtils.confirmAction(view,
                "¿Marcar las alertas seleccionadas como procesadas?"))
            return;
        int[] viewRows = rows;
        int[] modelRows = new int[viewRows.length];
        Integer[] prodIds = new Integer[viewRows.length];
        for (int i = 0; i < viewRows.length; i++) {
            int modelRow = view.getTable().convertRowIndexToModel(viewRows[i]);
            modelRows[i] = modelRow;
            prodIds[i] = (Integer) view.getModel().getValueAt(modelRow, 1);
        }

        AsyncTasks.busy(view, () -> {
            List<Integer> toRemove = new ArrayList<>();
            for (int i = prodIds.length - 1; i >= 0; i--) {
                try {
                    UiContext.alertaStockSvc().procesarProducto(prodIds[i]);
                    toRemove.add(modelRows[i]);
                } catch (jakarta.ws.rs.WebApplicationException wex) {
                    ErrorHandler.handle(wex);
                } catch (RuntimeException ex) {
                    ErrorHandler.handle(ex);
                }
            }
            return toRemove;
        }, removed -> {
            Collections.sort(removed, Collections.reverseOrder());
            for (int idx : removed) {
                view.getModel().removeRow(idx);
            }
            view.getTable().requestFocusInWindow();
            filtrar();
            if (view.getModel().getRowCount() == 0) {
                view.getChkSeleccionarTodo().setSelected(false);
                view.getSearchField().getTextField().requestFocusInWindow();
            }
        });
    }
}

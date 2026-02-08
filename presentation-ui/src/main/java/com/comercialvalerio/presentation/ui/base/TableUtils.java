package com.comercialvalerio.presentation.ui.base;

import javax.swing.table.DefaultTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.Comparator;

/** Métodos de utilidad para trabajar con tablas. */
public final class TableUtils {

    private TableUtils() {}

    /**
     * Muestra una etiqueta cuando el modelo de la tabla no tiene filas.
     *
     * @param sp    panel con scroll que contiene la tabla
     * @param table tabla cuyo modelo se inspecciona
     * @param label etiqueta a mostrar cuando el modelo está vacío
     */
    public static void updateEmptyView(JScrollPane sp, JTable table, JLabel label) {
        TableModel m = table.getModel();
        if (m.getRowCount() == 0) {
            sp.setViewportView(label);
        } else {
            sp.setViewportView(table);
        }
    }

    /** Sobrecarga conveniente que usa un texto personalizado. */
    public static JLabel updateEmptyView(JScrollPane sp, JTable table, String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        updateEmptyView(sp, table, lbl);
        return lbl;
    }

    /** Mensaje por defecto "Sin datos" cuando no se pasa una etiqueta. */
    public static JLabel updateEmptyView(JScrollPane sp, JTable table) {
        return updateEmptyView(sp, table, "Sin datos");
    }

    /**
     * Configura comparadores para que las columnas numéricas se ordenen por valor
     * y no de forma lexicográfica.
     *
     * @param sorter ordenador de filas cuyas columnas se configurarán
     * @param cols   índices de columnas que contienen valores numéricos
     */
    public static void setNumericComparators(TableRowSorter<? extends TableModel> sorter,
                                             int... cols) {
        Comparator<Object> cmp = Comparator.nullsLast((a, b) -> {
            try {
                BigDecimal n1 = new BigDecimal(a.toString());
                BigDecimal n2 = new BigDecimal(b.toString());
                return n1.compareTo(n2);
            } catch (NumberFormatException ex) {
                return a.toString().compareTo(b.toString());
            }
        });
        for (int c : cols) {
            sorter.setComparator(c, cmp);
        }
    }

    /**
     * Elimina todas las filas del modelo indicado.
     *
     * @param model modelo a limpiar
     */
    public static void clearModel(DefaultTableModel model) {
        model.setRowCount(0);
    }

    /** Número de filas usado por defecto al ajustar anchos. */
    public static final int DEFAULT_MAX_ROWS = 100;

    /**
     * Calcula el ancho preferido de cada columna según el contenido actual.
     * Ayuda a evitar recortes cuando el ancho por defecto es muy pequeño.
     * Revisa como máximo {@code maxRows} filas para no realizar operaciones costosas.
     *
     * @param table   tabla cuyas columnas serán redimensionadas
     * @param maxRows máximo de filas a inspeccionar o un número no positivo para todas
     *                las filas
     */
    public static void packColumns(JTable table, int maxRows) {
        var header = table.getTableHeader();
        var defaultHeaderRenderer = header.getDefaultRenderer();
        var model = table.getColumnModel();
        int rows = table.getRowCount();
        if (maxRows > 0 && maxRows < rows) {
            rows = maxRows;
        }
        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = model.getColumn(col);
            int max = 0;

            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = defaultHeaderRenderer;
            }
            Component cmp = headerRenderer.getTableCellRendererComponent(
                    table, column.getHeaderValue(), false, false, -1, col);
            max = cmp.getPreferredSize().width;

            for (int row = 0; row < rows; row++) {
                TableCellRenderer r = table.getCellRenderer(row, col);
                cmp = r.getTableCellRendererComponent(
                        table, table.getValueAt(row, col), false, false, row, col);
                max = Math.max(max, cmp.getPreferredSize().width);
            }
            column.setPreferredWidth(max + 10);
        }
    }

    /** Sobrecarga que utiliza {@link #DEFAULT_MAX_ROWS}. */
    public static void packColumns(JTable table) {
        packColumns(table, DEFAULT_MAX_ROWS);
    }

    /**
     * Detiene la edición actual de la tabla si existe.
     *
     * @param table tabla a liberar
     */
    public static void stopEditing(JTable table) {
        if (table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
    }
}

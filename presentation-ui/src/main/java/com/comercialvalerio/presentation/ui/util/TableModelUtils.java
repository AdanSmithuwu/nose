package com.comercialvalerio.presentation.ui.util;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Métodos utilitarios para preparar instancias de {@link DefaultTableModel}. */
public final class TableModelUtils {
    private TableModelUtils() {}

    /**
     * Crea un modelo, lo asigna a la tabla, aplica comparadores numéricos usando
     * {@link com.comercialvalerio.presentation.ui.base.TableUtils#setNumericComparators}
     * y oculta las columnas indicadas.
     *
     * @param table       tabla a configurar
     * @param columns     encabezados para el nuevo modelo
     * @param numericCols índices de columnas numéricas
     * @param hiddenCols  índices de modelo a remover de la vista
     * @return el modelo creado para manipulación posterior
     */
    public static DefaultTableModel createModel(JTable table, String[] columns,
            int[] numericCols, int... hiddenCols) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        table.setModel(model);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        for (int col : hiddenCols) {
            sorter.setSortable(col, false);
        }
        com.comercialvalerio.presentation.ui.base.TableUtils
                .setNumericComparators(sorter, numericCols);
        table.setRowSorter(sorter);

        for (int i = hiddenCols.length - 1; i >= 0; i--) {
            int col = hiddenCols[i];
            if (col >= 0 && col < table.getColumnCount()) {
                table.getColumnModel().removeColumn(
                        table.getColumnModel().getColumn(col));
            }
        }
        return model;
    }
}

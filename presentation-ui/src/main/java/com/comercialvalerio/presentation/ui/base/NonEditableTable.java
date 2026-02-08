package com.comercialvalerio.presentation.ui.base;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/** JTable que no permite editar celdas. */
public class NonEditableTable extends JTable {
    public NonEditableTable() {
        super();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public NonEditableTable(TableModel dm) {
        super(dm);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSorter(new TableRowSorter<>(dm));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        setRowSorter(new TableRowSorter<>(dataModel));
    }
}

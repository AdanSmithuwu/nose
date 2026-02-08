package com.comercialvalerio.presentation.ui.historial;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import net.miginfocom.swing.MigLayout;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.base.DatePickerField;

import com.comercialvalerio.presentation.controller.historial.HistorialClienteController;
import com.comercialvalerio.presentation.ui.util.KeyUtils;


import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import javax.swing.table.DefaultTableModel;

import java.awt.*;

public class FormHistorialCliente extends BaseForm implements Refreshable {
    private final JComboBox<String> cboCategoria = new JComboBox<>();
    private final JComboBox<String> cboProducto  = new JComboBox<>();
    private final DatePickerField spDesde = new DatePickerField();
    private final DatePickerField spHasta = new DatePickerField();
    private final JLabel lblCategoria = new JLabel("Categoría:");
    private final JLabel lblProducto  = new JLabel("Producto:");
    private final JTable table = new NonEditableTable();
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Fecha", "Total", "Descuento", "Cargo", "Estado", "Tipo"}, 0);
    private final JButton btnRefresh;
    private final HistorialClienteController controller;

    public FormHistorialCliente() {
        controller = new HistorialClienteController(this);
        btnRefresh = UIUtils.createRefreshButton(controller::refresh);
        initUI();
        controller.cargarCategorias();
        cboCategoria.addActionListener(e -> controller.cargarProductos());
        cboProducto.addActionListener(e -> controller.refresh());
        spDesde.addDateSelectionListener(e -> controller.refresh());
        spHasta.addDateSelectionListener(e -> controller.refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    controller.mostrarDetalle();
                }
            }
        });
        // Acciones definidas en el diálogo contenedor.
    }

    private void initUI() {
        setLayout(new MigLayout("fill,insets 16 20 10 20, gap " + UIStyle.FORM_GAP,
                "[grow]", "[]16[]8[grow]push"));
        putClientProperty(FlatClientProperties.STYLE, "background:@background");
        setBorder(new javax.swing.border.EmptyBorder(10,20,20,20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(btnRefresh, BorderLayout.EAST);
        add(top, "cell 0 0, growx, wrap");

        JPanel filtros = new JPanel(new MigLayout("insets 0, gap " + UIStyle.FILTER_GAP));
        filtros.setOpaque(false);
        spDesde.setDateFormat(com.comercialvalerio.presentation.ui.util.DateFormatUtils.getShortPattern());
        spHasta.setDateFormat(com.comercialvalerio.presentation.ui.util.DateFormatUtils.getShortPattern());
        cboCategoria.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        cboProducto.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
        lblCategoria.setLabelFor(cboCategoria);
        lblProducto.setLabelFor(cboProducto);
        filtros.add(new JLabel("Desde:"));
        filtros.add(spDesde, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(spHasta, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(lblCategoria);
        filtros.add(cboCategoria, "w 200!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblProducto);
        filtros.add(cboProducto,  "w 200!, h " + UIStyle.COMBO_HEIGHT + "!");
        add(filtros, "cell 0 1, growx, wrap");

        table.setModel(model);
        table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        add(scroll, "cell 0 2, grow, wrap");

        // No se agregan botones aquí; el diálogo contenedor maneja las acciones.
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public JTable getTable() { return table; }

    public JScrollPane getScroll() { return scroll; }
    public JLabel getLblEmpty() { return lblEmpty; }
    public JComboBox<String> getCboCategoria() { return cboCategoria; }
    public JComboBox<String> getCboProducto()  { return cboProducto; }
    public DatePickerField getSpDesde() { return spDesde; }
    public DatePickerField getSpHasta() { return spHasta; }

    public JButton getBtnRefresh() { return btnRefresh; }

    public HistorialClienteController getController() { return controller; }

    @Override
    public void refresh() {
        controller.refresh();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refresh);
        KeyUtils.registerFocusAction(this, spDesde);
    }
}

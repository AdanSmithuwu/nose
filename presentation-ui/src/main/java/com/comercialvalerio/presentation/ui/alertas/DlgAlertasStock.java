package com.comercialvalerio.presentation.ui.alertas;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.comercialvalerio.presentation.controller.alertas.AlertaStockController;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.base.TableUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.util.SearchField;
import com.comercialvalerio.presentation.ui.util.DocumentListeners;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import javax.swing.table.DefaultTableModel;
import com.comercialvalerio.presentation.ui.util.TableModelUtils;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.KeyEvent;
import net.miginfocom.swing.MigLayout;

/** Diálogo que muestra las alertas de stock pendientes. */
public class DlgAlertasStock extends BaseDialog {
    private final JTable table = new NonEditableTable();
    private DefaultTableModel model;
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final JButton btnProcesar;
    private final JCheckBox chkSeleccionarTodo = new JCheckBox("Seleccionar todo");
    private final SearchField searchField = new SearchField("Buscar producto");
    private final JButton btnRefresh = UIUtils.createRefreshButton(this::loadAlertas);
    private final AlertaStockController controller;

    public DlgAlertasStock(JFrame owner) {
        super(owner, "Alertas de stock pendientes", true, new JButton("Marcar procesadas"));
        this.btnProcesar = getDefaultButton();
        this.controller = new AlertaStockController(this);
        buildUI();
        KeyUtils.registerFocusAction(getRootPane(), searchField.getTextField());
        KeyUtils.registerRefreshAction(getRootPane(), this::loadAlertas);
        SwingUtilities.invokeLater(() -> table.requestFocusInWindow());
        btnProcesar.addActionListener(e -> procesar());
        KeyUtils.setTooltipAndMnemonic(btnProcesar, KeyEvent.VK_M, "Marcar procesadas");
        DocumentListeners.attachDebounced(searchField.getTextField(), this::filtrar);
        searchField.addActionListener(e -> filtrar());
        searchField.addClearActionListener(e -> {
            searchField.getTextField().setText("");
            filtrar();
        });
        chkSeleccionarTodo.addActionListener(e -> {
            if (chkSeleccionarTodo.isSelected()) {
                table.selectAll();
            } else {
                table.clearSelection();
            }
        });
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.putClientProperty(
                FlatClientProperties.STYLE,
                "background:@background" + "; arc:" + UIStyle.ARC_DEFAULT);
        panel.setBackground(UIStyle.getColorCardBg());
        panel.setBorder(new EmptyBorder(20,20,20,20));

        String[] cols = {"ID", "ProdId", "Producto", "Stock", "Umbral", "Fecha"};
        model = TableModelUtils.createModel(table, cols, new int[]{3,4}, 0,1);
        table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        HeaderPanel header = new HeaderPanel("Alertas de stock pendientes", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        panel.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new MigLayout(
                "insets 0, gap 10, fill",
                "[grow]",
                "[]10[grow]"));
        body.setOpaque(false);
        body.add(searchField, "growx, h 40!, wrap");
        body.add(scroll, "grow");
        panel.add(body, BorderLayout.CENTER);

        KeyUtils.setTooltipAndMnemonic(chkSeleccionarTodo, KeyEvent.VK_T,
                "Seleccionar todo");

        JPanel controls = new JPanel(new BorderLayout());
        controls.add(chkSeleccionarTodo, BorderLayout.WEST);
        controls.add(btnProcesar, BorderLayout.EAST);
        panel.add(controls, BorderLayout.SOUTH);
        setContentPane(panel);
    }

    /** Delegado al controlador para cargar alertas. */
    public void loadAlertas() {
        controller.loadAlertas();
    }

    /** Delegado al controlador para aplicar el filtrado. */
    private void filtrar() {
        controller.filtrar();
    }

    /** Delegado al controlador para procesar las alertas seleccionadas. */
    private void procesar() {
        controller.procesar();
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }
    public JScrollPane getScroll() { return scroll; }
    public JCheckBox getChkSeleccionarTodo() { return chkSeleccionarTodo; }
    public SearchField getSearchField() { return searchField; }

    public void packColumns() {
        TableUtils.packColumns(table);
        searchField.setPreferredSize(new Dimension(
                table.getPreferredSize().width,
                SearchField.DEFAULT_HEIGHT));
        scroll.revalidate();
    }

    public void showEmptyView() {
        scroll.setViewportView(lblEmpty);
    }

    public void showTable() {
        scroll.setViewportView(table);
    }

}

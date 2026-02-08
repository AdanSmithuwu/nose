package com.comercialvalerio.presentation.ui.historial;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.comercialvalerio.presentation.controller.historial.HistorialInventarioController;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import java.awt.*;
import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import javax.swing.border.*;
import net.miginfocom.swing.MigLayout;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;

/**
 * Historial de Inventario
 * ─ Filtros:   Movimiento · Categoría · Producto · Fecha · Empleado
 * ─ Tabla     con columnas Movimiento, Producto, Talla, Cantidad, Motivo, Fecha/Hora, Empleado
 * No incluye botones inferiores (no aparecen en el mock-up)
 */
public class FormHistorialInventario extends BaseForm implements Refreshable {

    /* ====== filtros ====== */
    private final JComboBox<String> cboMovimiento = new JComboBox<>();
    private final JComboBox<String> cboCategoria  = new JComboBox<>();
    private final JComboBox<String> cboProducto   = new JComboBox<>();
    private final JComboBox<String> cboFecha      = new JComboBox<>();
    private final JComboBox<String> cboEmpleado   = new JComboBox<>();
    private final JLabel lblMovimiento = new JLabel("Movimiento:");
    private final JLabel lblCategoria  = new JLabel("Categoría:");
    private final JLabel lblProducto   = new JLabel("Producto:");
    private final JLabel lblFecha      = new JLabel("Fecha:");
    private final JLabel lblEmpleado   = new JLabel("Empleado:");
    private final DatePickerField spDesde = new DatePickerField();
    private final DatePickerField spHasta = new DatePickerField();
    private final JPanel   pnlRango = new JPanel(new MigLayout("insets 0, gap " + UIStyle.FILTER_GAP));

    /* ====== tabla ====== */
    private final JTable tblHistorial = new NonEditableTable();
    private final JScrollPane spHistorial = new JScrollPane(tblHistorial);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final HistorialInventarioController controller;

    public FormHistorialInventario() {
        controller = new HistorialInventarioController(this);
        buildUI();
        controller.reload();
        cboMovimiento.addActionListener(e -> controller.refreshTable());
        cboCategoria.addActionListener(e -> controller.cargarProductos());
        cboProducto.addActionListener(e -> controller.refreshTable());
        cboFecha.addActionListener(e -> {
            updateCustomRangeVisibility();
            controller.refreshTable();
        });
        cboEmpleado.addActionListener(e -> controller.refreshTable());
        spDesde.addDateSelectionListener(e -> controller.refreshTable());
        spHasta.addDateSelectionListener(e -> controller.refreshTable());
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]10[grow]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE,"background:@background");

        /* ---------------- header ---------------- */
        HeaderPanel header = new HeaderPanel("Historial de Inventario");
        header.setBorder(new EmptyBorder(0,0,5,0));
        JButton btnRefresh = UIUtils.createRefreshButton(controller::reload);

        header.add(btnRefresh, BorderLayout.EAST);

        add(header, "cell 0 0, growx, wrap");

        /* ---------------- body ------------------ */
        JPanel body = new JPanel(new MigLayout(
                "insets 0, gap " + UIStyle.FILTER_GAP + ", fill",
                "[grow,fill]",
                "[]10[]10[grow]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        /* filtros en una línea */
        JPanel filtros = new JPanel(new MigLayout("insets 0, gap " + UIStyle.FILTER_GAP, "[][grow][grow][grow][grow]"));
        filtros.setOpaque(false);
        styleCombo(cboMovimiento, "Movimiento");
        styleCombo(cboCategoria , "Categoría");
        styleCombo(cboProducto  , "Producto");
        styleCombo(cboFecha     , "Fecha");
        styleCombo(cboEmpleado  , "Empleado");
        lblMovimiento.setLabelFor(cboMovimiento);
        lblCategoria.setLabelFor(cboCategoria);
        lblProducto.setLabelFor(cboProducto);
        lblFecha.setLabelFor(cboFecha);
        lblEmpleado.setLabelFor(cboEmpleado);
        filtros.add(lblMovimiento, "split 2, aligny center");
        filtros.add(cboMovimiento, "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblCategoria, "split 2, aligny center");
        filtros.add(cboCategoria , "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblProducto, "split 2, aligny center");
        filtros.add(cboProducto  , "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblFecha, "split 2, aligny center");
        filtros.add(cboFecha     , "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblEmpleado, "split 2, aligny center");
        filtros.add(cboEmpleado  , "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        body.add(filtros, "cell 0 0, gapleft 20, gaptop 10, growx, wrap");

        /* panel rango personalizado */
        spDesde.setDateFormat(DateFormatUtils.getShortPattern());
        spHasta.setDateFormat(DateFormatUtils.getShortPattern());
        pnlRango.setOpaque(false);
        pnlRango.add(new JLabel("Desde:"));
        pnlRango.add(spDesde, UIStyle.DATE_FIELD_CONSTRAINTS);
        pnlRango.add(new JLabel("Hasta:"));
        pnlRango.add(spHasta, UIStyle.DATE_FIELD_CONSTRAINTS);
        pnlRango.setVisible(false);
        body.add(pnlRango, "cell 0 1, gapleft 20, growx, wrap, hidemode 3");

        /* tabla */
        tblHistorial.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spHistorial, "cell 0 2, grow");

        /* borde exterior violeta */
        setBorder(UIStyle.FORM_BORDER_VIOLET);

        updateCustomRangeVisibility();
    }

    /* --- helper para combos con placeholder --- */
    private void styleCombo(JComboBox<String> cb, String placeholder){
        cb.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        cb.putClientProperty(FlatClientProperties.STYLE,"arc:" + UIStyle.ARC_DIALOG );
        cb.setPrototypeDisplayValue("XXXXXXXXXXXX");
    }

    private void updateCustomRangeVisibility() {
        boolean visible = "Rango personalizado".equals(cboFecha.getSelectedItem());
        pnlRango.setVisible(visible);
        pnlRango.revalidate();
    }

    @Override
    public void refresh() {
        controller.reload();
    }

    /* ============ getters ================= */
    public JComboBox<String> getCboMovimiento() { return cboMovimiento; }
    public JComboBox<String> getCboCategoria()  { return cboCategoria;  }
    public JComboBox<String> getCboProducto()   { return cboProducto;   }
    public JComboBox<String> getCboFecha()      { return cboFecha;      }
    public JComboBox<String> getCboEmpleado()   { return cboEmpleado;   }
    public JTable            getTblHistorial()  { return tblHistorial;  }
    public JScrollPane       getSpHistorial()   { return spHistorial;   }
    public JLabel            getLblEmpty()      { return lblEmpty;      }
    public DatePickerField   getSpDesde()       { return spDesde;       }
    public DatePickerField   getSpHasta()       { return spHasta;       }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::reload);
        KeyUtils.registerFocusAction(this, spDesde);
    }
}

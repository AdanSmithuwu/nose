package com.comercialvalerio.presentation.ui.bitacora;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.controller.bitacora.BitacoraLoginController;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Bitácora de accesos: permite filtrar eventos por fecha, empleado y resultado.
 */
public class FormBitacoraLogin extends BaseForm implements Refreshable {

    private final JComboBox<com.comercialvalerio.application.dto.EmpleadoDto> cboEmpleado = new JComboBox<>();
    private final JComboBox<String> cboResultado = new JComboBox<>();
    private final JLabel lblEmpleado = new JLabel("Empleado:");
    private final JLabel lblResultado = new JLabel("Resultado:");
    private final DatePickerField spDesde = new DatePickerField();
    private final DatePickerField spHasta = new DatePickerField();

    private final JTable tblEventos = new NonEditableTable();
    private final JScrollPane spEventos = new JScrollPane(tblEventos);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final BitacoraLoginController controller;

    public FormBitacoraLogin() {
        controller = new BitacoraLoginController(this);
        buildUI();
        controller.cargarEmpleados();
        controller.refreshTabla();
        cboEmpleado.addActionListener(e -> controller.refreshTabla());
        cboResultado.addActionListener(e -> controller.refreshTabla());
        spDesde.addDateSelectionListener(e -> controller.refreshTabla());
        spHasta.addDateSelectionListener(e -> controller.refreshTabla());
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]10[grow]"));
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        // ----- header -----
        JButton btnRefresh = UIUtils.createRefreshButton(controller::refreshTabla);
        HeaderPanel header = new HeaderPanel("Bitácora de Accesos", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));

        add(header, "cell 0 0, growx, wrap");

        // ----- body -----
        JPanel body = new JPanel(new MigLayout("insets 0, gap 10, fill", "[grow,fill]", "[]10[grow]push"));
        body.setOpaque(false);
        add(body, "cell 0 1, grow");

        JPanel filtros = new JPanel(new MigLayout("insets 0, gap " + UIStyle.FILTER_GAP));
        filtros.setOpaque(false);
        spDesde.setDateFormat(DateFormatUtils.getShortPattern());
        spHasta.setDateFormat(DateFormatUtils.getShortPattern());
        cboEmpleado.setPrototypeDisplayValue(
                new com.comercialvalerio.application.dto.EmpleadoDto(
                        0, "xxxxxxxxxx", "", "", "", 0, "", "", "", null, ""));
        cboEmpleado.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = (JLabel) new DefaultListCellRenderer()
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                lbl.setText("Todos");
            } else {
                lbl.setText((value.nombres() + " " + value.apellidos()).trim());
            }
            return lbl;
        });
        cboResultado.addItem("Todos");
        cboResultado.addItem("Éxito");
        cboResultado.addItem("Fallo");
        lblEmpleado.setLabelFor(cboEmpleado);
        lblResultado.setLabelFor(cboResultado);
        filtros.add(new JLabel("Desde:"));
        filtros.add(spDesde, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(spHasta, UIStyle.DATE_FIELD_CONSTRAINTS);
        filtros.add(lblEmpleado);
        filtros.add(cboEmpleado, "w 160!, h " + UIStyle.COMBO_HEIGHT + "!");
        filtros.add(lblResultado);
        filtros.add(cboResultado, "w 100!, h " + UIStyle.COMBO_HEIGHT + "!");
        body.add(filtros, "cell 0 0, growx, wrap");

        tblEventos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        body.add(spEventos, "cell 0 1, grow");

        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    // ----- getters -----
    public JComboBox<com.comercialvalerio.application.dto.EmpleadoDto> getCboEmpleado() { return cboEmpleado; }
    public JComboBox<String> getCboResultado() { return cboResultado; }
    public DatePickerField getSpDesde() { return spDesde; }
    public DatePickerField getSpHasta() { return spHasta; }
    public JTable getTblEventos() { return tblEventos; }
    public JScrollPane getSpEventos() { return spEventos; }
    public JLabel getLblEmpty() { return lblEmpty; }

    @Override
    public void refresh() {
        controller.refreshTabla();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerRefreshAction(this, controller::refreshTabla);
        KeyUtils.registerFocusAction(this, spDesde);
    }
}

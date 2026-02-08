package com.comercialvalerio.presentation.ui.reportes;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;

import com.comercialvalerio.presentation.controller.reportes.ReporteRotacionController;
import javax.swing.border.EmptyBorder;

public class FormReporteRotacion extends BaseForm implements Refreshable {
    private final JTable table = new NonEditableTable();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"#","Producto","Categoría","Unidades","Importe"},0);
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final DatePickerField spDesde = new DatePickerField();
    private final DatePickerField spHasta = new DatePickerField();
    private final JSpinner spTop   = new JSpinner(new SpinnerNumberModel(0,0,100,1));
    private final JButton btnGenerar = new JButton("Generar PDF");
    private final JButton btnPrint = new JButton("Imprimir");
    private JButton btnRefresh;
    private final ReporteRotacionController controller = new ReporteRotacionController(this);

    public FormReporteRotacion(){
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]5[grow]"));
        setOpaque(false);
        setBorder(UIStyle.FORM_MARGIN);
        btnRefresh = UIUtils.createRefreshButton(controller::loadData);

        JPanel top = new JPanel();
        top.setOpaque(false);
        spDesde.setDateFormat(DateFormatUtils.getShortPattern());
        spHasta.setDateFormat(DateFormatUtils.getShortPattern());
        spDesde.setPreferredSize(new Dimension(UIStyle.DATE_FIELD_WIDTH,
                UIStyle.DATE_FIELD_HEIGHT));
        spHasta.setPreferredSize(new Dimension(UIStyle.DATE_FIELD_WIDTH,
                UIStyle.DATE_FIELD_HEIGHT));
        top.add(new JLabel("Desde:")); top.add(spDesde);
        top.add(new JLabel("Hasta:")); top.add(spHasta);
        top.add(new JLabel("Top N:")); top.add(spTop);
        ButtonStyles.styleBottom(btnGenerar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/play.svg");
        ButtonStyles.styleBottom(btnPrint, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnGenerar, KeyEvent.VK_G, "Generar PDF y guardar en historial");
        KeyUtils.setTooltipAndMnemonic(btnPrint, KeyEvent.VK_I, "Imprimir");
        btnGenerar.setEnabled(false);
        btnPrint.setEnabled(false);
        top.add(btnGenerar); top.add(btnPrint);

        HeaderPanel header = new HeaderPanel("Reporte Rotación", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        add(body, "cell 0 1, grow");
        table.setModel(model); table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);

        spDesde.addDateSelectionListener(e -> {
            controller.loadData();
            updateButtons();
        });
        spHasta.addDateSelectionListener(e -> {
            controller.loadData();
            updateButtons();
        });
        spTop.addChangeListener(e -> {
            controller.loadData();
            updateButtons();
        });
        btnGenerar.addActionListener(e -> controller.generar());
        btnPrint.addActionListener(e -> controller.printPdf());
        controller.loadData();
    }

    public DatePickerField getSpDesde() { return spDesde; }
    public DatePickerField getSpHasta() { return spHasta; }
    public JSpinner getSpTop() { return spTop; }
    public DefaultTableModel getModel() { return model; }
    public JTable getTable() { return table; }
    public JScrollPane getScroll() { return scroll; }
    public JLabel getLblEmpty() { return lblEmpty; }
    public JButton getBtnGenerar() { return btnGenerar; }
    public JButton getBtnPrint() { return btnPrint; }

    /** Habilita acciones cuando las fechas son válidas. */
    public void updateButtons() {
        boolean fechasOk = spDesde.getDate() != null && spHasta.getDate() != null;
        btnGenerar.setEnabled(fechasOk);
        btnPrint.setEnabled(false);
    }

    @Override
    public void refresh() {
        controller.loadData();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK),
                controller::printPdf);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_DOWN_MASK),
                controller::generar);
        KeyUtils.registerRefreshAction(this, controller::loadData);
        KeyUtils.registerFocusAction(this, spDesde);
    }
}

package com.comercialvalerio.presentation.ui.reportes;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import java.time.LocalDate;

import javax.swing.BoxLayout;
import net.miginfocom.swing.MigLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.comercialvalerio.presentation.controller.reportes.ReporteMensualController;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.UIUtils;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.common.BusyOverlay;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatClientProperties;

public class FormReporteMensual extends BaseForm implements Refreshable {
    private final JTable table = new NonEditableTable();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Día","Transacciones","Monto"},0);
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);

    private final JTable tblCategoria = new NonEditableTable();
    private final DefaultTableModel modelCategoria = new DefaultTableModel(
            new String[]{"Categoría","Transacciones","Monto"},0);
    private final JScrollPane spCategoria = new JScrollPane(tblCategoria);
    private final JLabel lblEmptyCategoria = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);


    private final JLabel lblMinorista = new JLabel();
    private final JLabel lblEspecial = new JLabel();
    private final JLabel lblDomicilio = new JLabel();
    private final JButton btnGenerar = new JButton("Generar");
    private final JButton btnPdf = new JButton("Exportar a PDF");
    private final JButton btnPrint = new JButton("Imprimir");
    private JButton btnRefresh;
    private final JSpinner spAnio = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(),2000,2100,1));
    private final JSpinner spMes  = new JSpinner(new SpinnerNumberModel(LocalDate.now().getMonthValue(),1,12,1));
    private final ReporteMensualController controller;
    private boolean firstLoad = true;

    public FormReporteMensual(){
        controller = new ReporteMensualController(this);
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]5[grow]"));
        setOpaque(false);
        setBorder(UIStyle.FORM_MARGIN);
        btnRefresh = UIUtils.createRefreshButton(controller::loadData);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.add(new JLabel("Año:")); top.add(spAnio);
        top.add(new JLabel("Mes:")); top.add(spMes);
        ButtonStyles.styleBottom(btnGenerar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/play.svg");
        ButtonStyles.styleBottom(btnPdf, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        ButtonStyles.styleBottom(btnPrint, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnGenerar, KeyEvent.VK_G, "Generar y guardar en historial");
        KeyUtils.setTooltipAndMnemonic(btnPdf, KeyEvent.VK_P, "Exportar a PDF");
        KeyUtils.setTooltipAndMnemonic(btnPrint, KeyEvent.VK_I, "Imprimir");
        btnGenerar.setEnabled(false);
        btnPdf.setEnabled(false);
        btnPrint.setEnabled(false);
        top.add(btnGenerar); top.add(btnPdf); top.add(btnPrint);

        HeaderPanel header = new HeaderPanel("Reporte Mensual", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        JPanel body = new JPanel(new BorderLayout(5,5));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);

        table.setModel(model); table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tblCategoria.setModel(modelCategoria); tblCategoria.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);

        JPanel center = new JPanel(new BorderLayout(5,5));
        center.setOpaque(false);

        JPanel tables = new JPanel(new GridLayout(1,2,5,5));
        tables.setOpaque(false);
        tables.add(scroll);
        tables.add(spCategoria);
        center.add(tables, BorderLayout.CENTER);

        JPanel resumen = buildResumenPanel();
        center.add(resumen, BorderLayout.SOUTH);

        body.add(center, BorderLayout.CENTER);
        add(body, "cell 0 1, grow");

        spAnio.addChangeListener(e -> controller.loadData());
        spMes.addChangeListener(e -> controller.loadData());
        btnGenerar.addActionListener(e -> controller.generar());
        btnPdf.addActionListener(e -> controller.exportPdf());
        btnPrint.addActionListener(e -> controller.printPdf());
    }

    private JPanel buildResumenPanel() {
        JPanel panel = new JPanel(new GridLayout(1,3,10,0));
        panel.add(metric("Ventas", lblMinorista));
        panel.add(metric("Pedidos Especiales", lblEspecial));
        panel.add(metric("Pedidos Domicilio", lblDomicilio));
        return panel;
    }

    private JPanel metric(String title, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIStyle.getColorCardBg());
        p.putClientProperty(FlatClientProperties.STYLE, "arc:" + UIStyle.ARC_PILL);
        p.setBorder(UIStyle.FORM_MARGIN);
        JLabel t = new JLabel(title);
        t.putClientProperty(FlatClientProperties.STYLE,
                "font:+1; foreground:" + UIStyle.getHexSecondaryText());
        value.putClientProperty(FlatClientProperties.STYLE, "font:$h1.font");
        p.add(t);
        p.add(value);
        return p;
    }

    public JSpinner getSpAnio() { return spAnio; }
    public JSpinner getSpMes() { return spMes; }
    public DefaultTableModel getModel() { return model; }
    public JTable getTable() { return table; }
    public JScrollPane getScroll() { return scroll; }
    public JLabel getLblEmpty() { return lblEmpty; }
    public DefaultTableModel getModelCategoria() { return modelCategoria; }
    public JTable getTblCategoria() { return tblCategoria; }
    public JScrollPane getSpCategoria() { return spCategoria; }
    public JLabel getLblEmptyCategoria() { return lblEmptyCategoria; }
    public JLabel getLblMinorista() { return lblMinorista; }
    public JLabel getLblEspecial() { return lblEspecial; }
    public JLabel getLblDomicilio() { return lblDomicilio; }
    public JButton getBtnGenerar() { return btnGenerar; }
    public JButton getBtnPdf() { return btnPdf; }
    public JButton getBtnPrint() { return btnPrint; }

    @Override
    public void addNotify() {
        super.addNotify();
        if (firstLoad) {
            controller.loadData();
            firstLoad = false;
        }
    }

    @Override
    public void removeNotify() {
        BusyOverlay.hideFor(this);
        super.removeNotify();
    }

    /**
     * Habilita acciones según la existencia de un reporte previo.
     * @param existe si ya existe un PDF para el periodo
     */
    public void updateButtons(boolean existe) {
        btnGenerar.setEnabled(true);
        btnPdf.setEnabled(existe);
        btnPrint.setEnabled(existe);
    }

    @Override
    public void refresh() {
        controller.loadData();
    }

    @Override
    protected void registerShortcuts() {
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK),
                controller::exportPdf);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK),
                controller::printPdf);
        KeyUtils.registerKeyAction(this,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_DOWN_MASK),
                controller::generar);
        KeyUtils.registerRefreshAction(this, controller::loadData);
        KeyUtils.registerFocusAction(this,
                ((JSpinner.DefaultEditor) spAnio.getEditor()).getTextField());
    }
}

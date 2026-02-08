package com.comercialvalerio.presentation.ui.reportes;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import net.miginfocom.swing.MigLayout;

import com.comercialvalerio.presentation.controller.reportes.ReporteDiarioController;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.util.UIUtils;

import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import com.comercialvalerio.presentation.ui.core.Refreshable;
import com.comercialvalerio.presentation.ui.base.DatePickerField;
import com.comercialvalerio.presentation.ui.base.NonEditableTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DateFormatUtils;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class FormReporteDiario extends BaseForm implements Refreshable {
    private final JTable table = new NonEditableTable();
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblEmpty = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Fecha","Ventas","Pedidos","Bruto","Neto"},0);

    private final JTable tblPagos = new NonEditableTable();
    private final JScrollPane spPagos = new JScrollPane(tblPagos);
    private final JLabel lblEmptyPagos = new JLabel(UIStyle.TXT_NO_DATA, SwingConstants.CENTER);
    private final DefaultTableModel modelPagos = new DefaultTableModel(
            new String[]{"Método","Monto"},0);

    private final JButton btnGenerar = new JButton("Generar");
    private final JButton btnPdf = new JButton("Exportar a PDF");
    private final JButton btnPrint = new JButton("Imprimir");
    private JButton btnRefresh;
    private Timer refreshTimer;

    private final DatePickerField spFecha = new DatePickerField();
    private final ReporteDiarioController controller = new ReporteDiarioController(this);

    public FormReporteDiario(){
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]5[grow]"));
        setOpaque(false);
        setBorder(UIStyle.FORM_MARGIN);
        btnRefresh = UIUtils.createRefreshButton(controller::loadData);

        JPanel top = new JPanel();
        top.setOpaque(false);
        spFecha.setDateFormat(DateFormatUtils.getShortPattern());
        spFecha.setPreferredSize(new Dimension(UIStyle.DATE_FIELD_WIDTH,
                UIStyle.DATE_FIELD_HEIGHT));
        top.add(new JLabel("Fecha:"));
        top.add(spFecha);
        // botón de recarga
        ButtonStyles.styleBottom(btnGenerar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/play.svg");
        ButtonStyles.styleBottom(btnPdf, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        ButtonStyles.styleBottom(btnPrint, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        KeyUtils.setTooltipAndMnemonic(btnGenerar, KeyEvent.VK_G, "Generar");
        KeyUtils.setTooltipAndMnemonic(btnPdf, KeyEvent.VK_P, "Exportar a PDF");
        KeyUtils.setTooltipAndMnemonic(btnPrint, KeyEvent.VK_I, "Imprimir");
        btnGenerar.setEnabled(false);
        btnPdf.setEnabled(false);
        btnPrint.setEnabled(false);
        top.add(btnGenerar);
        top.add(btnPdf);
        top.add(btnPrint);

        HeaderPanel header = new HeaderPanel("Reporte Diario", btnRefresh);
        header.setBorder(new EmptyBorder(0,0,5,0));
        add(header, "cell 0 0, growx, wrap");

        JPanel body = new JPanel(new BorderLayout(5,5));
        body.setOpaque(false);
        body.add(top, BorderLayout.NORTH);

        table.setModel(model);
        table.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        table.setRowSelectionAllowed(false);
        tblPagos.setModel(modelPagos);
        tblPagos.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tblPagos.setRowSelectionAllowed(false);

        JPanel center = new JPanel(new BorderLayout(5,5));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);

        JPanel pagosPanel = new JPanel(new BorderLayout());
        pagosPanel.setOpaque(false);
        pagosPanel.add(new JLabel("Pagos por método"), BorderLayout.NORTH);
        pagosPanel.add(spPagos, BorderLayout.CENTER);
        center.add(pagosPanel, BorderLayout.SOUTH);

        body.add(center, BorderLayout.CENTER);
        add(body, "cell 0 1, grow");

        spFecha.addDateSelectionListener(e -> {
            controller.loadData();
            updateButtons();
        });
        btnGenerar.addActionListener(e -> controller.generar());
        btnPdf.addActionListener(e -> controller.exportPdf());
        btnPrint.addActionListener(e -> controller.printPdf());
        refreshTimer = new Timer(300_000, e -> controller.loadData());
        refreshTimer.setInitialDelay(300_000);
        controller.loadData();
        updateButtons();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTimer != null) {
            refreshTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.removeNotify();
    }

    public DatePickerField getSpFecha() { return spFecha; }
    public DefaultTableModel getModel() { return model; }
    public JTable getTable() { return table; }
    public JScrollPane getScroll() { return scroll; }
    public JLabel getLblEmpty() { return lblEmpty; }
    public DefaultTableModel getModelPagos() { return modelPagos; }
    public JTable getTblPagos() { return tblPagos; }
    public JScrollPane getSpPagos() { return spPagos; }
    public JLabel getLblEmptyPagos() { return lblEmptyPagos; }

    public JButton getBtnGenerar() { return btnGenerar; }
    public JButton getBtnPdf() { return btnPdf; }
    public JButton getBtnPrint() { return btnPrint; }

    /** Habilita o deshabilita acciones según la fecha elegida. */
    public void updateButtons() {
        boolean fechaOk = spFecha.getDate() != null;
        btnGenerar.setEnabled(fechaOk);
        boolean pdfDisponible = controller.isPdfAvailable();
        btnPdf.setEnabled(pdfDisponible);
        btnPrint.setEnabled(pdfDisponible);
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
        KeyUtils.registerFocusAction(this, spFecha);
    }
}

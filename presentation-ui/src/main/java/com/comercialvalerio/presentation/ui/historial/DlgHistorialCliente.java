package com.comercialvalerio.presentation.ui.historial;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;

import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.presentation.controller.historial.HistorialClienteController;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.common.BottomButtonsPanel;
import com.comercialvalerio.presentation.ui.common.HeaderPanel;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.formdev.flatlaf.FlatClientProperties;

/** Diálogo que muestra el historial de transacciones de un cliente seleccionado. */
public class DlgHistorialCliente extends BaseDialog {
    private final FormHistorialCliente form = new FormHistorialCliente();
    private final HistorialClienteController controller;
    private final JButton btnVerDetalle     = new JButton("Ver Detalle");
    private final JButton btnReimprimir     = new JButton("Imprimir Comprobante");
    private final JButton btnDescargar      = new JButton("Descargar Comprobante");
    private final JButton btnReimprimirOrden = new JButton("Imprimir Orden");
    private final JButton btnDescargarOrden = new JButton("Descargar Orden");
    private final JButton btnWhatsAppOrden   = new JButton("Enviar Orden por WhatsApp");
    private final JButton btnWhatsApp       = new JButton("Enviar Comprobante por WhatsApp");
    private HeaderPanel header;

    public DlgHistorialCliente(JFrame owner) {
        super(owner, "Historial por Cliente", true, null);
        this.controller = form.getController();
        controller.setUpdateButtonsCallback(this::updateButtons);
        buildUI();
        SwingUtilities.invokeLater(() -> form.getTable().requestFocusInWindow());
        ButtonStyles.styleBottom(btnVerDetalle, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/eye.svg");
        ButtonStyles.styleBottom(btnReimprimir, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        ButtonStyles.styleBottom(btnDescargar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        ButtonStyles.styleBottom(btnReimprimirOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        ButtonStyles.styleBottom(btnDescargarOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        ButtonStyles.styleBottom(btnWhatsAppOrden, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/whatsapp.svg");
        ButtonStyles.styleBottom(btnWhatsApp, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/whatsapp.svg");
        KeyUtils.setTooltipAndMnemonic(btnVerDetalle, KeyEvent.VK_V, "Ver Detalle");
        KeyUtils.setTooltipAndMnemonic(btnReimprimir, KeyEvent.VK_I, "Imprimir Comprobante");
        KeyUtils.setTooltipAndMnemonic(btnDescargar, KeyEvent.VK_D, "Descargar Comprobante");
        KeyUtils.setTooltipAndMnemonic(btnReimprimirOrden, KeyEvent.VK_R, "Imprimir Orden");
        KeyUtils.setTooltipAndMnemonic(btnDescargarOrden, KeyEvent.VK_O, "Descargar Orden");
        KeyUtils.setTooltipAndMnemonic(btnWhatsAppOrden, KeyEvent.VK_P, "Enviar Orden por WhatsApp");
        KeyUtils.setTooltipAndMnemonic(btnWhatsApp, KeyEvent.VK_W, "Enviar Comprobante por WhatsApp");
        btnVerDetalle.addActionListener(e -> controller.mostrarDetalle());
        btnReimprimir.addActionListener(e -> controller.reimprimirComprobante());
        btnDescargar.addActionListener(e -> controller.descargarComprobante());
        btnReimprimirOrden.addActionListener(e -> controller.reimprimirOrden());
        btnDescargarOrden.addActionListener(e -> controller.descargarOrden());
        btnWhatsAppOrden.addActionListener(e -> controller.enviarOrdenWhatsApp());
        btnWhatsApp.addActionListener(e -> controller.enviarComprobanteWhatsApp());
        KeyUtils.registerRefreshAction(getRootPane(), controller::refresh);
        form.getBtnRefresh().addActionListener(e -> controller.refresh());
        form.getTable().getSelectionModel().addListSelectionListener(e -> updateButtons());
        updateButtons();
        pack();
        DialogUtils.ensureMinWidth(this);
        int minWidth = 800;
        if (getWidth() < minWidth) {
            setSize(new Dimension(minWidth, getHeight()));
        }
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[]8[grow]8[]8[]push"));
        root.putClientProperty(FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_DEFAULT + ";");

        header = new HeaderPanel("Historial por Cliente", form.getBtnRefresh());
        header.setBorder(new EmptyBorder(0,0,5,10));
        root.add(header, "cell 0 0, growx, wrap");

        root.add(form, "cell 0 1, grow");

        JPanel panelOrden = new BottomButtonsPanel(
                btnVerDetalle, btnReimprimirOrden, btnDescargarOrden, btnWhatsAppOrden);
        root.add(panelOrden, "cell 0 2, alignx center, wrap");

        JPanel panelComp = new BottomButtonsPanel(
                btnReimprimir, btnDescargar, btnWhatsApp);
        root.add(panelComp, "cell 0 3, alignx center");

        root.setBorder(UIStyle.FORM_BORDER_VIOLET);
        setContentPane(root);
    }

    /**
     * Pre-carga el cliente indicado en el combo y obtiene su historial.
     */
    public void cargarCliente(ClienteDto cliente) {
        header.getTitleLabel().setText("Historial de " + cliente.nombreCompleto());
        controller.cargarHistorialDe(cliente);
        updateButtons();
    }

    public FormHistorialCliente getForm() { return form; }

    private void updateButtons() {
        int row = form.getTable().getSelectedRow();
        boolean sel = row >= 0;
        boolean reimp = false;
        boolean pedido = false;
        if (sel) {
            int modelRow = form.getTable().convertRowIndexToModel(row);
            Object est = form.getModel().getValueAt(modelRow, 5);
            Object tipo = form.getModel().getValueAt(modelRow, 6);
            if (tipo != null) {
                pedido = "Pedido".equalsIgnoreCase(tipo.toString());
            }
            if (est != null) {
                String s = est.toString();
                reimp = "Completada".equalsIgnoreCase(s)
                        || "Entregada".equalsIgnoreCase(s);
            }
        }
        btnReimprimir.setEnabled(reimp);
        btnDescargar.setEnabled(reimp);
        btnWhatsApp.setEnabled(reimp);
        btnVerDetalle.setEnabled(sel);
        btnReimprimirOrden.setEnabled(sel && pedido);
        btnDescargarOrden.setEnabled(sel && pedido);
        btnWhatsAppOrden.setEnabled(sel && pedido);
    }
}

package com.comercialvalerio.presentation.ui.pedidos;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.PhoneFilter;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.KeyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.KeyEvent;
import com.comercialvalerio.presentation.ui.util.CurrencyUtils;
import java.math.BigDecimal;

/** Diálogo mostrado tras entregar un pedido para generar su comprobante. */
public class DlgComprobantePedido extends BaseDialog {

    private final JCheckBox chkWhatsApp  = new JCheckBox("Enviar por WhatsApp:");
    private final JTextField txtTelefono = new JTextField();
    private final JLabel lblSub =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel lblCargo =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel lblTot =
            new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JButton btnConfirmar;
    private final JButton btnImprimir   = new JButton("Imprimir");
    private final JButton btnDescargar  = new JButton("Descargar");

    public DlgComprobantePedido(Window owner) {
        super(owner, "Comprobante", ModalityType.APPLICATION_MODAL, new JButton("Confirmar"));
        this.btnConfirmar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        updateTelefonoVisibility();
        chkWhatsApp.addItemListener(e -> updateTelefonoVisibility());
        SwingUtilities.invokeLater(() -> chkWhatsApp.requestFocusInWindow());
        pack();
        Dimension size = getSize();
        size.height += 30; // ligeramente más alto
        setSize(size);
        DialogUtils.ensureMinWidth(this);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.putClientProperty(FlatClientProperties.STYLE, "background:@background");
        root.setBackground(UIStyle.getColorCardBg());
        root.setBorder(new EmptyBorder(20,20,20,20));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Comprobante");
        title.putClientProperty(FlatClientProperties.STYLE,"font:$h1.font");
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new MigLayout("fillx, wrap, insets 5 0 0 0","[fill]"));
        body.setOpaque(false);
        root.add(body, BorderLayout.CENTER);

        body.add(new JSeparator(), "growx, gapbottom 10");

        JPanel pdfRow = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        pdfRow.setOpaque(false);
        pdfRow.add(chkWhatsApp);
        pdfRow.add(txtTelefono);
        txtTelefono.setColumns(12);
        ((AbstractDocument) txtTelefono.getDocument())
                .setDocumentFilter(new PhoneFilter(DbConstraints.LEN_TELEFONO));
        txtTelefono.setVisible(false);
        body.add(pdfRow, "gapbottom 15");

        JLabel det = new JLabel("Detalles");
        det.putClientProperty(FlatClientProperties.STYLE,"font:$h2.font");
        body.add(det, "gapbottom 5");

        JPanel totals = new JPanel(new GridLayout(3,2,8,0));
        totals.setOpaque(false);
        totals.add(new JLabel("Sub total"));
        totals.add(lblSub);
        totals.add(new JLabel("Cargo"));
        totals.add(lblCargo);
        totals.add(new JLabel("Total"));
        totals.add(lblTot);
        body.add(totals, "growx, gapbottom 20");

        ButtonStyles.styleBottom(btnConfirmar, 0x42A042, "com/comercialvalerio/presentation/ui/icon/svg/check.svg");
        ButtonStyles.styleBottom(btnImprimir, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/printer.svg");
        ButtonStyles.styleBottom(btnDescargar, UIStyle.RGB_ACTION_BLUE_LIGHT,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnConfirmar, KeyEvent.VK_C, "Confirmar");
        KeyUtils.setTooltipAndMnemonic(btnImprimir, KeyEvent.VK_I, "Imprimir");
        KeyUtils.setTooltipAndMnemonic(btnDescargar, KeyEvent.VK_D, "Descargar");
        btnImprimir.setEnabled(false);
        btnDescargar.setEnabled(false);
        JPanel btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnPane.setOpaque(false);
        btnPane.add(btnConfirmar);
        btnPane.add(btnImprimir);
        btnPane.add(btnDescargar);
        body.add(btnPane, "growx");

        getRootPane().setDefaultButton(btnConfirmar);
    }

    private void updateTelefonoVisibility() {
        boolean visible = chkWhatsApp.isSelected();
        txtTelefono.setVisible(visible);
        txtTelefono.getParent().revalidate();
        if (visible) {
            txtTelefono.requestFocusInWindow();
        }
    }

    public void setSubTotal(String t){ lblSub.setText(t); }
    public void setCargo(String t){ lblCargo.setText(t); }
    public void setTotal(String t){ lblTot.setText(t); }
    public boolean isEnviarWhatsApp(){ return chkWhatsApp.isSelected(); }
    public String getTelefono(){ return PhoneUtils.stripToDigits(txtTelefono.getText().trim()); }
    public JTextField getTxtTelefono(){ return txtTelefono; }
    public JCheckBox getChkWhatsApp(){ return chkWhatsApp; }
    public JButton getBtnConfirmar(){ return btnConfirmar; }
    public JButton getBtnImprimir(){ return btnImprimir; }
    public JButton getBtnDescargar(){ return btnDescargar; }
    public JLabel getLblCargo(){ return lblCargo; }

    /** Desactiva la opción de envío por WhatsApp tras confirmar. */
    public void disableWhatsAppOption() {
        chkWhatsApp.setSelected(false);
        chkWhatsApp.setEnabled(false);
        txtTelefono.setText("");
        txtTelefono.setEnabled(false);
        txtTelefono.setVisible(false);
    }

    /** Confirma descartar cambios de WhatsApp o teléfono al cerrar. */
    private void attemptCancel() {
        if (chkWhatsApp.isSelected() || !txtTelefono.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }
}

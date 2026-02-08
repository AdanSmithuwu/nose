package com.comercialvalerio.presentation.ui.pedidos;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.NumericFilter;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import java.awt.Window;
import java.awt.FlowLayout;
import net.miginfocom.swing.MigLayout;
import java.math.BigDecimal;

/** Diálogo para ingresar pagos al marcar un pedido como entregado. */
public class DlgPagoPedido extends BaseDialog {

    private final BigDecimal total;

    private final JCheckBox chkDigital  = new JCheckBox("Billetera Digital");
    private final JTextField txtDigital = new JTextField(10);
    private final JCheckBox chkEfectivo = new JCheckBox("Efectivo");
    private final JTextField txtEfectivo= new JTextField(10);
    private final JButton btnGuardar;

    public DlgPagoPedido(Window owner, BigDecimal total) {
        super(owner, "Pagos", ModalityType.APPLICATION_MODAL, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        this.total = total == null ? BigDecimal.ZERO : total;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> chkDigital.requestFocusInWindow());
        ((AbstractDocument) txtDigital.getDocument())
                .setDocumentFilter(new NumericFilter(txtDigital));
        ((AbstractDocument) txtEfectivo.getDocument())
                .setDocumentFilter(new NumericFilter(txtEfectivo));
        chkDigital.addActionListener(e -> updateFields());
        chkEfectivo.addActionListener(e -> updateFields());
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptCancel();
            }
        });
        pack();
        setLocationRelativeTo(owner);
        updateFields();
    }

    private void buildUI() {
        JPanel root = new JPanel(new MigLayout(
                "fillx, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow][grow]",
                "[]10[]10[]"));
        root.putClientProperty(FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_DEFAULT);
        root.setBackground(UIStyle.getColorCardBg());
        root.setBorder(new EmptyBorder(20,20,20,20));
        setContentPane(root);

        JLabel lblTitle = new JLabel("Métodos de Pago");
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:$h2.font");
        root.add(lblTitle, "cell 0 0 2 1, wrap");

        root.add(chkDigital, "cell 0 1");
        root.add(txtDigital, "cell 1 1, growx");
        root.add(chkEfectivo, "cell 0 2");
        root.add(txtEfectivo, "cell 1 2, growx, wrap");

        ButtonStyles.styleBottom(btnGuardar, 0x2ECC71, "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnGuardar);
        root.add(btnPanel, "cell 0 3 2 1, growx");

        getRootPane().setDefaultButton(btnGuardar);
    }

    private void updateFields() {
        boolean digital = chkDigital.isSelected();
        boolean efectivo = chkEfectivo.isSelected();
        if (digital && !efectivo) {
            txtDigital.setText(total.toPlainString());
            txtDigital.setEnabled(false);
            txtEfectivo.setText("");
            txtEfectivo.setEnabled(false);
        } else if (!digital && efectivo) {
            txtEfectivo.setText(total.toPlainString());
            txtEfectivo.setEnabled(false);
            txtDigital.setText("");
            txtDigital.setEnabled(false);
        } else {
            txtDigital.setEnabled(digital);
            if (!digital) txtDigital.setText("");
            txtEfectivo.setEnabled(efectivo);
            if (!efectivo) txtEfectivo.setText("");
        }
    }

    public JCheckBox  getChkDigital()  { return chkDigital; }
    public JTextField getTxtDigital()  { return txtDigital; }
    public JCheckBox  getChkEfectivo() { return chkEfectivo; }
    public JTextField getTxtEfectivo() { return txtEfectivo; }
    public JButton    getBtnGuardar()  { return btnGuardar; }

    /** Confirma descartar los pagos ingresados al cerrar. */
    private void attemptCancel() {
        if (chkDigital.isSelected() || chkEfectivo.isSelected()
                || !txtDigital.getText().isBlank()
                || !txtEfectivo.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }
}

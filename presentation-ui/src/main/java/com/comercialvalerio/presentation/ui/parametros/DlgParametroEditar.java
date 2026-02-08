package com.comercialvalerio.presentation.ui.parametros;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import javax.swing.text.AbstractDocument;
import com.comercialvalerio.presentation.ui.util.NumericFilter;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;

import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Diálogo para editar un parámetro del sistema existente. */
public class DlgParametroEditar extends BaseDialog {

    private final JTextField txtClave = new JTextField();
    private final JTextField txtValor = new JTextField();
    private final JTextField txtDescripcion = new JTextField();
    private final JButton btnGuardar;

    public DlgParametroEditar(JFrame owner) {
        super(owner, "Editar Parámetro", true, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtValor.requestFocusInWindow());
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptCancel();
            }
        });
        pack();
        DialogUtils.ensureMinWidth(this);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel p = new JPanel(new MigLayout(
                "fillx, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP));
        p.putClientProperty(
                FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_DEFAULT + ";");

        txtClave.setEditable(false);
        txtDescripcion.setEditable(false);

        p.add(new JLabel("Clave:"), "wrap");
        p.add(txtClave, "growx, wrap");
        p.add(new JLabel("Descripción:"), "wrap");
        p.add(txtDescripcion, "growx, wrap");
        p.add(new JLabel("Valor:"), "wrap");
        ((AbstractDocument) txtValor.getDocument())
                .setDocumentFilter(new NumericFilter(txtValor));
        txtValor.setInputVerifier(new RequiredVerifier());
        txtValor.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese valor");
        p.add(txtValor, "growx, wrap");
        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");
        p.add(btnGuardar, "span, right");
        setContentPane(p);
    }

    private void attemptCancel() {
        if (!txtValor.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtClave() { return txtClave; }
    public JTextField getTxtValor() { return txtValor; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }
    public JButton getBtnGuardar() { return btnGuardar; }

}

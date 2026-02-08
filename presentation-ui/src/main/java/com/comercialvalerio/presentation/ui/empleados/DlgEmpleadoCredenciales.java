package com.comercialvalerio.presentation.ui.empleados;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;

/** Diálogo para actualizar las credenciales del empleado. */
public class DlgEmpleadoCredenciales extends BaseDialog {

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JButton btnGuardar;

    public DlgEmpleadoCredenciales(JFrame owner) {
        super(owner, "Actualizar Credenciales", true, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtUsuario.requestFocusInWindow());
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
                "background:@background" + "; arc:" + UIStyle.ARC_DEFAULT);
        ((javax.swing.text.AbstractDocument) txtUsuario.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_USUARIO));
        txtUsuario.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese usuario");
        p.add(new JLabel("Usuario:"), "wrap");
        p.add(txtUsuario, "growx, wrap");
        p.add(new JLabel("Nueva contraseña:"), "wrap");
        txtClave.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Opcional");
        txtClave.putClientProperty(FlatClientProperties.STYLE,
                "showRevealButton:true; showCapsLock:true");
        p.add(txtClave, "growx, wrap");
        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");
        p.add(btnGuardar, "span, right");
        setContentPane(p);
    }

    private void attemptCancel() {
        if (!txtUsuario.getText().isBlank() || txtClave.getPassword().length > 0) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtUsuario() { return txtUsuario; }
    public JPasswordField getTxtClave() { return txtClave; }
    public JButton getBtnGuardar() { return btnGuardar; }

}

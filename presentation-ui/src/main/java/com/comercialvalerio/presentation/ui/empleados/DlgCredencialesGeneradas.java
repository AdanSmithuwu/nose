package com.comercialvalerio.presentation.ui.empleados;

import java.awt.Window;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.KeyUtils;

import net.miginfocom.swing.MigLayout;

/** Diálogo sencillo que muestra las credenciales generadas. */
public final class DlgCredencialesGeneradas extends BaseDialog {

    public DlgCredencialesGeneradas(Window owner, String usuario, String clave) {
        super(owner, "Credenciales Generadas", ModalityType.APPLICATION_MODAL,
                new JButton("Cerrar"));
        JButton btnCerrar = getDefaultButton();
        KeyUtils.setTooltipAndMnemonic(btnCerrar, KeyEvent.VK_C, "Cerrar");
        buildUI(usuario, clave, btnCerrar);
        btnCerrar.addActionListener(e -> dispose());
        pack();
        DialogUtils.ensureMinWidth(this);
        setLocationRelativeTo(owner);
    }

    private void buildUI(String usuario, String clave, JButton btnCerrar) {
        JPanel p = new JPanel(new MigLayout(
                "fillx, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP));
        p.putClientProperty(
                FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_DEFAULT);
        p.add(new JLabel("Usuario generado:"), "wrap");
        JTextField txtUsuario = new JTextField(usuario);
        txtUsuario.setEditable(false);
        p.add(txtUsuario, "growx, wrap");
        p.add(new JLabel("Contrase\u00f1a:"), "wrap");
        JTextField txtClave = new JTextField(clave);
        txtClave.setEditable(false);
        p.add(txtClave, "growx, wrap");
        ButtonStyles.styleBottom(btnCerrar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/check.svg");
        p.add(btnCerrar, "span, right");
        setContentPane(p);
    }
}

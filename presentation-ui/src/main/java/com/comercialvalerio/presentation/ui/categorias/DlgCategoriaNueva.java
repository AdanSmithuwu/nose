package com.comercialvalerio.presentation.ui.categorias;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.controller.categorias.CategoriaNuevaController;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.common.DbConstraints;
import javax.swing.JButton;
import javax.swing.JDialog;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import net.miginfocom.swing.MigLayout;

/** Diálogo para registrar una categoría. */
public class DlgCategoriaNueva extends BaseDialog {

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtDescripcion = new JTextField();
    private final JButton btnGuardar;
    private final CategoriaNuevaController controller;

    public DlgCategoriaNueva(JFrame owner) {
        super(owner, "Nueva Categoría", true, new JButton("Registrar"));
        this.btnGuardar = getDefaultButton();
        this.controller = new CategoriaNuevaController(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtNombre.requestFocusInWindow());
        btnGuardar.addActionListener(e -> registrar());
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
                "background:@background"
                        + "; arc:" + UIStyle.ARC_DEFAULT);
        ((javax.swing.text.AbstractDocument) txtNombre.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_CATEGORIA));
        ((javax.swing.text.AbstractDocument) txtDescripcion.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_DESCRIPCION));
        txtNombre.setInputVerifier(new RequiredVerifier());
        txtNombre.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese nombre");
        txtDescripcion.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese descripción");
        p.add(new JLabel("Nombre:"), "wrap");
        p.add(txtNombre, "growx, wrap");
        p.add(new JLabel("Descripci\u00f3n:"), "wrap");
        p.add(txtDescripcion, "growx, wrap");
        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_R, "Registrar");
        p.add(btnGuardar, "span, right");
        setContentPane(p);
    }

    /** Llama al servicio REST para crear la categoría y cierra el diálogo. */
    private void registrar() {
        controller.registrar();
    }

    private void attemptCancel() {
        if (!txtNombre.getText().isBlank()
                || !txtDescripcion.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }
    public JButton getBtnGuardar() { return btnGuardar; }

}

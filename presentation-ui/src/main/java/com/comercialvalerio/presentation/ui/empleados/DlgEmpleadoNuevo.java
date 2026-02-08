package com.comercialvalerio.presentation.ui.empleados;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.controller.empleados.EmpleadoNuevoController;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.presentation.ui.util.NumericVerifier;
import com.comercialvalerio.presentation.ui.util.PhoneFilter;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/** Diálogo para registrar un empleado. */
public final class DlgEmpleadoNuevo extends BaseDialog {

    private final JTextField txtNombres   = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JTextField txtDni       = new JTextField();
    private final JTextField txtTelefono  = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final JButton btnGuardar;
    private final EmpleadoNuevoController controller;

    public DlgEmpleadoNuevo(Window owner) {
        super(owner, "Nuevo Empleado", ModalityType.APPLICATION_MODAL, new JButton("Registrar"));
        this.btnGuardar = getDefaultButton();
        this.controller = new EmpleadoNuevoController(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtNombres.requestFocusInWindow());
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
                "background:@background" + "; arc:" + UIStyle.ARC_DEFAULT);

        txtNombres.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese nombre");
        txtApellidos.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese apellidos");
        txtDni.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese DNI");
        txtTelefono.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese teléfono");
        txtClave.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Opcional");
        txtClave.putClientProperty(FlatClientProperties.STYLE,
                "showRevealButton:true; showCapsLock:true");
        ((AbstractDocument) txtDni.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.LEN_DNI, txtDni));
        ((AbstractDocument) txtTelefono.getDocument())
                .setDocumentFilter(new PhoneFilter(DbConstraints.LEN_TELEFONO));
        ((AbstractDocument) txtNombres.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_PERSONA));
        ((AbstractDocument) txtApellidos.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_PERSONA));

        RequiredVerifier req = new RequiredVerifier();
        NumericVerifier num = new NumericVerifier();
        txtNombres.setInputVerifier(req);
        txtApellidos.setInputVerifier(req);
        txtDni.setInputVerifier(num);

        p.add(new JLabel("Nombre:"), "wrap");
        p.add(txtNombres, "growx, wrap");
        p.add(new JLabel("Apellidos:"), "wrap");
        p.add(txtApellidos, "growx, wrap");
        p.add(new JLabel("DNI:"), "wrap");
        p.add(txtDni, "growx, wrap");
        p.add(new JLabel("Teléfono:"), "wrap");
        p.add(txtTelefono, "growx, wrap");
        p.add(new JLabel("Contraseña (opcional):"), "wrap");
        p.add(txtClave, "growx, wrap");

        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_R, "Registrar");
        p.add(btnGuardar, "span, right");
        setContentPane(p);
    }

    private void registrar() {
        controller.registrar();
    }

    private void attemptCancel() {
        if (!txtNombres.getText().isBlank()
                || !txtApellidos.getText().isBlank()
                || !txtDni.getText().isBlank()
                || !txtTelefono.getText().isBlank()
                || txtClave.getPassword().length > 0) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JButton getBtnGuardar() { return btnGuardar; }
    public JTextField getTxtNombres() { return txtNombres; }
    public JTextField getTxtApellidos() { return txtApellidos; }
    public JTextField getTxtDni() { return txtDni; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JPasswordField getTxtClave() { return txtClave; }

}

package com.comercialvalerio.presentation.ui.clientes;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.AbstractAction;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.controller.clientes.ClienteNuevoController;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DigitFilter;
import com.comercialvalerio.presentation.ui.util.PhoneFilter;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/** Diálogo para registrar un nuevo cliente. */
public class DlgClienteNuevo extends BaseDialog {

    private final JTextField txtNombre    = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JTextField txtDni       = new JTextField();
    private final JTextField txtTelefono  = new JTextField();
    private final JTextField txtDireccion = new JTextField();
    private final JButton btnGuardar;
    private final ClienteNuevoController controller;

    public DlgClienteNuevo(Window owner) {
        super(owner, "Nuevo Cliente", java.awt.Dialog.ModalityType.APPLICATION_MODAL, new JButton("Registrar"));
        this.btnGuardar = getDefaultButton();
        this.controller = new ClienteNuevoController(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtNombre.requestFocusInWindow());
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_R, "Registrar");
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
        JPanel panel = new JPanel(new MigLayout(
                "fillx, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow][grow]"));
        panel.putClientProperty(
                FlatClientProperties.STYLE,
                "background:@background" + "; arc:" + UIStyle.ARC_DEFAULT);
        ((AbstractDocument) txtDni.getDocument())
                .setDocumentFilter(new DigitFilter(DbConstraints.LEN_DNI, txtDni));
        ((AbstractDocument) txtTelefono.getDocument())
                .setDocumentFilter(new PhoneFilter(DbConstraints.LEN_TELEFONO));
        ((AbstractDocument) txtDireccion.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_DIRECCION));
        ((AbstractDocument) txtNombre.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_PERSONA));
        ((AbstractDocument) txtApellidos.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_NOMBRE_PERSONA));
        RequiredVerifier req = new RequiredVerifier();
        txtNombre.setInputVerifier(req);
        txtApellidos.setInputVerifier(req);
        txtDni.setInputVerifier(req);
        txtDireccion.setInputVerifier(req);
        txtNombre.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese nombre");
        txtApellidos.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese apellidos");
        txtDni.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese DNI");
        txtTelefono.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese teléfono");
        txtDireccion.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese dirección");

        // fila 1
        panel.add(new JLabel("Nombre:"), "cell 0 0");
        panel.add(new JLabel("Apellidos:"), "cell 1 0, wrap");
        panel.add(txtNombre, "cell 0 1, growx");
        panel.add(txtApellidos, "cell 1 1, growx, wrap");

        // fila 2
        panel.add(new JLabel("DNI:"), "cell 0 2");
        panel.add(new JLabel("Teléfono:"), "cell 1 2, wrap");
        panel.add(txtDni, "cell 0 3, growx");
        panel.add(txtTelefono, "cell 1 3, growx, wrap");

        // fila 3 (dirección ocupa ambos anchos)
        panel.add(new JLabel("Dirección:"), "cell 0 4 2 1, wrap");
        panel.add(txtDireccion, "cell 0 5 2 1, growx, wrap");

        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        panel.add(btnGuardar, "cell 0 6 2 1, tag ok, right");
        setContentPane(panel);
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public JTextField getTxtNombre() {
        return txtNombre;
    }

    public JTextField getTxtApellidos() {
        return txtApellidos;
    }

    public JTextField getTxtDni() {
        return txtDni;
    }

    public JTextField getTxtTelefono() {
        return txtTelefono;
    }

    public JTextField getTxtDireccion() {
        return txtDireccion;
    }

    /** Delega en el controlador el registro del cliente. */
    private void registrar() {
        controller.registrar();
    }

    private void attemptCancel() {
        if (!txtNombre.getText().isBlank()
                || !txtApellidos.getText().isBlank()
                || !txtDni.getText().isBlank()
                || !txtTelefono.getText().isBlank()
                || !txtDireccion.getText().isBlank()) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

}

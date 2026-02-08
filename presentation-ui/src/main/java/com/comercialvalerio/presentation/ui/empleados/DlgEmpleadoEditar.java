package com.comercialvalerio.presentation.ui.empleados;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import com.comercialvalerio.application.dto.RolDto;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.presentation.ui.util.NumericVerifier;
import com.comercialvalerio.presentation.ui.util.PhoneFilter;
import com.comercialvalerio.presentation.ui.util.RequiredVerifier;
import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

/** Diálogo para editar un empleado existente. */
public class DlgEmpleadoEditar extends BaseDialog {

    private final JTextField txtNombres   = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JTextField txtDni       = new JTextField();
    private final JTextField txtTelefono  = new JTextField();
    private final JComboBox<RolDto> cboRol = new JComboBox<>();
    private final JButton btnGuardar;
    private final JButton btnCredenciales = new JButton("Credenciales...");

    public DlgEmpleadoEditar(Window owner) {
        super(owner, "Editar Empleado", ModalityType.APPLICATION_MODAL, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptCancel();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtNombres.requestFocusInWindow());
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
        txtTelefono.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Ingrese teléfono");
        p.add(new JLabel("Nombres:"), "wrap");
        p.add(txtNombres, "growx, wrap");
        p.add(new JLabel("Apellidos:"), "wrap");
        p.add(txtApellidos, "growx, wrap");
        p.add(new JLabel("DNI:"), "wrap");
        txtDni.setEditable(false);
        p.add(txtDni, "growx, wrap");
        p.add(new JLabel("Teléfono:"), "wrap");
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
        cboRol.setInputVerifier(req);
        cboRol.setPrototypeDisplayValue(new RolDto(0, "XXXXXXXXXXXX", (short) 0));
        cboRol.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Rol");
        cboRol.putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DIALOG);
        p.add(txtTelefono, "growx, wrap");
        p.add(new JLabel("Rol:"), "wrap");
        p.add(cboRol, "growx, wrap");
        ButtonStyles.styleBottom(btnCredenciales, UIStyle.RGB_ACTION_PURPLE,
                "com/comercialvalerio/presentation/ui/icon/svg/settings.svg");
        ButtonStyles.styleBottom(btnGuardar, UIStyle.RGB_ACTION_GREEN,
                "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnCredenciales, KeyEvent.VK_C, "Credenciales");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");
        p.add(btnCredenciales, "span, split 2, right");
        p.add(btnGuardar);
        setContentPane(p);
    }

    private void attemptCancel() {
        if (!txtNombres.getText().isBlank()
                || !txtApellidos.getText().isBlank()
                || !txtDni.getText().isBlank()
                || !txtTelefono.getText().isBlank()
                || cboRol.getSelectedItem() != null) {
            if (!DialogUtils.confirmAction(this, "¿Descartar cambios?"))
                return;
        }
        dispose();
    }

    public JTextField getTxtNombres() { return txtNombres; }
    public JTextField getTxtApellidos() { return txtApellidos; }
    public JTextField getTxtDni() { return txtDni; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JComboBox<RolDto> getCboRol() { return cboRol; }
    public JButton getBtnGuardar() { return btnGuardar; }
    public JButton getBtnCredenciales() { return btnCredenciales; }

}

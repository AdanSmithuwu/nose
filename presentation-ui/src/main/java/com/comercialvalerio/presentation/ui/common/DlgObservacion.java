package com.comercialvalerio.presentation.ui.common;

import com.comercialvalerio.presentation.ui.theme.UIStyle;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.comercialvalerio.presentation.ui.base.BaseDialog;
import com.comercialvalerio.presentation.controller.common.ObservacionController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyEvent;
import com.comercialvalerio.presentation.ui.util.LengthFilter;
import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import net.miginfocom.swing.MigLayout;

/**
 * Diálogo simple para registrar / editar una Observación.
 *  – Caja de texto multilínea.
 *  – Botones “Cancelar” (rojo) y “Guardar 💾” (verde).
 */
public class DlgObservacion extends BaseDialog {

    /* widgets públicos para el controller */
    private final JTextArea txtObs   = new JTextArea(6, 30);
    private final JButton   btnGuardar;
    private final JButton   btnCancelar = new JButton("Cancelar");
    private final ObservacionController controller;

    public DlgObservacion(Window owner) {
        super(owner, "Observación", ModalityType.APPLICATION_MODAL, new JButton("Guardar"));
        this.btnGuardar = getDefaultButton();
        this.controller = new ObservacionController(this);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.cancelar();
            }
        });
        buildUI();
        SwingUtilities.invokeLater(() -> txtObs.requestFocusInWindow());
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelar();
            }
        });
        pack();
        setLocationRelativeTo(owner);
    }

    /* --------------------------------------------------------------------- */
    private void buildUI() {
        JPanel root = new JPanel(new MigLayout(
                "fillx, insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]",
                "[grow]15[]"));
        root.putClientProperty(FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_DEFAULT);
        root.setBackground(UIStyle.getColorCardBg());
        root.setBorder(new EmptyBorder(20,20,20,20));
        setContentPane(root);

        /* ----- área de texto --------------------------------------------- */
        txtObs.setLineWrap(true);
        txtObs.setWrapStyleWord(true);
        ((javax.swing.text.AbstractDocument) txtObs.getDocument())
                .setDocumentFilter(new LengthFilter(DbConstraints.LEN_OBSERVACION));
        JScrollPane sp = new JScrollPane(txtObs);
        root.add(sp, "grow, wrap");

        /* ----- botones ---------------------------------------------------- */
        ButtonStyles.styleBottom(btnCancelar, 0xE74C3C,
                "com/comercialvalerio/presentation/ui/icon/svg/close_circle.svg");
        KeyUtils.setTooltipAndMnemonic(btnCancelar, KeyEvent.VK_C, "Cancelar");
        btnCancelar.addActionListener(e -> controller.cancelar());

        ButtonStyles.styleBottom(btnGuardar, 0x2ECC71, "com/comercialvalerio/presentation/ui/icon/svg/save.svg");
        KeyUtils.setTooltipAndMnemonic(btnGuardar, KeyEvent.VK_G, "Guardar");
        btnGuardar.addActionListener(e -> controller.guardar());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);

        root.add(btnPanel, "growx");

        /* tecla Enter = Guardar */
        getRootPane().setDefaultButton(btnGuardar);
    }

    /* --------------------------------------------------------------------- */
    public JTextArea getTxtObs()    { return txtObs;    }
    public JButton   getBtnGuardar(){ return btnGuardar;}
    public ObservacionController getController() { return controller; }

}

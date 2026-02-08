package com.comercialvalerio.presentation.ui.mantenimiento;

import com.comercialvalerio.presentation.controller.mantenimiento.MantenimientoController;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.comercialvalerio.presentation.ui.util.ButtonStyles;
import com.comercialvalerio.presentation.ui.util.KeyUtils;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;

import javax.swing.*;
import com.comercialvalerio.presentation.ui.base.BaseForm;
import java.awt.event.KeyEvent;

/**
 * Herramientas de mantenimiento avanzadas disponibles solo para administradores.
 */
public class FormMantenimiento extends BaseForm {

    private final JButton btnRecalcular = new JButton("Recalcular Stock");
    private final JButton btnDepurar = new JButton("Depurar Bitácora");
    private final MantenimientoController controller = new MantenimientoController(this);
    private final JLabel lblTitulo = new JLabel("Opciones de Mantenimiento");
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            updateTitleStyle();
        }
    };

    public FormMantenimiento() {
        buildUI();
        btnRecalcular.addActionListener(e -> controller.recalcularStock());
        btnDepurar.addActionListener(e -> controller.depurarBitacora());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UIManager.addPropertyChangeListener(lafListener);
    }

    @Override
    public void removeNotify() {
        UIManager.removePropertyChangeListener(lafListener);
        super.removeNotify();
    }

    private void buildUI() {
        setLayout(new MigLayout("fill,insets " + UIStyle.FORM_INSETS + ", gap " + UIStyle.FORM_GAP,
                "[grow]", "[][grow]") );
        setOpaque(false);
        putClientProperty(FlatClientProperties.STYLE, "background:@background");

        updateTitleStyle();
        add(lblTitulo, "wrap");

        JLabel lblInfo = new JLabel("<html>Estas opciones permiten recalcular el stock global " +
                "y depurar la bitácora de accesos.\n" +
                "Son acciones avanzadas; utilícelas solo para resolver inconsistencias y " +
                "previa copia de seguridad.</html>");
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.putClientProperty(FlatClientProperties.STYLE,
                "font:+1; foreground:" + UIStyle.getHexSecondaryText());
        add(lblInfo, "growx, alignx center, wrap");

        ButtonStyles.styleAction(btnRecalcular, UIStyle.RGB_ACTION_PURPLE,
                "com/comercialvalerio/presentation/ui/icon/svg/refresh.svg");
        KeyUtils.setTooltipAndMnemonic(btnRecalcular, KeyEvent.VK_R, "Recalcular Stock");

        ButtonStyles.styleAction(btnDepurar, UIStyle.RGB_ACTION_RED,
                "com/comercialvalerio/presentation/ui/icon/svg/history.svg");
        KeyUtils.setTooltipAndMnemonic(btnDepurar, KeyEvent.VK_D, "Depurar Bitácora");

        JPanel pnlRecalcular = sector(
                "Recalcula los saldos globales si detecta diferencias." +
                " Úselo después de ajustar inventario.",
                btnRecalcular);

        JPanel pnlDepurar = sector(
                "Elimina registros antiguos de la bitácora para liberar espacio." +
                " Úselo cuando la base crezca demasiado.",
                btnDepurar);

        JPanel actions = new JPanel(new MigLayout("insets 0, gap 20", "[grow,fill][grow,fill]", "[]"));
        actions.setOpaque(false);
        actions.add(pnlRecalcular, "growx");
        actions.add(pnlDepurar, "growx");
        add(actions, "growx, alignx center, wrap");

        setBorder(UIStyle.FORM_BORDER_VIOLET);
    }

    public JButton getBtnRecalcular() { return btnRecalcular; }
    public JButton getBtnDepurar() { return btnDepurar; }

    private JPanel sector(String texto, JButton boton) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIStyle.getColorCardBg());
        p.putClientProperty(FlatClientProperties.STYLE,
                "background:@background; arc:" + UIStyle.ARC_PILL);
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lbl = new JLabel("<html" + ">" + texto + "</html>");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.putClientProperty(FlatClientProperties.STYLE,
                "font:+1; foreground:" + UIStyle.getHexSecondaryText());
        p.add(lbl);
        p.add(Box.createVerticalStrut(10));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(boton);
        return p;
    }

    private void updateTitleStyle() {
        lblTitulo.putClientProperty(FlatClientProperties.STYLE,
                "font:$h1.font; foreground:" + UIStyle.getHexDarkText());
    }

    @Override
    protected void registerShortcuts() {
        // No hay atajos adicionales en esta vista
    }
}

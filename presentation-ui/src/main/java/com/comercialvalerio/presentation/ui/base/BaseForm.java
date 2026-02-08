package com.comercialvalerio.presentation.ui.base;

import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics;
import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;

/**
 * Panel base que registra sus atajos la primera vez que se agrega al contenedor.
 */
public abstract class BaseForm extends JPanel {

    private boolean shortcutsRegistered;

    public BaseForm() {
        setBorder(UIStyle.FORM_MARGIN);
        putClientProperty(FlatClientProperties.STYLE,
                "arc:" + UIStyle.ARC_DIALOG + "; background:@background");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) {
            bg = UIStyle.getColorFormBg();
        }
        g.setColor(bg);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!shortcutsRegistered) {
            registerShortcuts();
            shortcutsRegistered = true;
        }
    }

    /**
     * Implementado por las subclases para registrar sus atajos de teclado.
     */
    protected abstract void registerShortcuts();
}

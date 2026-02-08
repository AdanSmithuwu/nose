package com.comercialvalerio.presentation.ui.common;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.FlatClientProperties;
import com.comercialvalerio.presentation.ui.theme.UIStyle;

/** Panel de cabecera reutilizable con título y botones opcionales. */
public class HeaderPanel extends JPanel {
    private final JLabel titleLabel;
    private final PropertyChangeListener lafListener = e -> {
        if ("lookAndFeel".equals(e.getPropertyName())) {
            updateTitleStyle();
        }
    };

    /**
     * Crea una cabecera con el título indicado y botones opcionales.
     * Aplica por defecto un borde {@code new EmptyBorder(5,20,5,0)}.
     */
    public HeaderPanel(String title, JComponent... buttons) {
        this(title, new EmptyBorder(5,20,5,0), buttons);
    }

    /**
     * Crea una cabecera con borde personalizado, título y botones opcionales.
     */
    public HeaderPanel(String title, Border border, JComponent... buttons) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(border);
        titleLabel = new JLabel(title);
        updateTitleStyle();
        add(titleLabel, BorderLayout.WEST);
        if (buttons != null && buttons.length > 0) {
            if (buttons.length == 1) {
                add(buttons[0], BorderLayout.EAST);
            } else {
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
                btnPanel.setOpaque(false);
                for (JComponent b : buttons) {
                    btnPanel.add(b);
                }
                add(btnPanel, BorderLayout.EAST);
            }
        }
    }

    /** Devuelve la etiqueta del título para personalización adicional. */
    public JLabel getTitleLabel() { return titleLabel; }

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

    private void updateTitleStyle() {
        titleLabel.putClientProperty(FlatClientProperties.STYLE,
                "font:$h1.font; foreground:" + UIStyle.getHexDarkText());
    }
}

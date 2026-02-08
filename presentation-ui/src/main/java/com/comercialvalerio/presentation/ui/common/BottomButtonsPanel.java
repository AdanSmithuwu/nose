package com.comercialvalerio.presentation.ui.common;

import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel sencillo que acomoda los botones de acción en la zona inferior
 * estándar utilizada en los formularios de gestión.
 */
public class BottomButtonsPanel extends JPanel {
    /** Crea un panel con botones centrados y espaciados por igual. */
    public BottomButtonsPanel(JComponent... buttons) {
        super(new FlowLayout(FlowLayout.CENTER, 40, 0));
        setOpaque(false);
        if (buttons != null) {
            for (JComponent b : buttons) {
                add(b);
            }
        }
    }
}

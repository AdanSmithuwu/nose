package com.comercialvalerio.presentation.ui.util;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Métodos utilitarios para configurar diálogos. */
public final class DialogUtils {

    private DialogUtils() {}

    /**
     * Registra Escape para cerrar el diálogo y establece el botón indicado
     * como predeterminado.
     *
     * @param dialog  diálogo a configurar
     * @param saveBtn botón invocado cuando se ejecuta la acción predeterminada;
     *                puede ser {@code null}
     */
    public static void registerCloseSaveKeys(JDialog dialog, JButton saveBtn) {
        JRootPane rp = dialog.getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        am.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        if (saveBtn != null) {
            rp.setDefaultButton(saveBtn);
        }
    }

    /**
     * Fija el tamaño actual del diálogo y deshabilita el redimensionamiento.
     *
     * @param dialog diálogo a configurar
     */
    public static void fixSize(JDialog dialog) {
        Dimension size = dialog.getSize();
        dialog.setMinimumSize(size);
        dialog.setMaximumSize(size);
        dialog.setResizable(false);
    }

    /**
     * Muestra un diálogo de confirmación estándar usando el componente padre y
     * el mensaje indicados.
     *
     * @param parent  componente padre para anclar el diálogo
     * @param message mensaje de confirmación
     * @return {@code true} si el usuario elige "Sí"
     */
    public static boolean confirmAction(java.awt.Component parent, String message) {
        int opt = JOptionPane.showConfirmDialog(parent, message,
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return opt == JOptionPane.YES_OPTION;
    }

    /**
     * Asegura que el ancho del diálogo sea al menos un cuarto de la pantalla.
     *
     * @param dialog diálogo a actualizar
     */
    public static void ensureMinWidth(JDialog dialog) {
        int minWidth = Toolkit.getDefaultToolkit().getScreenSize().width / 4;
        Dimension size = dialog.getSize();
        if (size.width < minWidth) {
            size.width = minWidth;
            dialog.setSize(size);
        }
        Dimension min = dialog.getMinimumSize();
        if (min.width < minWidth) {
            dialog.setMinimumSize(new Dimension(minWidth, min.height));
        }
    }
}

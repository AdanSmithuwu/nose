package com.comercialvalerio.presentation.ui.base;

import javax.swing.*;
import java.awt.*;

import com.comercialvalerio.presentation.ui.util.DialogUtils;

/**
 * Diálogo base que registra atajos comunes y opcionalmente fija el tamaño.
 */
public class BaseDialog extends JDialog {

    private final JButton defaultButton;

    public BaseDialog(Window owner, String title, ModalityType modalityType,
                       JButton defaultButton) {
        this(owner, title, modalityType, defaultButton, false);
    }

    public BaseDialog(Window owner, String title, ModalityType modalityType,
                       JButton defaultButton, boolean fixSize) {
        super(owner, title, modalityType);
        this.defaultButton = defaultButton;
        SwingUtilities.invokeLater(() -> {
            DialogUtils.registerCloseSaveKeys(BaseDialog.this, defaultButton);
            if (fixSize) {
                DialogUtils.fixSize(BaseDialog.this);
            }
        });
    }

    public BaseDialog(Frame owner, String title, boolean modal, JButton defaultButton) {
        this(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS,
                defaultButton, false);
    }

    public BaseDialog(Frame owner, String title, boolean modal, JButton defaultButton, boolean fixSize) {
        this(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS,
                defaultButton, fixSize);
    }

    protected JButton getDefaultButton() {
        return defaultButton;
    }
}

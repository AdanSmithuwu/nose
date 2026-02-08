package com.comercialvalerio.presentation.ui.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * {@link InputVerifier} sencillo que marca los componentes con un borde de error
 * cuando la entrada es inválida. Los campos de texto se consideran inválidos si
 * están vacíos y las listas cuando no hay selección.
 */
public class RequiredVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent input) {
        boolean valid = true;
        if (input instanceof JTextComponent text) {
            valid = !text.getText().trim().isEmpty();
        } else if (input instanceof JComboBox<?> combo) {
            valid = combo.getSelectedItem() != null;
        }
        input.putClientProperty(FlatClientProperties.OUTLINE, valid ? null : "error");
        return valid;
    }
}

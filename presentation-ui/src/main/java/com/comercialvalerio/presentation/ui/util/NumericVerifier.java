package com.comercialvalerio.presentation.ui.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * {@link InputVerifier} que valida valores numéricos (enteros o decimales).
 * Muestra un borde de error cuando la entrada es inválida.
 */
public class NumericVerifier extends InputVerifier {
    private final char decimalSep =
            DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator();

    @Override
    public boolean verify(JComponent input) {
        boolean valid = true;
        if (input instanceof JTextComponent text) {
            String t = text.getText().trim();
            String regex = "\\d+(" + Pattern.quote(String.valueOf(decimalSep)) + "\\d+)?";
            if (t.matches(regex)) {
                try {
                    java.math.BigDecimal bd = new java.math.BigDecimal(t);
                    valid = bd.compareTo(java.math.BigDecimal.ZERO) >= 0;
                } catch (NumberFormatException ex) {
                    valid = false;
                }
            } else {
                valid = false;
            }
        }
        input.putClientProperty(FlatClientProperties.OUTLINE, valid ? null : "error");
        return valid;
    }
}

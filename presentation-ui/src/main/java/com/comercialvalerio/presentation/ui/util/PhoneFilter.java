package com.comercialvalerio.presentation.ui.util;

import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * {@link DocumentFilter} que permite dígitos y caracteres comunes de formato
 * telefónico como espacios, guiones, paréntesis, puntos y signo más.
 */
public class PhoneFilter extends DocumentFilter {

    private static final String ALLOWED = "0123456789 ()-.+";
    private final int maxLen;
    private final javax.swing.text.JTextComponent field;

    /**
     * Crea un filtro que aplica el límite de longitud indicado.
     *
     * @param maxLen número máximo de caracteres permitidos
     */
    public PhoneFilter(int maxLen) {
        this(maxLen, null);
    }

    /**
     * Crea un filtro que señala el truncamiento en el componente indicado.
     *
     * @param maxLen número máximo de caracteres permitidos
     * @param field  componente de texto a resaltar cuando se descartan caracteres;
     *               puede ser {@code null}
     */
    public PhoneFilter(int maxLen, javax.swing.text.JTextComponent field) {
        this.maxLen = maxLen;
        this.field = field;
    }

    private String filter(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ALLOWED.indexOf(c) >= 0) sb.append(c);
        }
        return sb.toString();
    }

    private String enforceLength(Document doc, String text, int removeLen) {
        if (maxLen < 0) return text;
        int allowed = Math.max(0, maxLen - (doc.getLength() - removeLen));
        return text.length() > allowed ? text.substring(0, allowed) : text;
    }

    private void updateOutline(boolean truncated) {
        if (field != null) {
            field.putClientProperty(com.formdev.flatlaf.FlatClientProperties.OUTLINE,
                    truncated ? "error" : null);
            if (truncated) {
                KeyUtils.setTooltipAndMnemonic(field, KeyEvent.VK_F5, "Caracteres descartados");
            } else {
                field.setToolTipText(null);
            }
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;
        String s = enforceLength(fb.getDocument(), filter(string), 0);
        updateOutline(!s.equals(string));
        if (!s.isEmpty()) super.insertString(fb, offset, s, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text != null) {
            String filtered = enforceLength(fb.getDocument(), filter(text), length);
            updateOutline(!filtered.equals(text));
            text = filtered;
        } else {
            updateOutline(false);
        }
        super.replace(fb, offset, length, text, attrs);
    }
}

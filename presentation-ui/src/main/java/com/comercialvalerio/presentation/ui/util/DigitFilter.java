package com.comercialvalerio.presentation.ui.util;

import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * {@link DocumentFilter} que solo permite ingresar dígitos en un componente
 * de texto. Opcionalmente limita la cantidad máxima de dígitos.
 */
public class DigitFilter extends DocumentFilter {

    private final int maxLen;
    private final javax.swing.text.JTextComponent field;

    /** Crea un filtro sin límite de longitud. */
    public DigitFilter() {
        this(-1, null);
    }

    /**
     * @param maxLen número máximo de dígitos permitidos, o {@code -1} sin límite
     */
    public DigitFilter(int maxLen) {
        this(maxLen, null);
    }

    /**
     * Crea un filtro que señala el truncamiento en el componente indicado.
     *
     * @param field componente de texto a resaltar cuando se descartan caracteres
     */
    public DigitFilter(javax.swing.text.JTextComponent field) {
        this(-1, field);
    }

    /**
     * @param maxLen número máximo de dígitos permitidos, o {@code -1} sin límite
     * @param field  componente de texto a resaltar cuando se descartan caracteres; puede ser {@code null}
     */
    public DigitFilter(int maxLen, javax.swing.text.JTextComponent field) {
        this.maxLen = maxLen;
        this.field = field;
    }

    private String filter(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) sb.append(c);
        }
        return sb.toString();
    }

    private String enforceLength(Document doc, String text, int removeLength) {
        if (maxLen < 0) return text;
        int current = doc.getLength() - removeLength;
        int allowed = Math.max(0, maxLen - current);
        if (text.length() > allowed) {
            return text.substring(0, allowed);
        }
        return text;
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
        Document doc = fb.getDocument();
        String filtered = enforceLength(doc, filter(string), 0);
        updateOutline(!filtered.equals(string));
        if (!filtered.isEmpty()) {
            super.insertString(fb, offset, filtered, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        Document doc = fb.getDocument();
        if (text != null) {
            String filtered = enforceLength(doc, filter(text), length);
            updateOutline(!filtered.equals(text));
            text = filtered;
        } else {
            updateOutline(false);
        }
        super.replace(fb, offset, length, text, attrs);
    }
}

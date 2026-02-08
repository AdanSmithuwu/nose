package com.comercialvalerio.presentation.ui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * {@link DocumentFilter} que restringe la longitud de entrada.
 */
public class LengthFilter extends DocumentFilter {

    private final int maxLen;

    /**
     * @param maxLen número máximo de caracteres permitidos
     */
    public LengthFilter(int maxLen) {
        this.maxLen = maxLen;
    }

    private String enforceLength(Document doc, String text, int removeLength) {
        int current = doc.getLength() - removeLength;
        int allowed = Math.max(0, maxLen - current);
        if (text.length() > allowed) {
            return text.substring(0, allowed);
        }
        return text;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;
        String s = enforceLength(fb.getDocument(), string, 0);
        if (!s.isEmpty()) {
            super.insertString(fb, offset, s, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text != null) {
            text = enforceLength(fb.getDocument(), text, length);
        }
        super.replace(fb, offset, length, text, attrs);
    }
}

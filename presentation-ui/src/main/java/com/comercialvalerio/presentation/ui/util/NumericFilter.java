package com.comercialvalerio.presentation.ui.util;

import java.awt.event.KeyEvent;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * {@link DocumentFilter} que permite solo dígitos y un único separador decimal.
 */
public class NumericFilter extends DocumentFilter {

    private final char decimalSep;
    private final javax.swing.text.JTextComponent field;
    private final int maxLen;

    /**
     * Crea un filtro usando el separador decimal de la configuración regional por defecto.
     */
    public NumericFilter() {
        this(DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator(), -1, null);
    }

    /**
     * @param decimalSep carácter usado como separador decimal
     */
    public NumericFilter(char decimalSep) {
        this(decimalSep, -1, null);
    }

    /**
     * Crea un filtro que señala el truncamiento en el componente indicado.
     *
     * @param field componente de texto a resaltar cuando se descartan caracteres
     */
    public NumericFilter(javax.swing.text.JTextComponent field) {
        this(DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator(), -1, field);
    }

    /**
     * @param decimalSep carácter usado como separador decimal
     * @param field      componente de texto a resaltar cuando se descartan caracteres; puede ser {@code null}
     */
    public NumericFilter(char decimalSep, javax.swing.text.JTextComponent field) {
        this(decimalSep, -1, field);
    }

    /**
     * @param maxLen   número máximo de caracteres permitidos o {@code -1} sin límite
     */
    public NumericFilter(int maxLen) {
        this(DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator(), maxLen, null);
    }

    /**
     * @param maxLen número máximo de caracteres permitidos o {@code -1} sin límite
     * @param field  componente de texto a resaltar cuando se descartan caracteres; puede ser {@code null}
     */
    public NumericFilter(int maxLen, javax.swing.text.JTextComponent field) {
        this(DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator(), maxLen, field);
    }

    /**
     * @param decimalSep carácter usado como separador decimal
     * @param maxLen     número máximo de caracteres permitidos o {@code -1} sin límite
     * @param field      componente de texto a resaltar cuando se descartan caracteres; puede ser {@code null}
     */
    public NumericFilter(char decimalSep, int maxLen, javax.swing.text.JTextComponent field) {
        this.decimalSep = decimalSep;
        this.field = field;
        this.maxLen = maxLen;
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

    private String filter(Document doc, int offset, int length, String text) throws BadLocationException {
        if (text == null || text.isEmpty()) return "";
        String docText = doc.getText(0, doc.getLength());
        String remaining = docText.substring(0, offset) + docText.substring(offset + length);
        boolean allowDecimal = remaining.indexOf(decimalSep) < 0;
        StringBuilder sb = new StringBuilder(text.length());
        boolean decimalUsed = !allowDecimal;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (c == decimalSep && !decimalUsed) {
                sb.append(c);
                decimalUsed = true;
            }
        }
        return sb.toString();
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
    public void insertString(FilterBypass fb, int offset, String string,
            AttributeSet attr) throws BadLocationException {
        String filtered = filter(fb.getDocument(), offset, 0, string);
        String s = enforceLength(fb.getDocument(), filtered, 0);
        updateOutline(!s.equals(string));
        if (!s.isEmpty()) {
            super.insertString(fb, offset, s, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        String filtered = filter(fb.getDocument(), offset, length, text);
        String s = enforceLength(fb.getDocument(), filtered, length);
        updateOutline(!s.equals(text));
        super.replace(fb, offset, length, s, attrs);
    }
}

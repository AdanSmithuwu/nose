package com.comercialvalerio.presentation.util;

import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import java.util.Set;

/**
 * Utilidad que evita que se ingresen emojis en cualquier
 * {@link JTextComponent}. Instala un oyente global de foco que aplica un
 * {@link DocumentFilter} a los componentes de texto cuando obtienen el foco.
 */
public final class EmojiBlocker {

    private EmojiBlocker() {}

    /** Instala el bloqueador de emojis global. */
    public static void installGlobal() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("permanentFocusOwner",
                        new FocusOwnerListener());
    }

    private static void install(JTextComponent tc) {
        Document doc = tc.getDocument();
        if (doc instanceof AbstractDocument ad) {
            if (!(ad.getDocumentFilter() instanceof NoEmojiFilter)) {
                ad.setDocumentFilter(new NoEmojiFilter());
            }
        }
    }

    private static class FocusOwnerListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object o = evt.getNewValue();
            if (o instanceof JTextComponent tc) {
                install(tc);
            }
        }
    }

    /** Filtro que elimina cualquier emoji del texto insertado. */
    private static class NoEmojiFilter extends DocumentFilter {
        private static final Set<Character.UnicodeBlock> EMOJI_BLOCKS = Set.of(
                Character.UnicodeBlock.EMOTICONS,
                Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS,
                Character.UnicodeBlock.SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS,
                Character.UnicodeBlock.SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A,
                Character.UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS,
                Character.UnicodeBlock.SYMBOLS_FOR_LEGACY_COMPUTING,
                Character.UnicodeBlock.DINGBATS,
                Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS);
        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a)
                throws BadLocationException {
            super.insertString(fb, offs, filter(str), a);
        }

        @Override
        public void replace(FilterBypass fb, int offs, int len, String str, AttributeSet a)
                throws BadLocationException {
            super.replace(fb, offs, len, filter(str), a);
        }

        private String filter(String text) {
            if (text == null || text.isEmpty()) return text;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length();) {
                int cp = text.codePointAt(i);
                if (!isEmoji(cp)) {
                    sb.appendCodePoint(cp);
                }
                i += Character.charCount(cp);
            }
            return sb.toString();
        }

        private boolean isEmoji(int cp) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
            return Character.charCount(cp) > 1 || EMOJI_BLOCKS.contains(block);
        }
    }
}

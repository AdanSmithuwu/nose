package com.comercialvalerio.presentation.ui.util;

import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Utilidades para trabajar con {@link javax.swing.text.DocumentListener}s.
 * Provee un listener con demora que ejecuta la acción usando un {@link Timer}.
 */
public final class DocumentListeners {

    private DocumentListeners() {}

    /**
     * Adjunta un listener al documento del componente que invoca la acción
     * indicada después del retraso especificado. El temporizador se reinicia
     * cada vez que cambia el documento, garantizando que la acción se ejecute
     * solo una vez tras un periodo sin cambios.
     *
     * @param field  componente de texto a observar
     * @param delay  retraso en milisegundos
     * @param action acción a ejecutar tras la espera
     */
    public static void attachDebounced(JTextComponent field, int delay, Runnable action) {
        Timer timer = new Timer(delay, e -> action.run());
        timer.setRepeats(false);
        DocumentListener listener = new DocumentListener() {
            private void restart() { timer.restart(); }
            @Override public void insertUpdate(DocumentEvent e) { restart(); }
            @Override public void removeUpdate(DocumentEvent e) { restart(); }
            @Override public void changedUpdate(DocumentEvent e) { restart(); }
        };
        field.getDocument().addDocumentListener(listener);
    }

    /** Sobrecarga de conveniencia que usa un retraso de 300&nbsp;ms. */
    public static void attachDebounced(JTextComponent field, Runnable action) {
        attachDebounced(field, 300, action);
    }
}

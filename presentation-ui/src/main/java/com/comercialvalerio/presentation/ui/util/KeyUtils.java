package com.comercialvalerio.presentation.ui.util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.StringJoiner;

/** Utilidades para registrar acciones de teclado. */
public final class KeyUtils {

    private KeyUtils() {}

    /** Tecla estándar usada para refrescar la interfaz. */
    public static final int REFRESH_KEY = KeyEvent.VK_F5;

    /**
     * Devuelve la combinación de teclas adecuada para mostrar el acelerador.
     * Si la tecla es una función (F1, F2, ...), no se aplican modificadores;
     * de lo contrario se usa la tecla Alt por defecto.
     */
    private static KeyStroke keyStrokeFor(int keyCode) {
        String keyText = KeyEvent.getKeyText(keyCode);
        int modifiers = keyText.startsWith("F") ? 0 : InputEvent.ALT_DOWN_MASK;
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }

    /**
     * Registra la tecla F5 en el componente indicado para ejecutar la acción de
     * recarga proporcionada.
     *
     * @param comp   componente cuyos mapas de entrada/acción se actualizan
     * @param action código ejecutado al presionar F5
     */
    public static void registerRefreshAction(JComponent comp, Runnable action) {
        registerKeyAction(comp, KeyStroke.getKeyStroke("F5"), action);
    }

    /**
     * Registra la tecla F3 en el componente indicado para enfocar el campo de
     * texto dado.
     *
     * @param comp  componente cuyos mapas de entrada/acción se actualizan
     * @param field campo de texto que recibirá el foco al presionar F3
     */
    public static void registerFocusAction(JComponent comp, JTextField field) {
        registerKeyAction(comp, KeyStroke.getKeyStroke("F3"),
                field::requestFocusInWindow);
    }

    /**
     * Registra la combinación indicada en el componente para ejecutar la acción
     * proporcionada al presionarla.
     *
     * @param comp   componente cuyos mapas de entrada/acción se actualizan
     * @param key    combinación de teclas a registrar
     * @param action código ejecutado al presionar la combinación
     */
    public static void registerKeyAction(JComponent comp, KeyStroke key, Runnable action) {
        InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = comp.getActionMap();
        String name = "action-" + key.toString();
        im.put(key, name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Aplica una tecla mnemónica y un tooltip a un botón. Si la tecla es una
     * {@code F}-tecla, el tooltip muestra solo la función; de lo contrario se
     * indica "Alt+<tecla>".
     *
     * @param button botón a modificar
     * @param key    código de tecla
     * @param text   texto base para el tooltip
     */
    public static void setTooltipAndMnemonic(AbstractButton button, int key, String text) {
        setTooltipAndMnemonic(button, keyStrokeFor(key), text);
    }

    /**
     * Aplica un tooltip a cualquier componente usando los modificadores de la
     * tecla indicada. Si el componente es un botón también se establece su
     * mnemónico.
     */
    public static void setTooltipAndMnemonic(JComponent comp, KeyStroke key, String text) {
        if (comp instanceof AbstractButton b) {
            String keyText = KeyEvent.getKeyText(key.getKeyCode());
            if (!keyText.startsWith("F")) {
                b.setMnemonic(key.getKeyCode());
            }
        }
        String mod = KeyEvent.getModifiersExText(key.getModifiers());
        String keyText = KeyEvent.getKeyText(key.getKeyCode());
        String suffix = mod.isEmpty()
                ? "(" + keyText + ")"
                : "(" + mod + "+" + keyText + ")";
        comp.setToolTipText(text + " " + suffix);
    }

    /** Sobrecarga de conveniencia usando solo el código de tecla. */
    public static void setTooltipAndMnemonic(JComponent comp, int key, String text) {
        setTooltipAndMnemonic(comp, keyStrokeFor(key), text);
    }

    /**
     * Aplica una tecla mnemónica y un tooltip con los modificadores de la
     * combinación dada.
     *
     * @param button botón a modificar
     * @param key    combinación que contiene código y modificadores
     * @param text   texto base del tooltip
     */
    public static void setTooltipAndMnemonic(AbstractButton button, KeyStroke key, String text) {
        setTooltipAndMnemonic((JComponent) button, key, text);
    }

    /**
     * Aplica una tecla mnemónica y un tooltip mostrando múltiples aceleradores.
     * El tooltip muestra cada acelerador separado con {@code " o "}. El mnemónico
     * se toma de la primera combinación.
     *
     * @param button botón a modificar
     * @param keys   lista de combinaciones con códigos y modificadores
     * @param text   texto base del tooltip
     */
    public static void setTooltipAndMnemonic(AbstractButton button, List<KeyStroke> keys, String text) {
        if (keys == null || keys.isEmpty()) {
            button.setToolTipText(text);
            return;
        }
        button.setMnemonic(keys.get(0).getKeyCode());
        StringJoiner joiner = new StringJoiner(" or ");
        for (KeyStroke key : keys) {
            String mod = KeyEvent.getModifiersExText(key.getModifiers());
            String keyText = KeyEvent.getKeyText(key.getKeyCode());
            joiner.add(mod.isEmpty() ? keyText : mod + "+" + keyText);
        }
        button.setToolTipText(text + " (" + joiner + ")");
    }

    /**
     * Sobrecarga que permite opciones de acelerador variables. El tooltip se
     * muestra como {@code "texto (Alt+X o Ctrl+Y)"}.
     *
     * @param button     botón a modificar
     * @param text       texto base del tooltip
     * @param primary    combinación principal
     * @param alternates combinaciones adicionales
     */
    public static void setTooltipAndMnemonic(AbstractButton button, String text,
                                             KeyStroke primary,
                                             KeyStroke... alternates) {
        List<KeyStroke> keys = new java.util.ArrayList<>(1 + alternates.length);
        keys.add(primary);
        if (alternates != null) {
            java.util.Collections.addAll(keys, alternates);
        }
        setTooltipAndMnemonic(button, keys, text);
    }
}

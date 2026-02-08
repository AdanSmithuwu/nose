package com.comercialvalerio.presentation.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/** Utilidad para alternar la pantalla completa nativa con F11. */
public final class FullScreenUtils {

    private static final String STATE_KEY = "fullScreenState";

    private FullScreenUtils() {}

    private static State getState(JFrame frame) {
        Object val = frame.getRootPane().getClientProperty(STATE_KEY);
        if (val instanceof State state) {
            return state;
        }
        State state = new State();
        frame.getRootPane().putClientProperty(STATE_KEY, state);
        return state;
    }

    /** Guarda los límites de la ventana y el estado de pantalla completa. */
    private static class State {
        boolean fullScreen;
        Rectangle windowedBounds;
        int windowedState;
    }

    /** Registra F11 en el root pane del frame para alternar pantalla completa. */
    public static void registerFullScreenShortcut(JFrame frame) {
        KeyUtils.registerKeyAction(frame.getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
                () -> toggleFullScreen(frame));
    }

    /** Indica si el frame está en modo de pantalla completa. */
    public static boolean isFullScreen(JFrame frame) {
        return getState(frame).fullScreen;
    }

    /** Devuelve los límites de la ventana guardados antes de la pantalla completa. */
    public static Rectangle getWindowedBounds(JFrame frame) {
        return getState(frame).windowedBounds;
    }

    /** Alterna la pantalla completa nativa en el dispositivo gráfico actual. */
    public static void toggleFullScreen(JFrame frame) {
        State state = getState(frame);
        GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
        if (state.fullScreen) {
            device.setFullScreenWindow(null);
            frame.setExtendedState(state.windowedState);
            if (state.windowedState == Frame.NORMAL && state.windowedBounds != null) {
                frame.setBounds(state.windowedBounds);
            }
        } else {
            state.windowedState = frame.getExtendedState();
            state.windowedBounds = frame.getBounds();
            device.setFullScreenWindow(frame);
        }
        state.fullScreen = !state.fullScreen;
    }
}

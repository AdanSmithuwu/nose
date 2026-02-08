package com.comercialvalerio.presentation.ui.util;

import java.util.prefs.Preferences;
import java.awt.Rectangle;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Utilidad para almacenar preferencias de usuario en {@link java.util.prefs.Preferences}. */
public final class UserPrefs {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(UserPrefs.class);
    private static final Logger LOG =
            Logger.getLogger(UserPrefs.class.getName());

    private static final String KEY_DARK = "darkMode";
    private static final String KEY_BOUNDS = "windowBounds";
    private static final String KEY_PDF_DIR = "pdfDirectory";
    private static final String KEY_FULL_SCREEN = "fullScreen";
    private static final String KEY_ACCENT_COLOR = "accentColor";

    private UserPrefs() {
    }

    private static String encodeBounds(Rectangle r) {
        return r.x + "," + r.y + "," + r.width + "," + r.height;
    }

    private static Rectangle decodeBounds(String val) {
        if (val == null) {
            return null;
        }
        String[] p = val.split(",");
        if (p.length != 4) {
            return null;
        }
        try {
            int x = Integer.parseInt(p[0]);
            int y = Integer.parseInt(p[1]);
            int w = Integer.parseInt(p[2]);
            int h = Integer.parseInt(p[3]);
            return new Rectangle(x, y, w, h);
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Invalid window bounds", ex);
            return null;
        }
    }

    /** Indica si el modo oscuro está habilitado. */
    public static boolean isDarkMode() {
        return PREFS.getBoolean(KEY_DARK, true);
    }

    /** Establece si el modo oscuro está habilitado. */
    public static void setDarkMode(boolean dark) {
        PREFS.putBoolean(KEY_DARK, dark);
    }

    /** Indica si la aplicación debe iniciar en pantalla completa. */
    public static boolean isFullScreen() {
        return PREFS.getBoolean(KEY_FULL_SCREEN, false);
    }

    /** Establece si la aplicación se cerró por última vez en pantalla completa. */
    public static void setFullScreen(boolean full) {
        PREFS.putBoolean(KEY_FULL_SCREEN, full);
    }

    /** Devuelve los límites de la ventana guardados o {@code null} si no existen. */
    public static Rectangle getWindowBounds() {
        String val = PREFS.get(KEY_BOUNDS, null);
        return decodeBounds(val);
    }

    /** Guarda los límites de la ventana principal. */
    public static void setWindowBounds(Rectangle r) {
        if (r == null) {
            PREFS.remove(KEY_BOUNDS);
        } else {
            PREFS.put(KEY_BOUNDS, encodeBounds(r));
        }
    }

    /** Devuelve el directorio preferido para guardar PDFs o {@code null}. */
    public static java.io.File getPdfDirectory() {
        String val = PREFS.get(KEY_PDF_DIR, null);
        return val != null ? new java.io.File(val) : null;
    }

    /** Establece el directorio preferido para guardar PDFs. */
    public static void setPdfDirectory(java.io.File dir) {
        if (dir == null) {
            PREFS.remove(KEY_PDF_DIR);
        } else {
            PREFS.put(KEY_PDF_DIR, dir.getAbsolutePath());
        }
    }

    /** Devuelve el color de acento guardado o {@code null} si no se definió. */
    public static Color getAccentColor() {
        String val = PREFS.get(KEY_ACCENT_COLOR, null);
        if (val == null) {
            return null;
        }
        try {
            return Color.decode(val);
        } catch (NumberFormatException ex) {
            LOG.log(Level.WARNING, "Invalid accent color", ex);
            return null;
        }
    }

    /** Guarda el color de acento elegido como cadena hexadecimal. */
    public static void setAccentColor(Color color) {
        if (color == null) {
            PREFS.remove(KEY_ACCENT_COLOR);
        } else {
            PREFS.put(KEY_ACCENT_COLOR, toHexString(color));
        }
    }

    private static String toHexString(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}

package com.comercialvalerio.presentation.ui.theme;

import java.awt.Color;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatLaf;

/**
 * Constantes de estilo centralizadas utilizadas en los componentes Swing.
 * Tener un solo lugar para colores y radios facilita ajustes futuros
 * y mantiene la interfaz consistente.
 */
public final class UIStyle {
    private UIStyle() {}

    // Colores comunes en formato hexadecimal
    public static final String HEX_WHITE = "#FFFFFF";
    public static final String HEX_BORDER = "#D0D7E2";
    public static final String HEX_LIGHT_BLUE = "#E6F0FF";
    public static final String HEX_PRIMARY = "#8F6ADE";
    public static final String HEX_WARNING = "#F47B20";
    public static final String HEX_SUCCESS = "#33A342";
    public static final String HEX_DANGER = "#DC3545";
    /** Amarillo brillante usado en la acción "Nuevo Pedido". */
    public static final String HEX_ACTION_YELLOW = "#FFD255";
    /** Verde brillante usado en la acción "Nueva Venta". */
    public static final String HEX_ACTION_GREEN = "#34D567";
    public static final String HEX_GRAY = "#B0C4D4";
    public static final String HEX_DARK_TEXT = "#0D2E5F";
    public static final String HEX_ERROR_TEXT = "#ff3b30";
    public static final String HEX_DISABLED = "#969696";
    /** Fondo claro usado en los paneles de métricas del dashboard. */
    public static final String HEX_CARD_BG = "#F3F6FB";
    /** Color de fondo para tarjetas en modo oscuro. */
    public static final String HEX_CARD_BG_DARK = "#2B2B2B";
    /** Color de fondo principal de los formularios. */
    public static final String HEX_FORM_BG = "#EFEFEF";
    /** Color de fondo de formularios en modo oscuro. */
    public static final String HEX_FORM_BG_DARK = "#2B2B2B";
    /** Color de texto secundario. */
    public static final String HEX_SECONDARY_TEXT = "#464646";
    /** Color de texto secundario en modo oscuro. */
    public static final String HEX_SECONDARY_TEXT_DARK = "#CCCCCC";
    /** Fondo claro de panel usado en dashboard y reportes. */
    public static final String HEX_PANEL_BG = "#F3F6FB";
    /** Color de fondo de panel para el modo oscuro. */
    public static final String HEX_PANEL_BG_DARK = "#2B2B2B";
    /** Gris oscuro para encabezados grandes. */
    public static final String HEX_DARK_GRAY = "#464646";
    /** Color de encabezado cuando el modo oscuro está activo. */
    public static final String HEX_DARK_TEXT_DARK = "#E5E5E5";

    // Colores como objetos Color
    public static final Color COLOR_WHITE = Color.decode(HEX_WHITE);
    public static final Color COLOR_BORDER = Color.decode(HEX_BORDER);
    public static final Color COLOR_LIGHT_BLUE = Color.decode(HEX_LIGHT_BLUE);
    public static final Color COLOR_PRIMARY = Color.decode(HEX_PRIMARY);
    public static final Color COLOR_WARNING = Color.decode(HEX_WARNING);
    public static final Color COLOR_SUCCESS = Color.decode(HEX_SUCCESS);
    public static final Color COLOR_DANGER = Color.decode(HEX_DANGER);
    public static final Color COLOR_ACTION_YELLOW = Color.decode(HEX_ACTION_YELLOW);
    public static final Color COLOR_ACTION_GREEN = Color.decode(HEX_ACTION_GREEN);
    public static final Color COLOR_GRAY = Color.decode(HEX_GRAY);
    public static final Color COLOR_DARK_TEXT = Color.decode(HEX_DARK_TEXT);
    public static final Color COLOR_ERROR_TEXT = Color.decode(HEX_ERROR_TEXT);
    public static final Color COLOR_DISABLED = Color.decode(HEX_DISABLED);
    public static final Color COLOR_CARD_BG = Color.decode(HEX_CARD_BG);
    public static final Color COLOR_CARD_BG_DARK = Color.decode(HEX_CARD_BG_DARK);
    public static final Color COLOR_SECONDARY_TEXT = Color.decode(HEX_SECONDARY_TEXT);
    public static final Color COLOR_SECONDARY_TEXT_DARK = Color.decode(HEX_SECONDARY_TEXT_DARK);
    public static final Color COLOR_PANEL_BG = Color.decode(HEX_PANEL_BG);
    public static final Color COLOR_PANEL_BG_DARK = Color.decode(HEX_PANEL_BG_DARK);
    public static final Color COLOR_FORM_BG = Color.decode(HEX_FORM_BG);
    public static final Color COLOR_FORM_BG_DARK = Color.decode(HEX_FORM_BG_DARK);
    public static final Color COLOR_DARK_GRAY = Color.decode(HEX_DARK_GRAY);
    public static final Color COLOR_DARK_TEXT_DARK = Color.decode(HEX_DARK_TEXT_DARK);

    /* ------------------------------------------------------------------ */
    /*  T H E M E   A W A R E   G E T T E R S                            */
    /* ------------------------------------------------------------------ */

    public static String getHexCardBg() {
        return FlatLaf.isLafDark() ? HEX_CARD_BG_DARK : HEX_CARD_BG;
    }

    public static Color getColorCardBg() {
        return FlatLaf.isLafDark() ? COLOR_CARD_BG_DARK : COLOR_CARD_BG;
    }

    public static String getHexPanelBg() {
        return FlatLaf.isLafDark() ? HEX_PANEL_BG_DARK : HEX_PANEL_BG;
    }

    public static Color getColorPanelBg() {
        return FlatLaf.isLafDark() ? COLOR_PANEL_BG_DARK : COLOR_PANEL_BG;
    }

    public static String getHexFormBg() {
        return FlatLaf.isLafDark() ? HEX_FORM_BG_DARK : HEX_FORM_BG;
    }

    public static Color getColorFormBg() {
        return FlatLaf.isLafDark() ? COLOR_FORM_BG_DARK : COLOR_FORM_BG;
    }

    public static String getHexDarkText() {
        return FlatLaf.isLafDark() ? HEX_DARK_TEXT_DARK : HEX_DARK_TEXT;
    }

    public static Color getColorDarkText() {
        return FlatLaf.isLafDark() ? COLOR_DARK_TEXT_DARK : COLOR_DARK_TEXT;
    }

    public static String getHexSecondaryText() {
        return FlatLaf.isLafDark() ? HEX_SECONDARY_TEXT_DARK : HEX_SECONDARY_TEXT;
    }

    public static Color getColorSecondaryText() {
        return FlatLaf.isLafDark() ? COLOR_SECONDARY_TEXT_DARK : COLOR_SECONDARY_TEXT;
    }

    /** Texto genérico para tablas vacías. */
    public static final String TXT_NO_DATA = "Sin datos para mostrar";

    /** Color de borde violeta estándar usado en muchos formularios. */
    public static final Color COLOR_BORDER_VIOLET = new Color(150, 150, 250);
    /** Color de borde gris neutro para ventas y pedidos. */
    public static final Color COLOR_BORDER_GRAY = new Color(120, 120, 120);

    /** Instancias de LineBorder para los colores anteriores. */
    public static final Border BORDER_VIOLET_1 = new LineBorder(COLOR_BORDER_VIOLET, 1, true);
    public static final Border BORDER_GRAY_1 = new LineBorder(COLOR_BORDER_GRAY, 1, true);
    /** Margen exterior estándar para la mayoría de formularios. */
    public static final Border FORM_MARGIN = new EmptyBorder(10,40,20,40);
    /** Borde violeta con margen estándar. */
    public static final Border FORM_BORDER_VIOLET = FORM_MARGIN;
    /** Borde gris con margen estándar. */
    public static final Border FORM_BORDER_GRAY = FORM_MARGIN;

    // Constantes de diseño comunes
    /** Relleno estándar para la mayoría de formularios (arriba, izquierda, abajo, derecha). */
    public static final String FORM_INSETS = "24 32 32 32";
    /** Espacio predeterminado entre componentes dentro de formularios. */
    public static final String FORM_GAP = "18";
    /** Espacio usado en paneles de listas como la gestión de productos. */
    public static final String PANEL_GAP = "10";
    /**
     * Separación estándar entre etiquetas y sus combobox asociados.
     * Se utiliza en formularios con filtros para mantener una distancia
     * coherente entre el texto y el selector.
     */
    public static final String FILTER_GAP = "10";
    /** Ancho mínimo predeterminado para campos de búsqueda. */
    public static final int SEARCH_FIELD_MIN_WIDTH = 200;
    /** Altura estándar de fila usada en toda la interfaz. */
    public static final int TABLE_ROW_HEIGHT = 28;
    /** Altura estándar para todos los ComboBox. */
    public static final int COMBO_HEIGHT = 40;
    /** Ancho estándar para los campos de fecha. */
    public static final int DATE_FIELD_WIDTH = 130;
    /** Altura estándar para los campos de fecha. */
    public static final int DATE_FIELD_HEIGHT = 30;
    /** Restricciones MigLayout para usar en selectores de fecha. */
    public static final String DATE_FIELD_CONSTRAINTS =
            "w " + DATE_FIELD_WIDTH + "!, h " + DATE_FIELD_HEIGHT + "!";
    // Valores RGB comunes para estilos de botones
    public static final int RGB_ACTION_GREEN  = 0x42A042;
    public static final int RGB_ACTION_PURPLE = 0x7259C4;
    public static final int RGB_ACTION_BLUE   = 0x006DCC;
    /** Azul claro para acciones secundarias. */
    public static final int RGB_ACTION_BLUE_LIGHT = 0x2980B9;
    public static final int RGB_ACTION_RED    = 0xE74C3C;

    // Tamaños de arco
    public static final int ARC_SMALL = 6;
    public static final int ARC_DEFAULT = 8;
    public static final int ARC_DIALOG = 10;
    public static final int ARC_PILL = 20;
    public static final int ARC_ROUND = 999;
}

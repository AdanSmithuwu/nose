package com.comercialvalerio.presentation.ui.main.dependencies;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.KeyStroke;

/**
 * Mapea cada {@link MenuOption} a un atajo de teclado.
 */
public final class MenuKeyMap {

    private static final Map<MenuOption, KeyStroke> MAP = new EnumMap<>(MenuOption.class);

    static {
        MAP.put(MenuOption.DASHBOARD, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.CLIENTES, KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.EMPLEADOS, KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.CATEGORIAS, KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.PEDIDO_ESPECIAL, KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.SEGUIMIENTO_VENTAS, KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.SEGUIMIENTO_PEDIDOS, KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.PRODUCTOS, KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.INVENTARIO, KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.HISTORIAL_INVENTARIO, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.REPORTE_DIARIO, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.REPORTE_MENSUAL, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.REPORTE_ROTACION, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.BITACORA_ACCESOS, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.PARAMETROS, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.MANTENIMIENTO, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        MAP.put(MenuOption.LOGOUT, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
    }

    private MenuKeyMap() {}

    /**
     * Devuelve el atajo asociado con la opción de menú indicada.
     */
    public static KeyStroke get(MenuOption option) {
        return MAP.get(option);
    }

    /**
     * Expone el mapa interno para iteración.
     */
    public static Map<MenuOption, KeyStroke> mapping() {
        return MAP;
    }
}

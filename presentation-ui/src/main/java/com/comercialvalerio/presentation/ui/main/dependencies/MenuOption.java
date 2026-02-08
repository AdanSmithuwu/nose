package com.comercialvalerio.presentation.ui.main.dependencies;

/**
 * Enumeración de opciones de menú que coincide con su orden de índice.
 */
public enum MenuOption {
    DASHBOARD(0),
    CLIENTES(1),
    EMPLEADOS(2),
    CATEGORIAS(3),
    PEDIDO_ESPECIAL(4),
    SEGUIMIENTO_VENTAS(5),
    SEGUIMIENTO_PEDIDOS(6),
    PRODUCTOS(7),
    INVENTARIO(8),
    HISTORIAL_INVENTARIO(9),
    REPORTE_DIARIO(10),
    REPORTE_MENSUAL(11),
    REPORTE_ROTACION(12),
    BITACORA_ACCESOS(13),
    PARAMETROS(14),
    MANTENIMIENTO(15),
    LOGOUT(16);

    private final int index;

    MenuOption(int index) {
        this.index = index;
    }

    /**
     * Devuelve el índice numérico asociado a esta opción.
     */
    public int index() {
        return index;
    }

    /**
     * Resuelve una opción de menú por su índice numérico.
     *
     * @param index índice usado por el componente de menú
     * @return la {@code MenuOption} correspondiente
     * @throws IllegalArgumentException si el índice no es válido
     */
    public static MenuOption fromIndex(int index) {
        for (MenuOption opt : values()) {
            if (opt.index == index) {
                return opt;
            }
        }
        throw new IllegalArgumentException("Invalid menu index: " + index);
    }
}

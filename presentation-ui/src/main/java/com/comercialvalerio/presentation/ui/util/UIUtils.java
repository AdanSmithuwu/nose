package com.comercialvalerio.presentation.ui.util;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.JButton;

/** Utilidades de la interfaz de usuario. */
public final class UIUtils {
    private UIUtils() {}

    /** Ícono usado en los botones de recarga. */
    private static final FlatSVGIcon REFRESH_ICON = new FlatSVGIcon(
            "com/comercialvalerio/presentation/ui/icon/svg/refresh.svg", 24, 24);

    /**
     * Crea un botón de recarga con el estilo predeterminado y ejecuta la acción
     * proporcionada al presionarlo.
     *
     * @param action código a ejecutar cuando se presiona el botón
     * @return botón configurado
     */
    public static JButton createRefreshButton(Runnable action) {
        JButton btn = new JButton();
        btn.setIcon(REFRESH_ICON);
        KeyUtils.setTooltipAndMnemonic(btn, KeyUtils.REFRESH_KEY, "Actualizar");
        btn.putClientProperty(FlatClientProperties.STYLE,
                "background:null; hoverBackground:" + UIStyle.HEX_LIGHT_BLUE);
        btn.addActionListener(e -> action.run());
        return btn;
    }
}

package com.comercialvalerio.presentation.ui.util;

import com.comercialvalerio.presentation.ui.theme.UIStyle;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.JButton;

/** Utilidades para aplicar estilos consistentes a los botones. */
public final class ButtonStyles {
    private ButtonStyles() {}

    /** Estiliza un botón de encabezado usando los colores verde o azul. */
    public static void styleHeader(JButton b, int rgb) {
        b.putClientProperty(FlatClientProperties.STYLE,
            "arc:" + UIStyle.ARC_DIALOG + "; " +
            "background:rgb(" + ((rgb >> 16) & 0xFF) + ',' + ((rgb >> 8) & 0xFF) + ',' + (rgb & 0xFF) + "); " +
            "foreground:rgb(255,255,255); font:+1; minimumWidth:160; minimumHeight:36");
        b.setIcon(new FlatSVGIcon("com/comercialvalerio/presentation/ui/icon/svg/plus.svg", 0.8f));
        b.setIconTextGap(UIScale.scale(4));
    }

    /** Estiliza un botón de acción estándar con el color e ícono indicados. */
    public static void styleAction(JButton b, int rgb, String svg) {
        b.putClientProperty(FlatClientProperties.STYLE,
            "arc:" + UIStyle.ARC_DIALOG + "; " +
            "background:rgb(" + ((rgb >> 16) & 0xFF) + ',' + ((rgb >> 8) & 0xFF) + ',' + (rgb & 0xFF) + "); " +
            "foreground:rgb(255,255,255); minimumWidth:140; minimumHeight:36; font:+1");
        if (svg != null) {
            b.setIcon(new FlatSVGIcon(svg, 0.8f));
        }
        b.setIconTextGap(UIScale.scale(4));
    }

    public static void styleBottom(JButton b, int rgb, String svg) {
        styleAction(b, rgb, svg);
    }
}

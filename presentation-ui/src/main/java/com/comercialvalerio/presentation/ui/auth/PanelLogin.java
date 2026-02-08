package com.comercialvalerio.presentation.ui.auth;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/*
 * Contenedor transparente con MigLayout; el degradado lo pinta su padre.
 */
public class PanelLogin extends JPanel {

    public PanelLogin() {
        setOpaque(false);
        setLayout(new MigLayout(
                "fillx, wrap, gap 10",          // columnas implícitas = 1
                "[fill]",                       // una sola columna
                "[]"));                         // filas dinámicas
    }
}

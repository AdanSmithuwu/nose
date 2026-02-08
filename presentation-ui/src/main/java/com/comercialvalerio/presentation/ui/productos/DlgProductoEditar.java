package com.comercialvalerio.presentation.ui.productos;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import com.comercialvalerio.presentation.ui.util.KeyUtils;

/** Diálogo para editar un producto existente. Reutiliza los campos de {@link DlgProductoNuevo}. */
public class DlgProductoEditar extends DlgProductoNuevo {

    public DlgProductoEditar(JFrame owner) {
        super(owner, true);
        setTitle("Editar Producto");
        getHeader().getTitleLabel().setText("Edición de Producto");
        getBtnGuardar().setText("Guardar");
        KeyUtils.setTooltipAndMnemonic(getBtnGuardar(), KeyEvent.VK_G, "Guardar");
        getCboTipo().setEnabled(false);
        pack();
    }
}

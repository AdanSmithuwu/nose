package com.comercialvalerio.presentation.ui.clientes;

import javax.swing.JFrame;

/** Diálogo para editar un cliente existente. Reutiliza los campos de
 *  {@link DlgClienteNuevo}. */
public class DlgClienteEditar extends DlgClienteNuevo {

    public DlgClienteEditar(JFrame owner) {
        super(owner);
        setTitle("Editar Cliente");
        getBtnGuardar().setText("Guardar");
        getTxtDni().setEditable(false);
    }
}

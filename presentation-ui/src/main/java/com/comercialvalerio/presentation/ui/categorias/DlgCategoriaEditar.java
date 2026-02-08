package com.comercialvalerio.presentation.ui.categorias;

import javax.swing.JFrame;

/** Diálogo para editar una categoría existente. Reutiliza los campos de
 *  {@link DlgCategoriaNueva}. */
public class DlgCategoriaEditar extends DlgCategoriaNueva {

    public DlgCategoriaEditar(JFrame owner) {
        super(owner);
        setTitle("Editar Categoría");
        getBtnGuardar().setText("Guardar");
    }
}

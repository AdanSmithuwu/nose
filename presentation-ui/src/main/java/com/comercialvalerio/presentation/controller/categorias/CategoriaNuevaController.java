package com.comercialvalerio.presentation.controller.categorias;

import javax.swing.JOptionPane;

import com.comercialvalerio.application.dto.CategoriaCreateDto;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.categorias.DlgCategoriaNueva;

/** Controlador para {@link DlgCategoriaNueva}. */
public class CategoriaNuevaController {

    private final DlgCategoriaNueva view;

    public CategoriaNuevaController(DlgCategoriaNueva view) {
        this.view = view;
    }

    /** Valida la entrada y crea la categoría. */
    public void registrar() {
        String nombre = view.getTxtNombre().getText().trim();
        String desc   = view.getTxtDescripcion().getText().trim();
        if (nombre.isBlank()) {
            JOptionPane.showMessageDialog(view,
                    "Ingrese el nombre de la categoría",
                    "Datos incompletos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CategoriaCreateDto dto = new CategoriaCreateDto(nombre,
                desc.isEmpty() ? null : desc);
        AsyncTasks.busy(view, () -> {
            UiContext.categoriaSvc().crear(dto);
            return null;
        }, v -> {
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Categoría registrada");
            view.dispose();
        });
    }
}

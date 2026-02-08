package com.comercialvalerio.presentation.controller.common;

import com.comercialvalerio.presentation.ui.common.DlgObservacion;
import com.comercialvalerio.presentation.ui.util.DialogUtils;

/** Controlador para {@link DlgObservacion}. */
public class ObservacionController {

    private final DlgObservacion view;
    private String observacion;

    public ObservacionController(DlgObservacion view) {
        this.view = view;
    }

    /** Guarda el texto y cierra el diálogo. */
    public void guardar() {
        observacion = view.getTxtObs().getText().trim();
        view.dispose();
    }

    /** Confirma descartar cambios y luego cierra. */
    public void cancelar() {
        if (view.getTxtObs().getDocument().getLength() > 0) {
            if (!DialogUtils.confirmAction(view, "¿Descartar cambios?")) {
                return;
            }
        }
        view.dispose();
    }

    public String getObservacion() {
        return observacion;
    }
}

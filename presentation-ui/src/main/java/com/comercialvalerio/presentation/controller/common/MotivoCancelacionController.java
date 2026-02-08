package com.comercialvalerio.presentation.controller.common;

import javax.swing.JOptionPane;

import com.comercialvalerio.presentation.ui.common.DlgMotivoCancelacion;
import com.comercialvalerio.presentation.ui.util.DialogUtils;

/** Controlador para {@link DlgMotivoCancelacion}. */
public class MotivoCancelacionController {

    private final DlgMotivoCancelacion view;
    private String motivo;

    public MotivoCancelacionController(DlgMotivoCancelacion view) {
        this.view = view;
    }

    /** Valida el texto y cierra el diálogo si es correcto. */
    public void guardar() {
        String tmp = view.getTxtMotivo().getText().trim();
        if (tmp.isBlank()) {
            JOptionPane.showMessageDialog(view,
                    "Ingrese el motivo de cancelación",
                    "Dato requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        motivo = tmp;
        view.dispose();
    }

    /** Confirma descartar la edición y luego cierra. */
    public void cancelar() {
        if (view.getTxtMotivo().getDocument().getLength() > 0) {
            if (!DialogUtils.confirmAction(view, "¿Descartar cambios?")) {
                return;
            }
        }
        view.dispose();
    }

    public String getMotivo() {
        return motivo;
    }
}

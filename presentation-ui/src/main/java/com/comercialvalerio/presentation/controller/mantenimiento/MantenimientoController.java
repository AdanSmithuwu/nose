package com.comercialvalerio.presentation.controller.mantenimiento;

import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.mantenimiento.FormMantenimiento;
import com.comercialvalerio.presentation.ui.util.DialogUtils;
import com.comercialvalerio.application.dto.RolNombre;
import java.util.logging.Logger;
import raven.toast.Notifications;

/**
 * Controlador para las acciones de {@link FormMantenimiento}.
 */
public class MantenimientoController {

    private final FormMantenimiento view;
    private static final Logger LOG = Logger.getLogger(MantenimientoController.class.getName());

    public MantenimientoController(FormMantenimiento view) {
        this.view = view;
    }

    /** Devuelve {@code true} si el usuario conectado es administrador. */
    private boolean isAdmin() {
        return UiContext.getUsuarioActual() != null
                && RolNombre.fromNombre(UiContext.getUsuarioActual().rolNombre()) == RolNombre.ADMINISTRADOR;
    }

    /** Recalcula el stock global tras confirmación del usuario. */
    public void recalcularStock() {
        if (!isAdmin()) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Sólo un administrador puede recalcular el stock",
                    "Permiso denegado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DialogUtils.confirmAction(view, "¿Recalcular el stock global?")) return;
        AsyncTasks.busy(view, () -> {
            UiContext.productoSvc().recalcularStockGlobal();
            return null;
        }, v -> Notifications.getInstance()
                .show(Notifications.Type.SUCCESS, "Stock recalculado"));
    }

    /** Depura entradas de inicio de sesión antiguas tras confirmación. */
    public void depurarBitacora() {
        if (!isAdmin()) {
            javax.swing.JOptionPane.showMessageDialog(view,
                    "Sólo un administrador puede depurar la bitácora",
                    "Permiso denegado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!DialogUtils.confirmAction(view, "¿Depurar registros antiguos?")) return;
        AsyncTasks.busy(view, () -> {
            UiContext.bitacoraLoginSvc().depurarAntiguos();
            return null;
        }, v -> Notifications.getInstance()
                .show(Notifications.Type.SUCCESS, "Bitácora depurada"));
    }
}

package com.comercialvalerio.presentation.controller.clientes;

import javax.swing.JOptionPane;

import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.clientes.DlgClienteNuevo;

/** Controlador para {@link DlgClienteNuevo}. */
public class ClienteNuevoController {

    private final DlgClienteNuevo view;

    public ClienteNuevoController(DlgClienteNuevo view) {
        this.view = view;
    }

    /** Valida la entrada y registra el cliente mediante el servicio REST. */
    public void registrar() {
        String nombre    = view.getTxtNombre().getText().trim();
        String apellidos = view.getTxtApellidos().getText().trim();
        String dni       = view.getTxtDni().getText().trim();
        String telefono  = PhoneUtils.stripToDigits(view.getTxtTelefono().getText().trim());
        String direccion = view.getTxtDireccion().getText().trim();

        boolean ok = true;
        if (!view.getTxtNombre().getInputVerifier().verify(view.getTxtNombre())) ok = false;
        if (!view.getTxtApellidos().getInputVerifier().verify(view.getTxtApellidos())) ok = false;
        if (!view.getTxtDni().getInputVerifier().verify(view.getTxtDni())) ok = false;
        if (!view.getTxtDireccion().getInputVerifier().verify(view.getTxtDireccion())) ok = false;
        if (!ok) {
            JOptionPane.showMessageDialog(view,
                    "Ingrese nombre, apellidos, DNI y dirección",
                    "Datos incompletos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ClienteCreateDto dto = new ClienteCreateDto(
                nombre, apellidos, dni, telefono,
                direccion);
        AsyncTasks.busy(view, () -> {
            UiContext.clienteSvc().registrar(dto);
            return null;
        }, v -> {
            raven.toast.Notifications.getInstance()
                    .show(raven.toast.Notifications.Type.SUCCESS,
                            "Cliente registrado");
            view.dispose();
        });
    }
}

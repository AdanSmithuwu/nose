package com.comercialvalerio.presentation.controller.empleados;

import javax.swing.JOptionPane;

import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.common.PhoneUtils;
import com.comercialvalerio.application.dto.RolNombre;
import com.comercialvalerio.presentation.core.AsyncTasks;
import com.comercialvalerio.presentation.core.UiContext;
import com.comercialvalerio.presentation.ui.empleados.DlgCredencialesGeneradas;
import com.comercialvalerio.presentation.ui.empleados.DlgEmpleadoNuevo;

/** Controlador para {@link DlgEmpleadoNuevo}. */
public class EmpleadoNuevoController {

    private final DlgEmpleadoNuevo view;

    public EmpleadoNuevoController(DlgEmpleadoNuevo view) {
        this.view = view;
    }

    /** Valida la entrada y registra al empleado. */
    public void registrar() {
        String nombres   = view.getTxtNombres().getText().trim();
        String apellidos = view.getTxtApellidos().getText().trim();
        String dni       = view.getTxtDni().getText().trim();
        String telefono  = PhoneUtils.stripToDigits(view.getTxtTelefono().getText().trim());
        String clave     = new String(view.getTxtClave().getPassword()).trim();

        boolean ok = true;
        if (!view.getTxtNombres().getInputVerifier().verify(view.getTxtNombres())) ok = false;
        if (!view.getTxtApellidos().getInputVerifier().verify(view.getTxtApellidos())) ok = false;
        if (!view.getTxtDni().getInputVerifier().verify(view.getTxtDni())) ok = false;
        if (!ok) {
            JOptionPane.showMessageDialog(view,
                    "Ingrese nombre, apellidos y DNI",
                    "Datos incompletos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AsyncTasks.busy(view, () -> {
            var rol = UiContext.rolSvc().buscarPorNombre(RolNombre.EMPLEADO.getNombre());
            EmpleadoCreateDto dto = new EmpleadoCreateDto(
                    nombres, apellidos, dni, telefono,
                    rol.idRol(),
                    clave.isBlank() ? null : clave
            );
            return UiContext.empleadoSvc().crear(dto);
        }, creado -> {
            new DlgCredencialesGeneradas(view, creado.usuario(),
                    creado.plainPassword()).setVisible(true);
            view.dispose();
        });
    }
}

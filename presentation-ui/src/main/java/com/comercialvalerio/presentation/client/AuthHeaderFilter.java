package com.comercialvalerio.presentation.client;

import java.io.IOException;

import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.security.HeaderNames;
import com.comercialvalerio.presentation.core.UiContext;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

/**
 * Agrega encabezados de autenticación con la información del empleado conectado.
 */
public class AuthHeaderFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        EmpleadoDto emp = UiContext.getUsuarioActual();
        if (emp != null) {
            if (emp.idPersona() != null) {
                ctx.getHeaders().putSingle(HeaderNames.X_ID_EMPLEADO, emp.idPersona());
            }
            if (emp.rolNombre() != null) {
                ctx.getHeaders().putSingle(HeaderNames.X_ROL, emp.rolNombre());
            }
        }
    }
}

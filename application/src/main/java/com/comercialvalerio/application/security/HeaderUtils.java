package com.comercialvalerio.application.security;

import jakarta.ws.rs.container.ContainerRequestContext;

import static com.comercialvalerio.application.security.HeaderNames.X_ID_EMPLEADO;
import static com.comercialvalerio.application.security.HeaderNames.X_ROL;

/** Utilidad para extraer cabeceras relacionadas con el empleado. */
public final class HeaderUtils {

    /**
     * Valores de cabecera procesados.
     *
     * @param id  identificador de empleado o {@code null} si falta o es inválido
     * @param rol rol enviado por el cliente o {@code null}
     */
    public record EmpleadoHeaders(Integer id, String rol, boolean invalidId) {}

    /**
     * Analiza las cabeceras <code>X-IdEmpleado</code> y <code>X-Rol</code> del
     * contexto de la petición.
     *
     * @return cabeceras procesadas (id puede ser {@code null} si falta o es inválido)
     */
    public static EmpleadoHeaders parseEmpleadoHeaders(ContainerRequestContext ctx) {
        String idRaw = ctx.getHeaderString(X_ID_EMPLEADO);
        Integer id = null;
        boolean invalidId = false;
        if (idRaw != null) {
            try {
                id = Integer.valueOf(idRaw);
            } catch (NumberFormatException ex) {
                invalidId = true;
            }
        }
        String rol = ctx.getHeaderString(X_ROL);
        return new EmpleadoHeaders(id, rol, invalidId);
    }

    private HeaderUtils() {}
}

package com.comercialvalerio.application.security;

/**
 * Nombres de cabeceras HTTP utilizadas para autenticación.
 */
public final class HeaderNames {
    private HeaderNames() {}

    /** Cabecera que contiene el id del empleado. */
    public static final String X_ID_EMPLEADO = "X-IdEmpleado";
    /** Cabecera que contiene el rol del empleado. */
    public static final String X_ROL = "X-Rol";
}

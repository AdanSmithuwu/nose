package com.comercialvalerio.domain.security;

import com.comercialvalerio.domain.exception.AuthorizationException;
import com.comercialvalerio.domain.model.RolNombre;

/** Utilidades para validaciones de seguridad usadas por infraestructura. */
public final class AdminChecks {
    private AdminChecks() {}

    /**
     * Verifica que la solicitud actual posea el rol de Administrador.
     *
     * @throws AuthorizationException cuando el rol no es Administrador
     */
    public static void requireAdminRole() {
        String rol = RequestContext.rol();
        if (rol == null) {
            throw new AuthorizationException("Sólo Administrador");
        }
        RolNombre rolNombre;
        try {
            rolNombre = RolNombre.fromNombre(rol);
        } catch (IllegalArgumentException ex) {
            throw new AuthorizationException("Sólo Administrador");
        }
        if (rolNombre != RolNombre.ADMINISTRADOR) {
            throw new AuthorizationException("Sólo Administrador");
        }
    }
}

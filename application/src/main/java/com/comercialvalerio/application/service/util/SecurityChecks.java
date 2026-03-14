package com.comercialvalerio.application.service.util;

import com.comercialvalerio.domain.exception.AuthorizationException;
import com.comercialvalerio.domain.security.AdminChecks;
import jakarta.ws.rs.ForbiddenException;

/** Métodos de utilidad para validaciones de seguridad comunes. */
public final class SecurityChecks {
    private SecurityChecks() {}

    /**
     * Garantiza que la solicitud actual tenga el rol Administrador.
     *
     * @throws ForbiddenException cuando el rol no es Administrador
     */
    public static void requireAdminRole() {
        try {
            AdminChecks.requireAdminRole();
        } catch (AuthorizationException ex) {
            throw new ForbiddenException(ex.getMessage());
        }
    }
}

package com.comercialvalerio.domain.security.service;

import com.comercialvalerio.domain.model.Empleado;

/**
 * Servicio de dominio encargado de verificar contraseñas y
 * registrar intentos fallidos de acceso.
 */
public interface AutenticacionService {

    /**
     * Valida la contraseña de un empleado y actualiza los contadores de
     * seguridad.
     *
     * @param empleado empleado autenticado
     * @param plainPassword contraseña en texto plano
     * @throws com.comercialvalerio.domain.exception.AuthenticationException
     *         si la contraseña es incorrecta o la cuenta está bloqueada
     */
    void autenticar(Empleado empleado, String plainPassword);

    /** Recarga los valores de configuración de intentos y bloqueo. */
    default void refrescarLimites() {}
}

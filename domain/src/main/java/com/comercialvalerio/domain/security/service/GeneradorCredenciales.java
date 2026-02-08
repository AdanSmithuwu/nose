package com.comercialvalerio.domain.security.service;

/** Servicio de dominio para generar usuarios y contraseñas. */
public interface GeneradorCredenciales {

    /**
     * Genera un nombre de usuario único basándose en los datos de la persona.
     *
     * @param nombres   nombres de la persona
     * @param apellidos apellidos de la persona
     * @return nombre de usuario único
     */
    String generarUsuario(String nombres, String apellidos);

    /**
     * Genera una contraseña aleatoria.
     *
     * @param longitud número de caracteres
     * @return contraseña generada
     */
    String generarClave(int longitud);
}

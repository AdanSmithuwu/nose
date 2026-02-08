package com.comercialvalerio.domain.security;
/*
 * Contrato para hashear y verificar contraseñas.
 */
public interface PasswordHasher {
    /* Devuelve un hash seguro de la contraseña en texto plano. */
    String hash(String plain);

    /* Verifica que el texto plano coincida con el hash. */
    boolean verify(String plain, String hash);
}

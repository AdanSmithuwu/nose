package com.comercialvalerio.infrastructure.security;

import com.comercialvalerio.domain.security.PasswordHasher;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.enterprise.context.ApplicationScoped;
import com.comercialvalerio.infrastructure.config.AppConfig;
import jakarta.annotation.PreDestroy;

/**
 * Implementación de PasswordHasher usando Argon2id.
 */
@ApplicationScoped
public class Argon2PasswordHasher implements PasswordHasher {
    private final int iterations;
    private final int memory;
    private final int parallelism;

    /** Las instancias de Argon2id no son thread-safe, por ello reutilizamos una por hilo. */
    private static final ThreadLocal<Argon2> ARGON2 = ThreadLocal.withInitial(
        () -> Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
    );

    // Las instancias de Argon2 no son thread-safe; mantenemos una por hilo
    // mediante ThreadLocal para evitar asignaciones repetidas.

    public Argon2PasswordHasher() {
        this.iterations  = AppConfig.getInt("password.iterations");
        this.memory      = AppConfig.getInt("password.memory");
        this.parallelism = AppConfig.getInt("password.parallelism");
    }

    @PreDestroy
    void cleanup() {
        ARGON2.remove();
    }

    @Override
    public String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("No se puede hashear contrase\u00f1a vac\u00eda");
        }
        // Usamos explícitamente Argon2id mediante la instancia por hilo
        Argon2 arg = ARGON2.get();
        char[] chars = plain.toCharArray();
        try {
            return arg.hash(iterations, memory, parallelism, chars);
        } finally {
            arg.wipeArray(chars);
        }
    }

    @Override
    public boolean verify(String plain, String hash) {
        if (plain == null || hash == null) return false;
        // Se requiere Argon2id para verificar correctamente hashes generados con
        // esa variante. Usar el valor por defecto (Argon2i) siempre devolvería false.
        Argon2 arg = ARGON2.get();
        char[] chars = plain.toCharArray();
        try {
            return arg.verify(hash, chars);
        } finally {
            arg.wipeArray(chars);
        }
    }
}

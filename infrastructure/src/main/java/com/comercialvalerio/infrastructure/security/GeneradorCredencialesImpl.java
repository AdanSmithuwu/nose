package com.comercialvalerio.infrastructure.security;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.security.service.GeneradorCredenciales;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación de {@link GeneradorCredenciales}. */
@ApplicationScoped
public class GeneradorCredencialesImpl implements GeneradorCredenciales {

    private final EmpleadoRepository repo;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHARSET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    /** Límite para evitar bucles infinitos al generar sufijos de usuario. */
    static final int MAX_SUFFIX_ATTEMPTS = 1000;
    /** Patrón reutilizable para detectar sufijos numéricos. */
    private static final Pattern NUMERIC_SUFFIX = Pattern.compile("(\\d+)$");

    @Inject
    public GeneradorCredencialesImpl(EmpleadoRepository repo) {
        this.repo = repo;
    }

    @Override
    public String generarUsuario(String nombres, String apellidos) {
        String initials = Arrays.stream(nombres.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> s.substring(0, 1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining());
        String base = (initials + apellidos.trim().replaceAll("\\s+", "")).toLowerCase(Locale.ROOT);

        var encontrados = repo.findByUsuarioLike(base + "%");
        boolean baseOcupado = false;
        int maxSuffix = 0;

        for (var emp : encontrados) {
            String usuario = emp.getUsuario().toLowerCase(Locale.ROOT);
            if (usuario.equals(base)) {
                baseOcupado = true;
            }

            var matcher = NUMERIC_SUFFIX.matcher(usuario);
            if (matcher.find(base.length())) {
                int suffix = Integer.parseInt(matcher.group(1));
                if (suffix > maxSuffix) {
                    maxSuffix = suffix;
                }
            }
        }

        if (!baseOcupado) {
            return base;
        }

        if (maxSuffix >= MAX_SUFFIX_ATTEMPTS) {
            throw new IllegalStateException(
                    "No se pudo generar usuario único para " + base);
        }

        return base + (maxSuffix + 1);
    }

    @Override
    public String generarClave(int longitud) {
        if (longitud <= 0) {
            throw new IllegalArgumentException("longitud debe ser mayor a cero");
        }
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CHARSET[RANDOM.nextInt(CHARSET.length)]);
        }
        return sb.toString();
    }
}

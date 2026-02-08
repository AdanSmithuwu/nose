package com.comercialvalerio.domain.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.security.PasswordHasher;
import static com.comercialvalerio.domain.util.ValidationUtils.validateRequiredLength;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotBlank;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationMessages.USER_REQUIRED_MAX_LENGTH;

/*
 * Empleado / usuario interno que accede al sistema.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>Usuario único (<code>UNIQUE(usuario)</code>)</li>
 *   <li>Hash de clave obligatorio</li>
 *   <li>FK a tabla <b>rol</b></li>
 * </ul>
 */
public class Empleado extends Persona {

    /** Máximo de intentos fallidos antes de bloquear la cuenta. */
    public static final int MAX_FAILED_ATTEMPTS = 3;

    /** Minutos que dura el bloqueo temporal de la cuenta. */
    public static final int LOCKOUT_MINUTES = 5;

    private static final Pattern USUARIO_PATTERN = Pattern.compile("[\\w.@-]+");

    private String         usuario;         // único
    private String         hashClave;       // hash Argon2
    private LocalDateTime  fechaCambioClave; // no nulo
    private Rol            rol;             // no nulo
    private LocalDateTime  ultimoAcceso;    // puede ser null
    private int            intentosFallidos; // ≥ 0
    private LocalDateTime  bloqueadoHasta;  // puede ser null/futuro

    /* ---------- Constructor completo con invariantes ---------- */
    public Empleado(String usuario, String hashClave, Rol rol,
                    LocalDateTime ultimoAcceso, int intentosFallidos,
                    LocalDateTime bloqueadoHasta,
                    LocalDateTime fechaCambioClave,
                    Integer idPersona, String nombres, String apellidos,
                    String dni, String telefono,
                    LocalDate fechaRegistro, Estado estado) {

        super(idPersona, nombres, apellidos, dni, telefono,
              fechaRegistro, estado);

        validarUsuario(usuario);
        validarHashClave(hashClave);
        validarRol(rol);
        validarIntentos(intentosFallidos);
        validarFechas(ultimoAcceso, bloqueadoHasta);
        requireNotNull(fechaCambioClave, "Fecha de cambio de clave obligatoria");

        this.usuario         = usuario.trim();
        this.hashClave       = hashClave;
        this.rol             = rol;
        this.ultimoAcceso    = ultimoAcceso;
        this.intentosFallidos= intentosFallidos;
        this.bloqueadoHasta  = bloqueadoHasta;
        this.fechaCambioClave= fechaCambioClave;
    }

    /* Constructor auxiliar sin datos de Persona (se rellenan luego). */
    public Empleado(String usuario, String hashClave, Rol rol,
                    LocalDateTime ultimoAcceso, int intentosFallidos,
                    LocalDateTime bloqueadoHasta,
                    LocalDateTime fechaCambioClave) {
        this(usuario, hashClave, rol, ultimoAcceso, intentosFallidos,
             bloqueadoHasta, fechaCambioClave,
             null, null, null, null, null, null, null);
    }

    public Empleado() {}

    /* ---------- Getters ---------- */
    public String        getUsuario()         { return usuario; }
    public String        getHashClave()       { return hashClave; }
    public Rol           getRol()             { return rol; }
    public LocalDateTime getUltimoAcceso()    { return ultimoAcceso; }
    public int           getIntentosFallidos(){ return intentosFallidos; }
    public LocalDateTime getBloqueadoHasta()  { return bloqueadoHasta; }
    public LocalDateTime getFechaCambioClave(){ return fechaCambioClave; }

    /* ---------- Setters con validaciones ---------- */

    public void setUsuario(String usuario) {
        validarUsuario(usuario);
        this.usuario = usuario.trim();
    }

    public void setHashClave(String hashClave) {
        validarHashClave(hashClave);
        this.hashClave = hashClave;
    }

    public void setRol(Rol rol) {
        validarRol(rol);
        this.rol = rol;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        if (ultimoAcceso != null && ultimoAcceso.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "El último acceso no puede ser una fecha futura");
        this.ultimoAcceso = ultimoAcceso;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        validarIntentos(intentosFallidos);
        this.intentosFallidos = intentosFallidos;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        validarFechas(null, bloqueadoHasta);
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public void setFechaCambioClave(LocalDateTime fechaCambioClave) {
        if (fechaCambioClave == null) {
            fechaCambioClave = LocalDateTime.now();
        }
        this.fechaCambioClave = fechaCambioClave;
    }

    /* ---------- Operaciones de dominio ---------- */

    /**
     * Cambia el estado del empleado validando jerarquía y actor.
     * @param nuevo nuevo estado a asignar
     * @param actor empleado que realiza la acción
     */
    public void cambiarEstado(Estado nuevo, Empleado actor) {
        if (nuevo == null)
            throw new BusinessRuleViolationException("Estado obligatorio");

        if (actor == null || actor.getRol() == null)
            throw new BusinessRuleViolationException("Actor sin rol");

        boolean root = actor.getUsuario() != null &&
                "admin".equalsIgnoreCase(actor.getUsuario());

        if (!root && this.getIdPersona() != null && this.getIdPersona().equals(actor.getIdPersona())) {
            if (EstadoNombre.INACTIVO.equalsNombre(nuevo.getNombre()))
                throw new BusinessRuleViolationException("No puede desactivar su propia cuenta");
        }

        if (!root && EstadoNombre.INACTIVO.equalsNombre(nuevo.getNombre())) {
            if (this.getRol() != null && this.getRol().getNivel() <= actor.getRol().getNivel()) {
                throw new BusinessRuleViolationException(
                        "No puede desactivar a un usuario de igual o mayor jerarquía");
            }
        }

        this.setEstado(nuevo);
    }

    /** Resetea la contraseña del empleado y limpia bloqueos. */
    public void resetClave(String nuevaClave, PasswordHasher hasher) {
        requireNotBlank(nuevaClave, "La nueva clave es obligatoria");
        if (hasher == null)
            throw new BusinessRuleViolationException("Hasher requerido");

        String hash = hasher.hash(nuevaClave);
        setHashClave(hash);
        setFechaCambioClave(LocalDateTime.now());
        setIntentosFallidos(0);
        setBloqueadoHasta(null);
    }

    /** Registra un acceso exitoso reiniciando contadores. */
    public void registrarAcceso(LocalDateTime fecha) {
        setUltimoAcceso(fecha);
        setIntentosFallidos(0);
        setBloqueadoHasta(null);
    }

    /** Incrementa contador de fallos y calcula fecha de bloqueo si aplica. */
    public void incrementarIntentoFallido(int maxFallidos, int minutosBloqueo) {
        this.intentosFallidos += 1;
        if (this.intentosFallidos >= maxFallidos) {
            this.bloqueadoHasta =
                    LocalDateTime.now().plusMinutes(minutosBloqueo);
        }
    }

    /** Usa constantes por defecto para incrementar intento fallido. */
    public void incrementarIntentoFallido() {
        incrementarIntentoFallido(MAX_FAILED_ATTEMPTS, LOCKOUT_MINUTES);
    }

    /* ---------- Validaciones privadas ---------- */

    private void validarUsuario(String u) {
        validateRequiredLength(u, DbConstraints.LEN_USUARIO,
                String.format(USER_REQUIRED_MAX_LENGTH, DbConstraints.LEN_USUARIO));
        if (!USUARIO_PATTERN.matcher(u).matches())
            throw new BusinessRuleViolationException(
                "El usuario contiene caracteres no válidos");
    }

    private void validarHashClave(String h) {
        validateRequiredLength(h, DbConstraints.LEN_HASH_CLAVE,
                "La clave cifrada es obligatoria (máx. " + DbConstraints.LEN_HASH_CLAVE + " caracteres)");
        // longitud mínima definida para hashes Argon2.
        if (h.length() < DbConstraints.MIN_HASH_CLAVE)
            throw new BusinessRuleViolationException(
                "El hash de la clave tiene un formato inválido");
    }

    private void validarRol(Rol r) {
        requireNotNull(r, "El rol del empleado es obligatorio");
    }

    private void validarIntentos(int i) {
        if (i < 0)
            throw new BusinessRuleViolationException(
                "Los intentos fallidos no pueden ser negativos");
    }

    private void validarFechas(LocalDateTime acceso, LocalDateTime bloqueado) {
        if (acceso != null && acceso.isAfter(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "El último acceso no puede ser futuro");
        if (bloqueado != null && bloqueado.isBefore(LocalDateTime.now()))
            throw new BusinessRuleViolationException(
                "La fecha de desbloqueo debe ser futura");
    }
}

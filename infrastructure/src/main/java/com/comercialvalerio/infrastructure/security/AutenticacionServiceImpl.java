package com.comercialvalerio.infrastructure.security;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.domain.exception.AuthenticationException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.ParametroSistemaRepository;
import com.comercialvalerio.domain.security.PasswordHasher;
import com.comercialvalerio.domain.security.service.AutenticacionService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación base de {@link AutenticacionService}. */
@ApplicationScoped
public class AutenticacionServiceImpl implements AutenticacionService {

    private static final Logger LOG =
            Logger.getLogger(AutenticacionServiceImpl.class.getName());

    private final PasswordHasher     hasher;
    private final EmpleadoRepository repo;
    private final ParametroSistemaRepository repoPar;
    private volatile int  maxFallidos;
    private volatile int  minBloqueo;
    private volatile boolean configInvalida;

    @Inject
    public AutenticacionServiceImpl(PasswordHasher hasher,
                                   EmpleadoRepository repo,
                                   ParametroSistemaRepository repoPar) {
        this.hasher = hasher;
        this.repo   = repo;
        this.repoPar= repoPar;
        refrescarLimites();
    }

    /** Lee nuevamente los par\u00e1metros de seguridad. */
    @Override
    public void refrescarLimites() {
        configInvalida = false;

        var pMax = repoPar.findByClave("MAX_INTENTOS_FALLIDOS");
        var pMin = repoPar.findByClave("MINUTOS_BLOQUEO_CUENTA");

        try {
            if (pMax == null || pMin == null)
                throw new IllegalStateException("Faltan par\u00e1metros");

            maxFallidos = pMax.getValor().intValue();
            minBloqueo  = pMin.getValor().intValue();

            if (maxFallidos <= 0 || minBloqueo <= 0)
                throw new IllegalArgumentException("Valores de seguridad inv\u00e1lidos");
        } catch (IllegalStateException ex) {
            LOG.log(Level.SEVERE,
                    "Faltan par\u00e1metros de seguridad", ex);
            configInvalida = true;
            maxFallidos = Empleado.MAX_FAILED_ATTEMPTS;
            minBloqueo  = Empleado.LOCKOUT_MINUTES;
            throw new AuthenticationException("Configuraci\u00f3n de seguridad inv\u00e1lida");
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE,
                    "Valores de seguridad inv\u00e1lidos", ex);
            configInvalida = true;
            maxFallidos = Empleado.MAX_FAILED_ATTEMPTS;
            minBloqueo  = Empleado.LOCKOUT_MINUTES;
            throw new AuthenticationException("Configuraci\u00f3n de seguridad inv\u00e1lida");
        }
    }

    @Override
    public void autenticar(Empleado emp, String plainPassword) {
        // parámetros ya cargados; se refrescan manualmente cuando sea necesario
        if (emp == null)
            throw new IllegalArgumentException("empleado nulo");
        if (plainPassword == null)
            throw new IllegalArgumentException("contraseña nula");
        if (configInvalida) {
            throw new AuthenticationException(
                "Configuraci\u00f3n de bloqueo inv\u00e1lida");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (!repo.isActivo(emp.getIdPersona())) {
            LOG.log(Level.WARNING,
                    "Intento de login con cuenta no activa: {0}",
                    emp.getUsuario());
            throw new AuthenticationException("Cuenta inactiva");
        }

        if (emp.getBloqueadoHasta() != null && emp.getBloqueadoHasta().isBefore(ahora)) {
            emp.setIntentosFallidos(0);
            emp.setBloqueadoHasta(null);
            repo.actualizarSeguridad(emp.getIdPersona(), 0, null);
        }

        if (emp.getBloqueadoHasta() != null && emp.getBloqueadoHasta().isAfter(ahora)) {
            long minutosRestantes = Duration
                    .between(ahora, emp.getBloqueadoHasta())
                    .toMinutes();
            LOG.log(Level.WARNING,
                    "Cuenta bloqueada para {0} hasta {1}",
                    new Object[]{emp.getUsuario(), emp.getBloqueadoHasta()});
            throw new AuthenticationException(
                String.format("Cuenta bloqueada (%d minutos restantes)",
                              minutosRestantes),
                minutosRestantes
            );
        }

        boolean ok;
        try {
            ok = hasher.verify(plainPassword, emp.getHashClave());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error verifying password for {0}", emp.getUsuario());
            throw new AuthenticationException("Credenciales inválidas", ex);
        }
        if (!ok) {
            emp.incrementarIntentoFallido(maxFallidos, minBloqueo);
            repo.actualizarSeguridad(emp.getIdPersona(),
                                     emp.getIntentosFallidos(),
                                     emp.getBloqueadoHasta());
            LOG.log(Level.WARNING, "Contrase\u00f1a incorrecta para usuario {0}",
                    emp.getUsuario());
            throw new AuthenticationException("Credenciales inválidas");
        }
    }
}

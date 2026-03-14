package com.comercialvalerio.application.service.impl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.EmpleadoCreateDto;
import com.comercialvalerio.application.dto.EmpleadoCredencialesDto;
import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.application.mapper.EmpleadoDtoMapper;
import com.comercialvalerio.application.service.EmpleadoService;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.common.DependencyUtils;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.domain.exception.AuthenticationException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DataAccessException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.model.Rol;
import com.comercialvalerio.domain.notification.AlertService;
import com.comercialvalerio.domain.repository.BitacoraLoginRepository;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.domain.repository.ReporteRepository;
import com.comercialvalerio.domain.repository.RolRepository;
import com.comercialvalerio.domain.repository.TransaccionRepository;
import com.comercialvalerio.domain.security.PasswordHasher;
import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.domain.security.service.AutenticacionService;
import com.comercialvalerio.domain.security.service.GeneradorCredenciales;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Servicio de aplicación para operaciones con {@link Empleado}. El
 * {@link EmpleadoDtoMapper} se inyecta mediante el constructor para facilitar
 * el uso en pruebas sin recurrir a reflexión.
 */
@ApplicationScoped
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger LOG = Logger.getLogger(EmpleadoServiceImpl.class.getName());

    private final EmpleadoRepository        repoEmp;
    private final RolRepository             repoRol;
    private final EstadoCache               estadoCache;
    private final TransaccionRepository     repoTx;
    private final PedidoRepository          repoPed;
    private final MovimientoInventarioRepository repoMov;
    private final BitacoraLoginRepository   repoBit;
    private final ReporteRepository         repoRep;
    private final PasswordHasher     hasher;
    private final AutenticacionService authSvc;
    private final GeneradorCredenciales generador;
    private final EmpleadoDtoMapper mapper;
    private final AlertService alertSvc;

    @Inject
    public EmpleadoServiceImpl(EmpleadoRepository repoEmp,
                               RolRepository repoRol,
                               EstadoCache estadoCache,
                               PasswordHasher hasher,
                               AutenticacionService authSvc,
                               GeneradorCredenciales generador,
                               EmpleadoDtoMapper mapper,
                               AlertService alertSvc,
                               TransaccionRepository repoTx,
                               PedidoRepository repoPed,
                               MovimientoInventarioRepository repoMov,
                               BitacoraLoginRepository repoBit,
                               ReporteRepository repoRep) {
        this.repoEmp = repoEmp;
        this.repoRol = repoRol;
        this.estadoCache = estadoCache;
        this.hasher = hasher;
        this.authSvc = authSvc;
        this.generador = generador;
        this.mapper = mapper;
        this.alertSvc = alertSvc;
        this.repoTx = repoTx;
        this.repoPed = repoPed;
        this.repoMov = repoMov;
        this.repoBit = repoBit;
        this.repoRep = repoRep;
    }

    /* ---------- CRUD ---------- */

    @Override
    @Transactional
    public List<EmpleadoDto> listar() {
        return ServiceUtils.mapList(repoEmp.findAll(), mapper::toDto);
    }

    @Override
    @Transactional
    public EmpleadoDto obtener(Integer id) {
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado no encontrado");
        return mapper.toDto(emp);
    }

    @Override
    @Transactional
    public EmpleadoDto crear(EmpleadoCreateDto in) {
        SecurityChecks.requireAdminRole();
        // Validaciones de rol
        if (in.idRol() == null) {
            throw new BusinessRuleViolationException("idRol es obligatorio");
        }
        Rol rol = ServiceChecks.requireFound(
                repoRol.findById(in.idRol()),
                "Rol inexistente (id=" + in.idRol() + ")");

        // Estado 'Activo' por defecto
        Estado activo = estadoCache.get("Persona", EstadoNombre.ACTIVO);

        // Generar usuario y contraseña (opcionalmente proporcionada)
        String usuario  = generador.generarUsuario(in.nombres(), in.apellidos());
        String plainPwd = (in.plainPassword() != null && !in.plainPassword().isBlank())
                ? in.plainPassword()
                : generador.generarClave(10);

        // Crear entidad
        Empleado emp = new Empleado();
        emp.setNombres(in.nombres());
        emp.setApellidos(in.apellidos());
        emp.setDni(in.dni());
        emp.setTelefono(in.telefono());
        emp.setFechaRegistro(LocalDate.now());
        emp.setEstado(activo);
        emp.setUsuario(usuario);
        emp.setRol(rol);
        emp.resetClave(plainPwd, hasher);

        // Guardar (usa SP o merge internamente)
        repoEmp.save(emp);

        // Devolver DTO incluyendo la contraseña en claro sólo en la creación
        EmpleadoDto base = mapper.toDto(emp);
        return new EmpleadoDto(
            base.idPersona(), base.nombres(), base.apellidos(), base.dni(),
            base.telefono(), base.idRol(), base.rolNombre(),
            base.usuario(), base.estado(), base.fechaCambioClave(), plainPwd
        );
    }

    @Override
    @Transactional
    public EmpleadoDto actualizar(Integer id, EmpleadoCreateDto chg) {
        SecurityChecks.requireAdminRole();
        Empleado actor = ServiceChecks.requireFound(
                repoEmp.findById(RequestContext.idEmpleado()),
                "Empleado no encontrado");

        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado no encontrado");
        boolean root = actor.getUsuario() != null &&
                "admin".equalsIgnoreCase(actor.getUsuario());
        if (!root && emp.getRol() != null &&
                emp.getRol().getNivel() <= actor.getRol().getNivel()) {
            throw new BusinessRuleViolationException("Operación no permitida");
        }

        // Validar rol
        if (chg.idRol() == null)
            throw new BusinessRuleViolationException("idRol es obligatorio");
        Rol rol = ServiceChecks.requireFound(
                repoRol.findById(chg.idRol()),
                "Rol inexistente (id=" + chg.idRol() + ")");
        emp.setRol(rol);

        // Actualizar datos permitidos
        emp.setNombres(chg.nombres());
        emp.setApellidos(chg.apellidos());
        emp.setTelefono(chg.telefono());

        repoEmp.save(emp);
        return mapper.toDto(emp);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        SecurityChecks.requireAdminRole();
        repoEmp.delete(id);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, CambiarEstadoDto dto) {
        SecurityChecks.requireAdminRole();
        Empleado actor = ServiceChecks.requireFound(
                repoEmp.findById(RequestContext.idEmpleado()),
                "Empleado no encontrado");

        Empleado target = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado no encontrado");

        EstadoNombre nuevoEstado =
                EstadoNombre.fromNombre(dto.nuevoEstado());
        Estado nuevo = estadoCache.get("Persona", nuevoEstado);

        target.cambiarEstado(nuevo, actor);
        repoEmp.updateEstado(id, nuevoEstado);
    }

    @Override
    @Transactional
    public void resetClave(Integer id, String nuevaClave) {
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado no encontrado");
        emp.resetClave(nuevaClave, hasher);
        repoEmp.save(emp);
    }

    @Override
    @Transactional
    public EmpleadoDto updateCredenciales(Integer id, EmpleadoCredencialesDto dto) {
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado no encontrado");
        return EmpleadoCredencialHelper.updateCredenciales(
                repoEmp,
                emp,
                dto,
                hasher,
                generador,
                mapper);
    }

    @Override
    @Transactional
    public List<String> obtenerDependencias(Integer idEmpleado) {
        List<String> deps = new ArrayList<>();
        DependencyUtils.addIf(repoTx.existsByEmpleado(idEmpleado), "transacciones", deps);
        DependencyUtils.addIf(repoPed.existsByEmpleadoEntrega(idEmpleado), "entregas", deps);
        DependencyUtils.addIf(repoMov.existsMovimientosByEmpleado(idEmpleado), "movimientos", deps);
        DependencyUtils.addIf(repoBit.existsBitacoraByEmpleado(idEmpleado), "bitacora", deps);
        DependencyUtils.addIf(!repoRep.findByEmpleado(idEmpleado).isEmpty(), "reportes", deps);
        return deps;
    }
    /* ---------- Auth + bloqueo ---------- */

    @Override
    public EmpleadoDto autenticar(String usuario, String plainPassword) {
        java.util.Optional<Empleado> opt = repoEmp.findByUsuario(usuario);
        if (opt.isEmpty()) {
            LOG.log(Level.WARNING,
                    "Intento de login con usuario inexistente: {0}", usuario);
            throw new AuthenticationException("Credenciales inválidas");
        }
        Empleado emp = opt.get();

        if (!repoEmp.isActivo(emp.getIdPersona())) {
            LOG.log(Level.WARNING,
                    "Intento de login con cuenta no activa: {0}", usuario);
            throw new AuthenticationException("Cuenta inactiva");
        }

        try {
            // delegar autenticación y control de intentos
            authSvc.autenticar(emp, plainPassword);
        } catch (AuthenticationException ex) {
            // dispara el registro de intentos fallidos
            throw ex;
        }

        // login exitoso → registrar acceso y reiniciar contadores
        LocalDateTime ahora = LocalDateTime.now();
        emp.registrarAcceso(ahora);
        try {
            repoEmp.actualizarUltimoAcceso(emp.getIdPersona(), ahora);
        } catch (DataAccessException | EntityNotFoundException ex) {
            LOG.log(Level.WARNING,
                    "No se pudo registrar último acceso: {0}", ex.getMessage());
            if (alertSvc != null) {
                alertSvc.alertAdmin(
                        "Error actualizando último acceso de empleado "
                        + emp.getIdPersona());
            }
        }

        // dispara el registro de login exitoso

        // devolver DTO
        return mapper.toDto(emp);
    }

}

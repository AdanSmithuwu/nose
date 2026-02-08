package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.EstadoNombre;
import java.time.LocalDateTime;
import java.util.List;

/* Acceso a los empleados del sistema (usuarios internos) */
public interface EmpleadoRepository {
    List<Empleado> findAll();
    /** Obtiene todos los empleados para los ids indicados. */
    List<Empleado> findAllById(java.util.Collection<Integer> ids);
    java.util.Optional<Empleado> findById(Integer id);
    java.util.Optional<Empleado> findByUsuario(String usuario);
    /* Busca empleados cuyo nombre de usuario inicie con el patrón indicado. */
    List<Empleado> findByUsuarioLike(String patron);
    void save(Empleado empleado);
    void delete(Integer id);
    void updateEstado(Integer idEmpleado, EstadoNombre nuevoEstado);
    void resetClave  (Integer idEmpleado, String hashArgon2);
    void updateCredenciales(Integer idEmpleado, String nuevoUsuario, String hashArgon2);
    /* Actualiza los intentos fallidos y bloqueo de un usuario. */
    void actualizarSeguridad(Integer idEmpleado, int intentosFallidos, LocalDateTime bloqueadoHasta);
    /** Verifica si el empleado indicado está activo. */
    boolean isActivo(Integer idEmpleado);
    /*
     * Registra fecha/hora de último acceso y reinicia los
     * contadores de seguridad tras un login exitoso.
     */
    void actualizarUltimoAcceso(Integer idEmpleado, LocalDateTime fechaAcceso);

    /**
     * Elimina de la caché de segundo nivel las entidades asociadas al empleado
     * para garantizar que futuros accesos reflejen el estado actualizado.
     */
    default void evictCache(Integer idEmpleado) {}
}

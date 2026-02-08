package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.MovimientoInventarioRepository;
import com.comercialvalerio.domain.repository.BitacoraLoginRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.EmpleadoEntity;
import com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity;
import com.comercialvalerio.infrastructure.persistence.entity.RolEntity;
import com.comercialvalerio.infrastructure.persistence.entity.PersonaEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.EmpleadoMapper;
import java.util.Locale;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

/** Implementación JPA de {@link EmpleadoRepository}. */
@ApplicationScoped
public class EmpleadoRepositoryImpl extends BaseRepository implements EmpleadoRepository {

    @Inject
    EmpleadoMapper mapper;
    @Inject
    MovimientoInventarioRepository repoMov;
    @Inject
    BitacoraLoginRepository repoBitacora;
    /* ---------- Lectura ---------- */
    @Override
    public List<Empleado> findAll() {
        return read(em -> map(
                em.createNamedQuery("Empleado.findAll", EmpleadoEntity.class),
                mapper::toDomain));
    }

    @Override
    public List<Empleado> findAllById(java.util.Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> map(
                em.createNamedQuery("Empleado.findByIds", EmpleadoEntity.class)
                        .setParameter("ids", ids),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<Empleado> findById(Integer id) {
        return readOptional(em -> em.createNamedQuery("Empleado.findByIdFull", EmpleadoEntity.class)
                .setParameter("id", id)
                .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }
    @Override
    public java.util.Optional<Empleado> findByUsuario(String usuario) {
        return readOptional(em -> em.createNamedQuery("Empleado.findByUsuario", EmpleadoEntity.class)
                            .setParameter("usuario", usuario)
                            .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                            .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }

    @Override
    public List<Empleado> findByUsuarioLike(String patron) {
        return read(em -> map(
                em.createNamedQuery("Empleado.findByUsuarioLike", EmpleadoEntity.class)
                        .setParameter("patron", patron.toLowerCase(Locale.ROOT) + "%"),
                mapper::toDomain));
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(Empleado emp) {
        tx(em -> {
            // Validación de unicidad de usuario y DNI antes de registrar
            if (emp.getIdPersona() == null) {
                boolean usuarioDup = em.createNamedQuery(
                        "Empleado.countByUsuario", Long.class)
                    .setParameter("u", emp.getUsuario())
                    .getSingleResult() > 0;
                if (usuarioDup)
                    throw new DuplicateEntityException(
                            "Ya existe el usuario «" + emp.getUsuario() + "»");

                boolean dniDup = em.createNamedQuery(
                        "Persona.countByDniExcludingId", Long.class)
                    .setParameter("dni", emp.getDni())
                    .setParameter("id", emp.getIdPersona())
                    .getSingleResult() > 0;
                if (dniDup)
                    throw new DuplicateEntityException(
                            "Ya existe una persona con DNI " + emp.getDni());

                // Alta vía SP
                StoredProcedureQuery sp = em
                    .createNamedStoredProcedureQuery("Empleado.registrar");

                sp.setParameter("nombres",       emp.getNombres());
                sp.setParameter("apellidos",     emp.getApellidos());
                sp.setParameter("dni",           emp.getDni());
                sp.setParameter("telefono",      emp.getTelefono());
                sp.setParameter("fechaRegistro", java.sql.Date.valueOf(emp.getFechaRegistro()));
                sp.setParameter("idEstado",      emp.getEstado().getIdEstado());
                sp.setParameter("usuario",       emp.getUsuario());
                sp.setParameter("hashClave",     emp.getHashClave());
                sp.setParameter("idRol",         emp.getRol().getIdRol());

                sp.execute();
                Integer newId = (Integer) sp.getOutputParameterValue("newIdPersona");
                emp.setIdPersona(newId);

            } else {
                // Actualización de datos de Persona y Empleado
                em.createNamedQuery("Empleado.updatePersona")
                  .setParameter("n",  emp.getNombres())
                  .setParameter("a",  emp.getApellidos())
                  .setParameter("t",  emp.getTelefono())
                  .setParameter("id", emp.getIdPersona())
                  .executeUpdate();

                em.createNamedQuery("Empleado.updateEmpleado")
                  .setParameter("r",
                      em.getReference(RolEntity.class, emp.getRol().getIdRol()))
                  .setParameter("h", emp.getHashClave())
                  .setParameter("id", emp.getIdPersona())
                  .executeUpdate();
            }
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            EmpleadoEntity e = em.find(EmpleadoEntity.class, id);
            if (e == null) throw new EntityNotFoundException("Empleado no encontrado (id=" + id + ")");
            /* Regla: no eliminar si tiene transacciones u otros registros */
            long usos = em.createNamedQuery("Empleado.countTransacciones", Long.class)
                          .setParameter("id", id)
                          .getSingleResult();
            if (usos > 0)
                throw new BusinessRuleViolationException("Empleado tiene transacciones asociadas");
            if (repoMov.existsMovimientosByEmpleado(id))
                throw new BusinessRuleViolationException("Empleado tiene movimientos de inventario");
            if (repoBitacora.existsBitacoraByEmpleado(id))
                throw new BusinessRuleViolationException("Empleado tiene registros de bitácora");
            em.remove(e.getPersona());
            return null;
        });
    }

    @Override
    public void updateEstado(Integer id, EstadoNombre estado) {
        tx(em -> {
            EstadoEntity nuevoEstado = em.createNamedQuery("Estado.findByModuloAndNombre", EstadoEntity.class)
                    .setParameter("m", "Persona")
                    .setParameter("n", estado.getNombre())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevoEstado == null)
                throw new EntityNotFoundException("Estado no encontrado");

            EmpleadoEntity emp = em.find(EmpleadoEntity.class, id);
            if (emp == null)
                throw new EntityNotFoundException("Empleado no encontrado");

            emp.getPersona().setEstado(nuevoEstado);
            if (EstadoNombre.ACTIVO.equals(estado)) {
                emp.setIntentosFallidos(0);
                emp.setBloqueadoHasta(null);
            }
            em.merge(emp);
            return null;
        });
    }

    @Override
    public void resetClave(Integer id, String hashArgon2) {
        tx(em -> {
            int n = em.createNamedQuery("Empleado.resetClave")
                .setParameter("h",  hashArgon2)
                .setParameter("id", id)
                .executeUpdate();
            if (n == 0) throw new EntityNotFoundException("Empleado no encontrado");
            return null;
        });
    }

    @Override
    public void updateCredenciales(Integer id, String usuario, String hashArgon2) {
        tx(em -> {
            if (usuario == null && hashArgon2 == null) return null;

            int n = em.createNamedQuery("Empleado.updateCredenciales")
                    .setParameter("u", usuario)
                    .setParameter("h", hashArgon2)
                    .setParameter("id", id)
                    .executeUpdate();
            if (n == 0)
                throw new EntityNotFoundException("Empleado no encontrado");
            return null;
        });
    }
    /* ---------- Seguridad ---------- */
    @Override
    public void actualizarSeguridad(Integer idEmpleado,
                                    int intentosFallidos,
                                    LocalDateTime bloqueadoHasta) {
        tx(em -> {
            int updated = em.createNamedQuery("Empleado.updateSeguridad")
                    .setParameter("i", intentosFallidos)
                    .setParameter("b", bloqueadoHasta)
                    .setParameter("id", idEmpleado)
                    .executeUpdate();
            if (updated == 0)
                throw new EntityNotFoundException("Empleado no encontrado (id=" + idEmpleado + ")");
            return null;
        });
    }

    @Override
    public void actualizarUltimoAcceso(Integer idEmpleado, LocalDateTime fechaAcceso) {
        tx(em -> {
            int updated = em.createNamedQuery("Empleado.updateUltimoAcceso")
                    .setParameter("f", fechaAcceso)
                    .setParameter("id", idEmpleado)
                    .executeUpdate();
            if (updated == 0)
                throw new EntityNotFoundException("Empleado no encontrado (id=" + idEmpleado + ")");
            return null;
        });
    }

    @Override
    public boolean isActivo(Integer idEmpleado) {
        return read(em -> em.createNamedQuery(
                        "Empleado.countActivoById", Long.class)
                .setParameter("id", idEmpleado)
                .setParameter("estado", EstadoNombre.ACTIVO.getNombre())
                .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.BYPASS)
                .getSingleResult() > 0);
    }

    @Override
    public void evictCache(Integer idEmpleado) {
        read(em -> {
            var cache = em.getEntityManagerFactory().getCache();
            cache.evict(EmpleadoEntity.class, idEmpleado);
            cache.evict(PersonaEntity.class, idEmpleado);
            return null;
        });
    }
}

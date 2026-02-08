package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.model.Rol;
import com.comercialvalerio.domain.repository.RolRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.RolEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.RolMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;

/** Implementación JPA de {@link RolRepository}. */
@ApplicationScoped
public class RolRepositoryImpl
        extends CrudRepository<Rol, RolEntity, Integer>
        implements RolRepository {
@Inject
    RolMapper mapper;

    public RolRepositoryImpl() {
        super(RolEntity.class);
    }

    private final java.util.concurrent.ConcurrentMap<Integer, Rol> cacheById = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentMap<String, Rol> cacheByNombre = new java.util.concurrent.ConcurrentHashMap<>();
    /* ---------- Lectura ---------- */
    @Override
    public List<Rol> findAll() {
        return read(em -> map(
                em.createNamedQuery("Rol.findAll", RolEntity.class),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<Rol> findById(Integer id) {
        Rol cached = cacheById.get(id);
        if (cached != null) return java.util.Optional.of(cached);
        java.util.Optional<Rol> result = readOptional(em -> {
            RolEntity e = em.find(RolEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
        result.ifPresent(r -> {
            cacheById.putIfAbsent(id, r);
            cacheByNombre.putIfAbsent(r.getNombre(), r);
        });
        return result;
    }
    @Override
    public java.util.Optional<Rol> findByNombre(String nombre) {
        Rol cached = cacheByNombre.get(nombre);
        if (cached != null) return java.util.Optional.of(cached);
        java.util.Optional<Rol> result = readOptional(em -> em.createNamedQuery("Rol.findByNombre", RolEntity.class)
                            .setParameter("nombre", nombre)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
        result.ifPresent(r -> {
            cacheByNombre.putIfAbsent(nombre, r);
            cacheById.putIfAbsent(r.getIdRol(), r);
        });
        return result;
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(Rol rol) {
        tx(em -> {
            RolEntity entity = mapper.toEntity(rol);
            entity = em.merge(entity);
            assignId(rol, entity);
            cacheById.put(rol.getIdRol(), rol);
            cacheByNombre.put(rol.getNombre(), rol);
            return null;
        });
    }

    @Override
    public void delete(Integer id) {
        tx(em -> {
            RolEntity entity = em.find(RolEntity.class, id);
            if (entity != null) {
                Long cnt = em.createNamedQuery("Rol.countEmpleados", Long.class)
                        .setParameter("id", id)
                        .getSingleResult();
                if (cnt > 0)
                    throw new BusinessRuleViolationException("Rol tiene empleados asociados");
                em.remove(entity);
                cacheById.remove(id);
                cacheByNombre.entrySet().removeIf(e -> e.getValue().getIdRol().equals(id));
            }
            return null;
        });
    }

    @Override
    protected RolEntity toEntity(Rol model) {
        return mapper.toEntity(model);
    }

    @Override
    protected Rol toDomain(RolEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(Rol model, RolEntity entity) {
        model.setIdRol(entity.getIdRol());
    }
}

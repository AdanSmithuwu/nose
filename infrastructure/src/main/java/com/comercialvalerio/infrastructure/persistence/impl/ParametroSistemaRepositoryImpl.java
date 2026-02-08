package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.ParametroSistema;
import com.comercialvalerio.domain.repository.ParametroSistemaRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ParametroSistemaEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ParametroSistemaMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Implementación JPA de {@link ParametroSistemaRepository}. */
@ApplicationScoped
public class ParametroSistemaRepositoryImpl
        extends BaseRepository implements ParametroSistemaRepository {
@Inject
    ParametroSistemaMapper mapper;
    /* ---------- Lectura ---------- */
    @Override
    public List<ParametroSistema> findAll() {
        return read(em -> map(
                em.createNamedQuery("ParametroSistema.findAll",
                                   ParametroSistemaEntity.class),
                mapper::toDomain));
    }
    @Override
    public ParametroSistema findByClave(String clave) {
        return read(em -> em
                .createNamedQuery("ParametroSistema.findByClave",
                                  ParametroSistemaEntity.class)
                .setParameter("clave", clave)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    public int getInt(String clave, int defecto) {
        return read(em -> {
            Number n = (Number) em.createNamedQuery("ParametroSistema.decimal")
                .setParameter(1, clave)
                .setParameter(2, defecto)
                .getSingleResult();
            return n == null ? defecto : n.intValue();
        });
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(ParametroSistema param) {
        tx(em -> {
            ParametroSistemaEntity existing = em.find(ParametroSistemaEntity.class,
                                                    param.getClave());
            if (existing == null) {
                throw new EntityNotFoundException(
                        "Parámetro no encontrado (" + param.getClave() + ")");
            }

            param.setActualizado(LocalDateTime.now());
            ParametroSistemaEntity entity = mapper.toEntity(param);
            em.merge(entity);
            return null;
        });
    }
}

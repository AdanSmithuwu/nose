package com.comercialvalerio.infrastructure.persistence.impl;

import com.comercialvalerio.domain.model.Categoria;
import com.comercialvalerio.domain.repository.CategoriaRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.CategoriaEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.CategoriaMapper;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

/**
 * Implementación JPA de {@link CategoriaRepository}.
 *
 * <p>Cuando un procedimiento almacenado usa TVP, JPA no permite registrar
 * ese parámetro con {@code @StoredProcedureParameter}. En tal caso se debe
 * obtener un {@code CallableStatement} con {@code unwrap(CallableStatement.class)}
 * y asignar manualmente el {@code SQLServerDataTable}.
 */
@ApplicationScoped
public class CategoriaRepositoryImpl
        extends CrudRepository<Categoria, CategoriaEntity, Integer>
        implements CategoriaRepository {
@Inject
    CategoriaMapper mapper;

    public CategoriaRepositoryImpl() {
        super(CategoriaEntity.class);
    }
    /* -------- Lectura -------- */
    @Override
    public java.util.Optional<Categoria> findByNombre(String nombre) {
        return readOptional(em -> em.createNamedQuery("Categoria.findByNombre", CategoriaEntity.class)
                            .setParameter("nombre", nombre.toUpperCase(Locale.ROOT))
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }

    @Override
    public boolean existsByNombre(String nombre, Integer excludeId) {
        return read(em -> em.createNamedQuery("Categoria.findByNombre", CategoriaEntity.class)
                .setParameter("nombre", nombre.toUpperCase(Locale.ROOT))
                .getResultStream()
                .findFirst()
                .map(ent -> excludeId == null || !ent.getIdCategoria().equals(excludeId))
                .orElse(false));
    }
    /* -------- Escritura -------- */
    @Override
    public void save(Categoria cat) {
        tx(em -> {
            if (cat.getIdCategoria() == null) {
                StoredProcedureQuery sp = em
                        .createNamedStoredProcedureQuery("Categoria.insert");

                sp.setParameter("nombre", cat.getNombre());
                sp.setParameter("descripcion", cat.getDescripcion());
                sp.execute();
                cat.setIdCategoria((Integer) sp.getOutputParameterValue("newIdCategoria"));
            } else {
                StoredProcedureQuery sp = em
                        .createNamedStoredProcedureQuery("Categoria.update");

                sp.setParameter("idCategoria", cat.getIdCategoria());
                sp.setParameter("nombre", cat.getNombre());
                sp.setParameter("descripcion", cat.getDescripcion());
                sp.execute();
            }
            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            StoredProcedureQuery sp =
                    em.createNamedStoredProcedureQuery("Categoria.delete");
            sp.setParameter("idCategoria", id);
            sp.execute();
            return null;
        });
    }

    @Override
    public int cambiarEstado(Integer id, String nuevoEstado, boolean actualizarProductos) {
        return tx(em -> {
            StoredProcedureQuery sp = em.createNamedStoredProcedureQuery("Categoria.cambiarEstado");
            sp.setParameter("idCategoria", id);
            sp.setParameter("nuevoEstado", nuevoEstado);
            sp.setParameter("actualizarProductos", actualizarProductos);
            sp.execute();
            Integer n = (Integer) sp.getOutputParameterValue("numProductos");
            return n == null ? 0 : n;
        });
    }

    @Override
    protected CategoriaEntity toEntity(Categoria model) {
        return mapper.toEntity(model);
    }

    @Override
    protected Categoria toDomain(CategoriaEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(Categoria model, CategoriaEntity entity) {
        model.setIdCategoria(entity.getIdCategoria());
    }
}

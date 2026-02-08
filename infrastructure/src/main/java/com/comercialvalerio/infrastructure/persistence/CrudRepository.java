package com.comercialvalerio.infrastructure.persistence;

import java.util.List;
import java.util.logging.Logger;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;

/**
 * Repositorio CRUD genérico que brinda implementaciones por defecto con JPA.
 * Las subclases proveen funciones de mapeo entre modelos de dominio y
 * entidades y especifican la clase de entidad.
 */
public abstract class CrudRepository<M, E, ID> extends BaseRepository {

    private static final Logger LOG =
            Logger.getLogger(CrudRepository.class.getName());

    private final Class<E> entityClass;
    private String entityName;

    protected CrudRepository(Class<E> entityClass) {
        this.entityClass = entityClass;
        var ent = entityClass.getAnnotation(Entity.class);
        this.entityName = ent != null && !ent.name().isBlank()
                ? ent.name()
                : entityClass.getSimpleName();
    }

    /** Convierte una entidad JPA al modelo de dominio. */
    protected abstract M toDomain(E entity);

    /** Convierte el modelo de dominio a una entidad JPA. */
    protected abstract E toEntity(M model);

    /** Copia el id generado desde la entidad al modelo luego de guardar. */
    protected abstract void assignId(M model, E entity);

    /** Nombre del campo identificador para esta entidad. */
    protected String idField() {
        return "id" + entityName;
    }

    private List<E> listAll(EntityManager em) {
        return em.createNamedQuery(entityName + ".findAll", entityClass)
                 .getResultList();
    }

    /** Devuelve todas las filas de la entidad. */
    public List<M> findAll() {
        return read(em -> listAll(em).stream()
                                     .map(this::toDomain)
                                     .toList());
    }

    /** Busca un objeto de dominio por clave primaria. */
    public java.util.Optional<M> findById(ID id) {
        return readOptional(em -> {
            E e = em.find(entityClass, id);
            return e != null ? this.toDomain(e) : null;
        });
    }

    /**
     * Obtiene todos los objetos de dominio que coincidan con los
     * identificadores dados.
     *
     * La JPQL se arma de manera programática ya que el tipo de entidad es
     * genérico y la lista de IDs varía, siguiendo lo indicado en
     * {@code docs/persistence/jpql-guidelines.md}.
     *
     * @param ids      colección de identificadores a buscar
     * @param idField  nombre del campo identificador en la entidad
     * @return lista de objetos en el orden devuelto por JPA
     */
    public List<M> findAllByIds(java.util.Collection<ID> ids, String idField) {
        if (ids == null || ids.isEmpty()) return List.of();
        return read(em -> {
            var cb = em.getCriteriaBuilder();
            var query = cb.createQuery(entityClass);
            var root = query.from(entityClass);
            query.select(root)
                 .where(root.get(idField).in(ids));
            return em.createQuery(query)
                     .getResultStream()
                     .map(this::toDomain)
                     .toList();
        });
    }

    /** Inserta o actualiza el registro. */
    public void save(M model) {
        tx(em -> {
            E entity = toEntity(model);
            entity = em.merge(entity);
            assignId(model, entity);
            return null;
        });
    }

    /** Elimina el registro si existe. */
    public void delete(ID id) {
        tx(em -> {
            E entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            return null;
        });
    }
}

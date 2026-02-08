package com.comercialvalerio.domain.model;

/**
 * Clase base genérica para entidades identificadas por un ID.
 */
public abstract class BaseEntity<ID> {

    /** Devuelve el identificador de la entidad. */
    public abstract ID getId();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity<?> other = (BaseEntity<?>) obj;
        ID id = getId();
        Object otherId = other.getId();
        if (id == null || otherId == null) return false;
        return id.equals(otherId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(getId());
    }
}

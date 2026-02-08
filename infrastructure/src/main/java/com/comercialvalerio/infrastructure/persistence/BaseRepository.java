package com.comercialvalerio.infrastructure.persistence;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.DataAccessException;
import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.ConstraintViolationException;
import com.comercialvalerio.infrastructure.transaction.TransactionManager;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

/* Plantilla base para repositorios JPA */
public abstract class BaseRepository {
    /* Ejecuta trabajo en transacción, con rollback y traducción de excepciones */
    protected <T> T tx(Function<EntityManager, T> work) {
        if (TransactionManager.isActive()) {
            EntityManager em = TransactionManager.getEntityManager();
            try {
                return work.apply(em);
            } catch (PersistenceException pe) {
                throw translate(pe);
            }
        }
        try (TransactionManager.Tx tx = TransactionManager.begin()) {
            EntityManager em = TransactionManager.getEntityManager();
            try {
                T result = work.apply(em);
                tx.commit();
                return result;
            } catch (PersistenceException pe) {
                tx.rollback(pe);
                throw translate(pe);
            } catch (RuntimeException re) {
                tx.rollback(re);
                throw re;                       // error de lógica→propaga sin traducir
            }
        }
    }
    /* Sólo lectura (sin transacción explícita) */
    protected <T> T read(Function<EntityManager, T> query) {
        try {
            if (TransactionManager.isActive()) {
                EntityManager em = TransactionManager.getEntityManager();
                return query.apply(em);
            }
            return PersistenceManager.run(query);
        } catch (PersistenceException pe) {
            throw translate(pe);
        }
    }

    /**
     * Ejecuta una consulta de solo lectura que puede devolver un valor nulo
     * y lo envuelve en {@link Optional}.
     */
    protected <T> Optional<T> readOptional(Function<EntityManager, T> query) {
        return read(em -> Optional.ofNullable(query.apply(em)));
    }
    /* Traduce excepciones de la capa JPA a nuestra jerarquía */
    protected RuntimeException translate(PersistenceException pe) {
        var cause = pe.getCause();
        SQLException se = null;
        Throwable t = cause;
        while (t != null) {
            if (t instanceof SQLException sql) {
                se = sql;
                break;
            }
            t = t.getCause();
        }
        if (se != null)
            return translate(se);
        if (cause instanceof EntityExistsException)
            return new DuplicateEntityException("Duplicidad en BD");
        if (cause instanceof jakarta.validation.ConstraintViolationException cve)
            return new ConstraintViolationException(cve.getMessage(), cve);
        return new DataAccessException("Error de acceso a datos", pe);
    }

    /* Traduce SQLException a nuestra jerarquía */
    protected RuntimeException translate(SQLException se) {
        int code = se.getErrorCode();
        String state = se.getSQLState();
        if (state != null && state.startsWith("23"))
            return new ConstraintViolationException("Datos inválidos", se);
        if (code >= 50000)
            return new BusinessRuleViolationException(se.getMessage(), se);
        if (code == 2601 || code == 2627)
            return new DuplicateEntityException("Duplicidad en BD");
        return new DataAccessException("Error de acceso a datos", se);
    }

    /** Mapea cada fila devuelta por la consulta usando la función dada. */
    protected <E, T> List<T> map(jakarta.persistence.TypedQuery<E> query,
                                Function<E, T> mapper) {
        return query.getResultStream()
                    .map(mapper)
                    .toList();
    }

    /** Convierte los resultados crudos de {@link jakarta.persistence.Query#getResultList()} en una lista tipada. */
    public static <T> List<T> resultList(jakarta.persistence.Query query, Class<T> type) {
        List<?> list = query.getResultList();
        return list.stream().map(type::cast).toList();
    }

}

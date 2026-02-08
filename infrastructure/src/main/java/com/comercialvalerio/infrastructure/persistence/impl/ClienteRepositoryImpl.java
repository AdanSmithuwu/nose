package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.domain.exception.DuplicateEntityException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.repository.ClienteRepository;
import com.comercialvalerio.domain.repository.PedidoRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ClienteEntity;
import com.comercialvalerio.infrastructure.persistence.entity.EstadoEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ClienteMapper;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

/** Implementación JPA de {@link ClienteRepository}. */
@ApplicationScoped
public class ClienteRepositoryImpl extends BaseRepository implements ClienteRepository {
@Inject
    ClienteMapper mapper;
    @Inject
    PedidoRepository repoPed;
    /* ---------- Lectura ---------- */
    @Override
    public List<Cliente> findAll() {
        return read(em -> map(
                em.createNamedQuery("Cliente.findAll", ClienteEntity.class),
                mapper::toDomain));
    }

    @Override
    public List<Cliente> findActivos() {
        return read(em -> map(
                em.createNamedQuery("Cliente.activos", ClienteEntity.class),
                mapper::toDomain));
    }

    @Override
    public List<Cliente> findByEstado(String nombre) {
        return read(em -> map(
                em.createNamedQuery("Cliente.byEstado", ClienteEntity.class)
                        .setParameter("estado", nombre.toUpperCase(Locale.ROOT)),
                mapper::toDomain));
    }
    @Override
    public java.util.Optional<Cliente> findById(Integer id) {
        return readOptional(em -> {
            ClienteEntity e = em.find(ClienteEntity.class, id);
            return e == null ? null : mapper.toDomain(e);
        });
    }
    @Override
    public java.util.Optional<Cliente> findByDni(String dni) {
        return readOptional(em -> em.createNamedQuery("Cliente.findByDni", ClienteEntity.class)
                            .setParameter("dni", dni)
                            .getResultStream()
                            .findFirst()
                            .map(mapper::toDomain)
                            .orElse(null));
    }
    @Override
    public List<Cliente> findByNombreLike(String patron) {
        return read(em -> map(
                em.createNamedQuery("Cliente.activosByNombre", Object[].class)
                        .setParameter("patron", "%" + patron.toUpperCase(Locale.ROOT) + "%"),
                (Object[] r) -> mapper.toDomain((ClienteEntity) r[0])));
    }
    @Override
    public List<Cliente> findByTelefono(String numero) {
        return read(em -> map(
                em.createNamedQuery("Cliente.activosByTelefono", Object[].class)
                        .setParameter("tel", numero),
                (Object[] r) -> mapper.toDomain((ClienteEntity) r[0])));
    }
    @Override
    public List<Cliente> findByRangoRegistro(LocalDate d, LocalDate h) {
        return read(em -> map(
                em.createNamedQuery("Cliente.activosByRangoRegistro", ClienteEntity.class)
                        .setParameter("d", d)
                        .setParameter("h", h),
                mapper::toDomain));
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(Cliente cli) {
        tx(em -> {
            // 1) Reglas de dominio: validar duplicado de DNI
            boolean dniDup = em.createNamedQuery(
                    "Persona.countByDniExcludingId", Long.class)
                .setParameter("dni", cli.getDni())
                .setParameter("id",  cli.getIdPersona())
                .getSingleResult() > 0;
            if (dniDup) {
                throw new DuplicateEntityException(
                    "Ya existe una persona con DNI " + cli.getDni());
            }

            if (cli.getIdPersona() == null) {
                // 2) Alta completa: invocar SP
                StoredProcedureQuery sp = em
                    .createNamedStoredProcedureQuery("Cliente.registrar");

                sp.setParameter("nombres",   cli.getNombres());
                sp.setParameter("apellidos", cli.getApellidos());
                sp.setParameter("dni",       cli.getDni());
                sp.setParameter("telefono",  cli.getTelefono());
                sp.setParameter("direccion", cli.getDireccion());
                // el estado ya fue seteado en el modelo Cliente (no nulo)
                sp.setParameter("idEstado",  cli.getEstado().getIdEstado());

                sp.execute();
                Integer newId = (Integer) sp.getOutputParameterValue("newIdPersona");
                cli.setIdPersona(newId);

            } else {
                // 3) Actualización de datos de Persona y Cliente
                em.createNamedQuery("Persona.updateDatos")
                  .setParameter("n",  cli.getNombres())
                  .setParameter("a",  cli.getApellidos())
                  .setParameter("d",  cli.getDni())
                  .setParameter("t",  cli.getTelefono())
                  .setParameter("id", cli.getIdPersona())
                  .executeUpdate();

                em.createNamedQuery("Cliente.updateDireccion")
                  .setParameter("dir", cli.getDireccion())
                  .setParameter("id",  cli.getIdPersona())
                  .executeUpdate();
            }

            return null;
        });
    }
    @Override
    public void delete(Integer id) {
        tx(em -> {
            ClienteEntity e = em.find(ClienteEntity.class, id);
            if (e == null)
                throw new EntityNotFoundException("Cliente no encontrado (id=" + id + ")");
            if (repoPed.existsByCliente(id))
                throw new BusinessRuleViolationException("Cliente posee pedidos; no se puede eliminar");
            if (!e.getTransacciones().isEmpty())
                throw new BusinessRuleViolationException("Cliente posee transacciones; no se puede eliminar");
            em.remove(e.getPersona());
            return null;
        });
    }

    @Override
    public void updateEstado(Integer id, String estado) {
        tx(em -> {
            EstadoEntity nuevo = em.createNamedQuery("Estado.findByModuloAndNombre", EstadoEntity.class)
                    .setParameter("m", "Persona")
                    .setParameter("n", estado)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (nuevo == null)
                throw new EntityNotFoundException("Estado no encontrado");

            ClienteEntity cli = em.find(ClienteEntity.class, id);
            if (cli == null)
                throw new EntityNotFoundException("Cliente no encontrado");

            cli.getPersona().setEstado(nuevo);
            em.merge(cli);
            return null;
        });
    }
}

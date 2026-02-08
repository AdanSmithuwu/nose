package com.comercialvalerio.infrastructure.persistence.impl;
import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.domain.model.BitacoraLogin;
import com.comercialvalerio.domain.repository.BitacoraLoginRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.infrastructure.persistence.entity.BitacoraLoginEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.BitacoraLoginMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.StoredProcedureQuery;

/** Implementación JPA de {@link BitacoraLoginRepository}. */
@ApplicationScoped
public class BitacoraLoginRepositoryImpl
        extends BaseRepository implements BitacoraLoginRepository {
@Inject
    BitacoraLoginMapper mapper;
    /* ---------- Lectura ---------- */
    @Override
    public List<BitacoraLogin> findByEmpleado(Integer idEmpleado) {
        return read(em -> map(
                em.createNamedQuery(
                        "BitacoraLogin.byEmpleado", BitacoraLoginEntity.class)
                        .setParameter("idEmpleado", idEmpleado),
                mapper::toDomain));
    }
    @Override
    public List<BitacoraLogin> findByRangoFecha(LocalDateTime desde,
                                                LocalDateTime hasta) {
        return findByRangoFecha(desde, hasta, null);
    }

    @Override
    public List<BitacoraLogin> findByRangoFecha(LocalDateTime desde,
                                                LocalDateTime hasta,
                                                Boolean exitoso) {
        return read(em -> map(
                em.createNamedQuery("BitacoraLogin.byRangoFechaExitoso",
                                     BitacoraLoginEntity.class)
                        .setParameter("desde", desde)
                        .setParameter("hasta", hasta)
                        .setParameter("exitoso", exitoso),
                mapper::toDomain));
    }

    @Override
    public boolean existsBitacoraByEmpleado(Integer idEmpleado) {
        return read(em -> em.createNamedQuery(
                        "BitacoraLogin.countByEmpleado",
                        Long.class)
                .setParameter("idEmpleado", idEmpleado)
                .getSingleResult() > 0);
    }
    /* ---------- Escritura ---------- */
    @Override
    public void save(BitacoraLogin evento) {
        tx(em -> {
            BitacoraLoginEntity ent = mapper.toEntity(evento);
            ent = em.merge(ent);                    // insertar/actualizar
            evento.setIdBitacora(ent.getIdBitacora());
            return null;
        });
    }

    @Override
    public void depurarAntiguos(LocalDateTime hasta) {
        tx(em -> {
            StoredProcedureQuery sp =
                    em.createNamedStoredProcedureQuery("BitacoraLogin.depurarAntiguos");
            sp.setParameter("maxFecha", hasta);
            sp.execute();
            return null;
        });
    }
}

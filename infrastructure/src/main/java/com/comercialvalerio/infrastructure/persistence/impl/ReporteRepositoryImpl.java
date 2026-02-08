package com.comercialvalerio.infrastructure.persistence.impl;
import java.util.List;

import com.comercialvalerio.domain.model.Reporte;
import com.comercialvalerio.domain.repository.ReporteRepository;
import com.comercialvalerio.infrastructure.persistence.CrudRepository;
import com.comercialvalerio.infrastructure.persistence.entity.ReporteEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.ReporteMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ReporteRepositoryImpl
        extends CrudRepository<Reporte, ReporteEntity, Integer>
        implements ReporteRepository {
@Inject
    ReporteMapper mapper;

    public ReporteRepositoryImpl() {
        super(ReporteEntity.class);
    }

    /* -------------------- Lectura -------------------- */

    @Override
    public List<Reporte> findByEmpleado(Integer idEmpleado) {
        return read(em -> map(
                em.createNamedQuery("Reporte.findByEmpleado", ReporteEntity.class)
                        .setParameter("empId", idEmpleado),
                mapper::toDomain));
    }

    @Override
    public java.util.Optional<Reporte> findMensual(int anio, int mes) {
        java.time.LocalDate desde = java.time.LocalDate.of(anio, mes, 1);
        java.time.LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        return readOptional(em -> em.createNamedQuery("Reporte.findMensual", ReporteEntity.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    public java.util.Optional<Reporte> findDiario(java.time.LocalDate fecha) {
        return readOptional(em -> em.createNamedQuery("Reporte.findDiario", ReporteEntity.class)
                .setParameter("fecha", fecha)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain)
                .orElse(null));
    }

    @Override
    protected ReporteEntity toEntity(Reporte model) {
        return mapper.toEntity(model);
    }

    @Override
    protected Reporte toDomain(ReporteEntity entity) {
        return mapper.toDomain(entity);
    }

    @Override
    protected void assignId(Reporte model, ReporteEntity entity) {
        model.setIdReporte(entity.getIdReporte());
    }
}

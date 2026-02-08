package com.comercialvalerio.infrastructure.persistence.impl.report;

import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.domain.repository.report.ReporteViewRepository;
import com.comercialvalerio.infrastructure.persistence.BaseRepository;
import com.comercialvalerio.domain.view.report.PagoMetodoDia;
import com.comercialvalerio.domain.view.report.ResumenCategoria;
import com.comercialvalerio.domain.view.report.ResumenModalidad;
import com.comercialvalerio.domain.view.report.RotacionProducto;
import com.comercialvalerio.domain.view.report.TransaccionesDia;
import com.comercialvalerio.infrastructure.persistence.entity.report.PagoMetodoDiaEntity;
import com.comercialvalerio.infrastructure.persistence.entity.report.ResumenCategoriaEntity;
import com.comercialvalerio.infrastructure.persistence.entity.report.ResumenModalidadEntity;
import com.comercialvalerio.infrastructure.persistence.entity.report.RotacionProductoEntity;
import com.comercialvalerio.infrastructure.persistence.entity.report.TransaccionesDiaEntity;
import com.comercialvalerio.infrastructure.persistence.mapper.report.ReporteViewMapper;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReporteViewRepositoryImpl extends BaseRepository
        implements ReporteViewRepository<TransaccionesDia, PagoMetodoDia, ResumenModalidad, RotacionProducto, ResumenCategoria> {

    @jakarta.inject.Inject
    ReporteViewMapper mapper;

    @Override
    public DiarioData<TransaccionesDia, PagoMetodoDia> diario(LocalDate fecha) {
        TransaccionesDiaEntity d = read(em -> em.find(TransaccionesDiaEntity.class, fecha));
        List<PagoMetodoDiaEntity> pagos = read(em -> em.createNamedQuery("PagoMetodoDia.byFecha", PagoMetodoDiaEntity.class)
                .setParameter("d", fecha)
                .getResultList());
        TransaccionesDia dto = d == null ? null : mapper.toDomain(d);
        List<PagoMetodoDia> list = pagos == null ? java.util.List.of() : pagos.stream().map(mapper::toDomain).toList();
        return new DiarioData<>(dto, list);
    }

    @Override
    public List<TransaccionesDia> rango(LocalDate desde, LocalDate hasta) {
        return read(em -> em.createNamedQuery("TransaccionesDia.rango", TransaccionesDiaEntity.class)
                .setParameter("d", desde)
                .setParameter("h", hasta)
                .getResultList()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public ResumenModalidad resumenMensual(int anio, int mes) {
        ResumenModalidadEntity e = read(em -> em.createNamedQuery("ResumenModalidad.byMes", ResumenModalidadEntity.class)
                .setParameter("a", anio)
                .setParameter("m", mes)
                .getResultStream()
                .findFirst()
                .orElse(null));
        return e == null ? null : mapper.toDomain(e);
    }

    public List<ResumenCategoria> resumenMensualCategoria(int anio, int mes) {
        return read(em -> em.createNamedQuery("ResumenCategoria.byMes", ResumenCategoriaEntity.class)
                .setParameter("a", anio)
                .setParameter("m", mes)
                .getResultList()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<RotacionProducto> rotacion(java.time.LocalDate desde,
                                           java.time.LocalDate hasta,
                                           Integer top) {
        return read(em -> em.createNamedQuery("RotacionProducto.byRango", RotacionProductoEntity.class)
                .setParameter(1, desde)
                .setParameter(2, hasta)
                .setParameter(3, top)
                .getResultList()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

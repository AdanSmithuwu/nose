package com.comercialvalerio.application.service.impl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import com.comercialvalerio.application.dto.ReporteCreateDto;
import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.application.dto.ReportePdfDto;
import com.comercialvalerio.application.dto.report.ResumenDiarioDto;
import com.comercialvalerio.application.dto.report.ResumenMensualDto;
import com.comercialvalerio.application.dto.report.RotacionDto;
import com.comercialvalerio.application.mapper.ReporteDtoMapper;
import com.comercialvalerio.application.mapper.report.ReporteViewDtoMapper;
import com.comercialvalerio.application.service.ReporteService;
import com.comercialvalerio.common.transaction.Transactional;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.application.service.util.ServiceUtils;
import com.comercialvalerio.application.service.util.SecurityChecks;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.Reporte;
import com.comercialvalerio.domain.model.TipoReporte;
import com.comercialvalerio.domain.security.RequestContext;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.ReporteRepository;
import com.comercialvalerio.domain.repository.report.ReporteViewRepository;
import com.comercialvalerio.domain.view.report.TransaccionesDia;
import com.comercialvalerio.domain.view.report.PagoMetodoDia;
import com.comercialvalerio.domain.view.report.ResumenModalidad;
import com.comercialvalerio.domain.view.report.RotacionProducto;
import com.comercialvalerio.domain.view.report.ResumenCategoria;
import com.comercialvalerio.domain.service.ReporteGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Transactional
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository repoRep;
    private final EmpleadoRepository repoEmp;
    private final ReporteGenerator generator;
    private final ReporteViewRepository<
            TransaccionesDia,
            PagoMetodoDia,
            ResumenModalidad,
            RotacionProducto,
            ResumenCategoria> viewRepo;
    @Inject ReporteDtoMapper mapper;
    @Inject ReporteViewDtoMapper viewMapper;

    @Inject
    public ReporteServiceImpl(
            ReporteRepository repoRep,
            EmpleadoRepository repoEmp,
            ReporteGenerator generator,
            ReporteViewRepository<
                    TransaccionesDia,
                    PagoMetodoDia,
                    ResumenModalidad,
                    RotacionProducto,
                    ResumenCategoria> viewRepo){
        this.repoRep = repoRep;
        this.repoEmp = repoEmp;
        this.generator = generator;
        this.viewRepo = viewRepo;
    }

    public ReporteServiceImpl(ReporteRepository repoRep,
                              EmpleadoRepository repoEmp,
                              ReporteGenerator generator){
        this(repoRep, repoEmp, generator, null);
    }

    @Override public List<ReporteDto> listar(){
        return ServiceUtils.mapList(repoRep.findAll(), mapper::toDto);
    }

    @Override public List<ReporteDto> listarPorEmpleado(Integer idEmp){
        return ServiceUtils.mapList(repoRep.findByEmpleado(idEmp), mapper::toDto);
    }

    @Override public ReporteDto crear(ReporteCreateDto dto){
        if (dto.idEmpleado() == null)
            throw new IllegalArgumentException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(dto.idEmpleado()), "Empleado inexistente");
        Reporte r = new Reporte();
        r.setTipoReporte(
                TipoReporte.fromNombre(
                        dto.tipoReporte().getNombre()));
        r.setEmpleado(emp);
        r.setDesde(dto.desde());
        r.setHasta(dto.hasta());
        r.setFiltros(dto.filtros());
        r.setBytesPdf(dto.pdf());
        r.setFechaGeneracion(LocalDateTime.now());
        repoRep.save(r);
        return mapper.toDto(r);
    }
    @Override
    public ReportePdfDto generarDiario(LocalDate fecha) {
        SecurityChecks.requireAdminRole();
        if (fecha == null)
            throw new BusinessRuleViolationException("fecha obligatoria");
        byte[] pdf = generator.generarReporteDiario(fecha);
        // opcionalmente registrar meta-dato de descarga
        Reporte meta = new Reporte();
        meta.setTipoReporte(TipoReporte.DIARIO);
        meta.setDesde(fecha);
        meta.setHasta(fecha);
        meta.setBytesPdf(pdf);
        meta.setFechaGeneracion(LocalDateTime.now());
        Integer id = RequestContext.idEmpleado();
        if (id == null)
            throw new BusinessRuleViolationException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(id), "Empleado inexistente");
        meta.setEmpleado(emp);
        repoRep.save(meta);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    @Override
    public ReportePdfDto guardarDiario(LocalDate fecha) {
        SecurityChecks.requireAdminRole();
        if (fecha == null)
            throw new BusinessRuleViolationException("fecha obligatoria");
        byte[] pdf = generator.generarReporteDiario(fecha);
        Reporte r = repoRep.findDiario(fecha).orElseGet(Reporte::new);
        r.setTipoReporte(TipoReporte.DIARIO);
        r.setDesde(fecha);
        r.setHasta(fecha);
        r.setBytesPdf(pdf);
        r.setFechaGeneracion(LocalDateTime.now());
        Integer idEmp = RequestContext.idEmpleado();
        if (idEmp == null)
            throw new BusinessRuleViolationException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(idEmp), "Empleado inexistente");
        r.setEmpleado(emp);
        repoRep.save(r);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    /* ------------------ NUEVO: generar MENSUAL ---------------- */
    @Override
    public ReportePdfDto generarMensual(int anio, int mes, boolean incluirResumen) {
        SecurityChecks.requireAdminRole();
        if (mes < 1 || mes > 12)
            throw new BusinessRuleViolationException("mes debe estar entre 1 y 12");
        byte[] pdf = generator.generarReporteMensual(anio, mes, incluirResumen);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    @Override
    public ReportePdfDto guardarMensual(int anio, int mes, boolean incluirResumen) {
        SecurityChecks.requireAdminRole();
        if (mes < 1 || mes > 12)
            throw new BusinessRuleViolationException("mes debe estar entre 1 y 12");
        byte[] pdf = generator.generarReporteMensual(anio, mes, incluirResumen);
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        Reporte r = repoRep.findMensual(anio, mes).orElseGet(Reporte::new);
        r.setTipoReporte(TipoReporte.MENSUAL);
        r.setDesde(desde);
        r.setHasta(hasta);
        r.setBytesPdf(pdf);
        r.setFechaGeneracion(LocalDateTime.now());
        Integer idEmp = RequestContext.idEmpleado();
        if (idEmp == null)
            throw new BusinessRuleViolationException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(idEmp), "Empleado inexistente");
        r.setEmpleado(emp);
        repoRep.save(r);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    @Override
    public ReporteDto buscarDiario(LocalDate fecha) {
        return repoRep.findDiario(fecha)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public ReporteDto buscarMensual(int anio, int mes) {
        return repoRep.findMensual(anio, mes)
                .map(mapper::toDto)
                .orElse(null);
    }

    /* ------------------ NUEVO: generar ROTACIÓN --------------- */
    @Override
    public ReportePdfDto generarRotacion(java.time.LocalDate desde, java.time.LocalDate hasta, Integer top) {
        SecurityChecks.requireAdminRole();
        byte[] pdf = generator.generarReporteRotacion(desde, hasta, top);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    @Override
    public ReportePdfDto guardarRotacion(java.time.LocalDate desde, java.time.LocalDate hasta, Integer top) {
        SecurityChecks.requireAdminRole();
        byte[] pdf = generator.generarReporteRotacion(desde, hasta, top);
        Reporte r = new Reporte();
        r.setTipoReporte(TipoReporte.ROTACION);
        r.setDesde(desde);
        r.setHasta(hasta);
        r.setBytesPdf(pdf);
        r.setFechaGeneracion(LocalDateTime.now());
        Integer idEmp = RequestContext.idEmpleado();
        if (idEmp == null)
            throw new BusinessRuleViolationException("idEmpleado obligatorio");
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(idEmp), "Empleado inexistente");
        r.setEmpleado(emp);
        repoRep.save(r);
        return new ReportePdfDto(Base64.getEncoder().encodeToString(pdf));
    }

    @Override
    public byte[] descargarPdf(Integer idReporte) {
        Reporte r = ServiceChecks.requireFound(
                repoRep.findById(idReporte), "Reporte inexistente");
        return r.getBytesPdf();
    }

    // ----- datos de vistas -----
    @Override
    public ResumenDiarioDto datosDiario(LocalDate fecha) {
        var d = viewRepo.diario(fecha);
        if (d == null || d.resumen() == null)
            return new ResumenDiarioDto(
                    fecha,
                    0,
                    java.math.BigDecimal.ZERO,
                    0,
                    0,
                    java.math.BigDecimal.ZERO,
                    java.math.BigDecimal.ZERO,
                    java.util.List.of());
        return viewMapper.toDto(d.resumen(), d.pagos());
    }

    @Override
    public ResumenMensualDto datosMensual(int anio, int mes) {
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        var dias = viewRepo.rango(desde, hasta).stream()
                .map(viewMapper::toDiaDto)
                .toList();
        var categorias = viewRepo.resumenMensualCategoria(anio, mes).stream()
                .map(viewMapper::toDto)
                .toList();
        var resumen = viewRepo.resumenMensual(anio, mes);
        return viewMapper.toDto(resumen, dias, categorias);
    }

    @Override
    public List<RotacionDto> datosRotacion(java.time.LocalDate desde, java.time.LocalDate hasta, Integer top) {
        return viewRepo.rotacion(desde, hasta, top).stream()
                .map(viewMapper::toDto)
                .toList();
    }
}

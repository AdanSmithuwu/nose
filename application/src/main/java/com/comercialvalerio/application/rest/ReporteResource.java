package com.comercialvalerio.application.rest;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.application.dto.ReporteCreateDto;
import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.application.dto.ReportePdfDto;
import com.comercialvalerio.application.dto.report.ResumenDiarioDto;
import com.comercialvalerio.application.dto.report.ResumenMensualDto;
import com.comercialvalerio.application.dto.report.RotacionDto;
import com.comercialvalerio.application.service.ReporteService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;

@Path("/reportes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReporteResource implements ReporteResourceApi {

    @Inject
    ReporteService svc;

    /* ---- consultas ---- */
    @GET
    @Override
    public List<ReporteDto> listar() {
        return svc.listar();
    }

    @GET
    @Path("/empleado/{id}")
    @Override
    public List<ReporteDto> listarPorEmpleado(@PathParam("id") @NotNull Integer id) {
        return svc.listarPorEmpleado(id);
    }

    /* ---- creación manual (subir PDF) ---- */
    @POST
    @Override
    public ReporteDto crear(ReporteCreateDto dto) {
        return svc.crear(dto);
    }

    /* ---- generación automática ---- */

    /* POST /reportes/diario  { "fecha":"2025-05-28" } */
    @POST
    @Path("/diario")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto generarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fechaQuery,
                                       ReporteResourceApi.FechaDto body) {
        LocalDate fecha = fechaQuery != null ? fechaQuery
                              : body != null ? body.fecha() : null;
        return svc.generarDiario(fecha);
    }

    @POST
    @Path("/diario/guardar")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto guardarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fechaQuery,
                                       ReporteResourceApi.FechaDto body) {
        LocalDate fecha = fechaQuery != null ? fechaQuery
                              : body != null ? body.fecha() : null;
        return svc.guardarDiario(fecha);
    }

    @GET
    @Path("/diario/buscar")
    @RolesAllowed("Administrador")
    @Override
    public ReporteDto buscarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha) {
        return svc.buscarDiario(fecha);
    }

    /* POST /reportes/mensual  { "anio":2025, "mes":5 } */
    @POST
    @Path("/mensual")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto generarMensual(ReporteResourceApi.MesDto body) {
        return svc.generarMensual(body.anio(), body.mes(), body.incluirResumen());
    }

    @POST
    @Path("/mensual/guardar")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto guardarMensual(ReporteResourceApi.MesDto body) {
        return svc.guardarMensual(body.anio(), body.mes(), body.incluirResumen());
    }

    @GET
    @Path("/mensual/buscar")
    @RolesAllowed("Administrador")
    @Override
    public ReporteDto buscarMensual(@jakarta.ws.rs.QueryParam("anio") int anio,
                                   @jakarta.ws.rs.QueryParam("mes") int mes) {
        return svc.buscarMensual(anio, mes);
    }

    /* POST /reportes/rotacion  { "anio":2025, "mes":5 } */
    @POST
    @Path("/rotacion")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto generarRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desdeQuery,
                                         @jakarta.ws.rs.QueryParam("hasta") LocalDate hastaQuery,
                                         ReporteResourceApi.RangoDto body) {
        LocalDate desde = desdeQuery != null ? desdeQuery
                               : body != null ? body.desde() : null;
        LocalDate hasta = hastaQuery != null ? hastaQuery
                               : body != null ? body.hasta() : null;
        Integer top = body != null ? body.top() : null;
        return svc.generarRotacion(desde, hasta, top);
    }

    @POST
    @Path("/rotacion/guardar")
    @RolesAllowed("Administrador")
    @Override
    public ReportePdfDto guardarRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desdeQuery,
                                         @jakarta.ws.rs.QueryParam("hasta") LocalDate hastaQuery,
                                         ReporteResourceApi.RangoDto body) {
        LocalDate desde = desdeQuery != null ? desdeQuery
                               : body != null ? body.desde() : null;
        LocalDate hasta = hastaQuery != null ? hastaQuery
                               : body != null ? body.hasta() : null;
        Integer top = body != null ? body.top() : null;
        return svc.guardarRotacion(desde, hasta, top);
    }

    /* ----- datos para vistas previas ----- */
    @GET
    @Path("/diario")
    @Override
    public ResumenDiarioDto datosDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha) {
        return svc.datosDiario(fecha);
    }

    @GET
    @Path("/mensual")
    @Override
    public ResumenMensualDto datosMensual(@jakarta.ws.rs.QueryParam("anio") int anio,
                                          @jakarta.ws.rs.QueryParam("mes") int mes) {
        return svc.datosMensual(anio, mes);
    }

    @GET
    @Path("/rotacion")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<RotacionDto> datosRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desde,
                                           @jakarta.ws.rs.QueryParam("hasta") LocalDate hasta,
                                           @jakarta.ws.rs.QueryParam("top") Integer top) {
        return svc.datosRotacion(desde, hasta, top);
    }

    /* ---- descarga de PDF ---- */
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    @Override
    public jakarta.ws.rs.core.Response descargarPdf(@PathParam("id") @NotNull Integer id) {
        byte[] pdf = svc.descargarPdf(id);
        String disposition = "attachment; filename=reporte-" + id + ".pdf";
        return jakarta.ws.rs.core.Response.ok(pdf)
                .header("Content-Disposition", disposition)
                .build();
    }

    /* DTOs “inline” para payloads muy simples */
}

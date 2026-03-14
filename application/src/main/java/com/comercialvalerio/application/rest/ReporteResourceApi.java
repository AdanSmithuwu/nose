package com.comercialvalerio.application.rest;
import java.time.LocalDate;
import java.util.List;
import com.comercialvalerio.application.dto.ReporteCreateDto;
import com.comercialvalerio.application.dto.ReporteDto;
import com.comercialvalerio.application.dto.ReportePdfDto;
import com.comercialvalerio.application.dto.report.ResumenDiarioDto;
import com.comercialvalerio.application.dto.report.ResumenMensualDto;
import com.comercialvalerio.application.dto.report.RotacionDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.constraints.NotNull;

@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ReporteResourceApi {
    @GET
    public List<ReporteDto> listar();
    @GET
    @Path("/empleado/{id}")
    public List<ReporteDto> listarPorEmpleado(@PathParam("id") @NotNull Integer id);
    @POST
    public ReporteDto crear(ReporteCreateDto dto);
    @POST
    @Path("/diario")
    public ReportePdfDto generarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha,
                                       FechaDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto generarDiario(LocalDate fecha) {
        return generarDiario(fecha, new FechaDto(fecha));
    }
    @POST
    @Path("/diario/guardar")
    public ReportePdfDto guardarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha,
                                       FechaDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto guardarDiario(LocalDate fecha) {
        return guardarDiario(fecha, new FechaDto(fecha));
    }
    @POST
    @Path("/mensual")
    public ReportePdfDto generarMensual(MesDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto generarMensual(int anio, int mes, boolean incluirResumen) {
        return generarMensual(new MesDto(anio, mes, incluirResumen));
    }
    @POST
    @Path("/mensual/guardar")
    public ReportePdfDto guardarMensual(MesDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto guardarMensual(int anio, int mes, boolean incluirResumen) {
        return guardarMensual(new MesDto(anio, mes, incluirResumen));
    }
    @GET
    @Path("/mensual/buscar")
    public ReporteDto buscarMensual(@jakarta.ws.rs.QueryParam("anio") int anio,
                                   @jakarta.ws.rs.QueryParam("mes") int mes);
    @GET
    @Path("/diario/buscar")
    public ReporteDto buscarDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha);
    @POST
    @Path("/rotacion")
    public ReportePdfDto generarRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desde,
                                         @jakarta.ws.rs.QueryParam("hasta") LocalDate hasta,
                                         RangoDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto generarRotacion(LocalDate desde, LocalDate hasta, Integer top) {
        return generarRotacion(desde, hasta, new RangoDto(desde, hasta, top));
    }
    @POST
    @Path("/rotacion/guardar")
    public ReportePdfDto guardarRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desde,
                                         @jakarta.ws.rs.QueryParam("hasta") LocalDate hasta,
                                         RangoDto body);
    /** Sobrecarga de conveniencia para proxies de UI. */
    default ReportePdfDto guardarRotacion(LocalDate desde, LocalDate hasta, Integer top) {
        return guardarRotacion(desde, hasta, new RangoDto(desde, hasta, top));
    }
    @GET
    @Path("/diario")
    public ResumenDiarioDto datosDiario(@jakarta.ws.rs.QueryParam("fecha") LocalDate fecha);
    @GET
    @Path("/mensual")
    public ResumenMensualDto datosMensual(@jakarta.ws.rs.QueryParam("anio") int anio, @jakarta.ws.rs.QueryParam("mes") int mes);
    @GET
    @Path("/rotacion")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RotacionDto> datosRotacion(@jakarta.ws.rs.QueryParam("desde") LocalDate desde,
                                           @jakarta.ws.rs.QueryParam("hasta") LocalDate hasta,
                                           @jakarta.ws.rs.QueryParam("top") Integer top);
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public jakarta.ws.rs.core.Response descargarPdf(@PathParam("id") @NotNull Integer id);
    public record FechaDto(LocalDate fecha) {}
    public record MesDto(int anio, int mes, boolean incluirResumen) {}
    public record RangoDto(LocalDate desde, LocalDate hasta, Integer top) {}
}

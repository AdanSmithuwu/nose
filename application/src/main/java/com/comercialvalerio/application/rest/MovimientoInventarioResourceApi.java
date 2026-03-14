package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;
import com.comercialvalerio.application.dto.MovimientoInventarioCreateDto;
import com.comercialvalerio.application.dto.MovimientoInventarioDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/movimientos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MovimientoInventarioResourceApi {
    @GET
    @Path("/producto/{idProd}")
    List<MovimientoInventarioDto> listarPorProducto(@PathParam("idProd") @NotNull Integer id);

    @GET
    List<MovimientoInventarioDto> listarPorRango(
            @QueryParam("d") LocalDateTime d,
            @QueryParam("h") LocalDateTime h);

    @POST
    MovimientoInventarioDto registrar(@Valid MovimientoInventarioCreateDto dto);
}

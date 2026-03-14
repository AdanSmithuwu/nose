package com.comercialvalerio.application.rest;
import java.time.LocalDateTime;
import java.util.List;
import com.comercialvalerio.application.dto.MotivoDto;
import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.application.dto.VentaDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface VentaResourceApi {
    @GET
    public List<VentaDto> listar();
    @GET @Path("/rango")
    public List<VentaDto> listarPorRango(@QueryParam("d") LocalDateTime d, @QueryParam("h") LocalDateTime h);
    @GET @Path("/cliente/{id}")
    public List<VentaDto> listarPorCliente(@PathParam("id") @NotNull Integer id);
    @GET @Path("{id}")
    public VentaDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public VentaDto crear(@Valid VentaCreateDto dto);
    @PUT @Path("{id}/cancelar")
    public Response cancelar( @PathParam("id") @NotNull Integer id, @Valid MotivoDto body );
}

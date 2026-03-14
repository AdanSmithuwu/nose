package com.comercialvalerio.application.rest;
import java.net.URI;
import java.util.List;

import com.comercialvalerio.application.dto.MetodoPagoCreateDto;
import com.comercialvalerio.application.dto.MetodoPagoDto;
import com.comercialvalerio.application.service.MetodoPagoService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import com.comercialvalerio.application.rest.BaseResource;

@Path("/metodos-pago")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MetodoPagoResource extends BaseResource implements MetodoPagoResourceApi {

    @Inject
    private MetodoPagoService svc;

    @Context
    private UriInfo uriInfo;

    /*
     * GET /metodos-pago           → lista completa 
     * GET /metodos-pago?nombre=X → busca único 
     */
    @GET
    @Override
    public Response listar(
        @QueryParam("nombre") String nombre
    ) {
        if (nombre != null && !nombre.isBlank()) {
            MetodoPagoDto one = svc.buscarPorNombre(nombre);
            return ok(one);
        } else {
            List<MetodoPagoDto> all = svc.listar();
            return ok(all);
        }
    }

    @GET @Path("{id}")
    @Override
    public Response obtener(@PathParam("id") @NotNull Integer id) {
        MetodoPagoDto dto = svc.obtener(id);
        return ok(dto);
    }

    @POST
    @Override
    public Response crear(
        @Valid MetodoPagoCreateDto dto
    ) {
        MetodoPagoDto created = svc.crear(dto);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(created.idMetodoPago().toString())
                              .build();
        return created(location, created);
    }

    @PUT @Path("{id}")
    @Override
    public Response actualizar(@PathParam("id") @NotNull Integer id,
                           @Valid MetodoPagoCreateDto dto) {
        MetodoPagoDto updated = svc.actualizar(id, dto);
        return ok(updated);
    }

    @DELETE @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }
}

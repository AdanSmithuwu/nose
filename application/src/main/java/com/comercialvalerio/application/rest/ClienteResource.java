package com.comercialvalerio.application.rest;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.ClienteDto;
import com.comercialvalerio.application.dto.HistorialDto;
import com.comercialvalerio.application.rest.BaseResource;
import com.comercialvalerio.application.service.ClienteService;
import com.comercialvalerio.application.service.HistorialService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@Path("/clientes")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource extends BaseResource implements ClienteResourceApi {

    @Inject
    private ClienteService svc;
    @Inject
    private HistorialService historialService;

    @Context
    private UriInfo uriInfo;

    /**
     * Devuelve todos los clientes sin importar su estado.
     */
    @GET
    @Override
    public Response listar(@QueryParam("nombre") String nombre,
                           @QueryParam("telefono") String telefono) {
        boolean byNombre   = nombre != null && !nombre.isBlank();
        boolean byTelefono = telefono != null && !telefono.isBlank();

        if (byNombre) {
            List<ClienteDto> r = svc.buscarPorNombre(nombre);
            return ok(r);
        } else if (byTelefono) {
            List<ClienteDto> r = svc.buscarPorTelefono(telefono);
            return ok(r);
        } else {
            List<ClienteDto> todos = svc.listar();
            return ok(todos);
        }
    }

    @GET
    @Path("activos")
    @Override
    public Response listarActivos() {
        List<ClienteDto> lista = svc.listarActivos();
        return ok(lista);
    }

    @GET
    @Path("estado/{nombre}")
    @Override
    public Response findByEstado(@PathParam("nombre") @NotBlank String nombre) {
        List<ClienteDto> lista = svc.findByEstado(nombre);
        return ok(lista);
    }

    @GET
    @Path("{id}")
    @Override
    public Response obtener(@PathParam("id") @NotNull Integer id) {
        ClienteDto dto = svc.obtener(id);
        return ok(dto);
    }

    @GET
    @Path("dni/{dni}")
    @Override
    public Response obtenerPorDni(@PathParam("dni") @NotBlank String dni) {
        ClienteDto dto = svc.obtenerPorDni(dni);
        return ok(dto);
    }

    @GET
    @Path("rango")
    @Override
    public Response listarPorRangoRegistro(
        @QueryParam("desde") @NotNull LocalDate desde,
        @QueryParam("hasta") @NotNull LocalDate hasta
    ) {
        List<ClienteDto> lista = svc.listarPorRangoRegistro(desde, hasta);
        return ok(lista);
    }

    @POST
    @Override
    public Response registrar(@Valid ClienteCreateDto dto) {
        ClienteDto creado = svc.registrar(dto);
        URI location = uriInfo.getAbsolutePathBuilder()
                              .path(creado.idPersona().toString())
                              .build();
        return created(location, creado);
    }

    @PUT
    @Path("{id}")
    @Override
    public Response actualizar(
        @PathParam("id") @NotNull Integer id,
        @Valid ClienteCreateDto dto
    ) {
        ClienteDto actualizado = svc.actualizar(id, dto);
        return ok(actualizado);
    }

    @DELETE
    @Path("{id}")
    @Override
    public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id);
        return noContent();
    }

    @PUT
    @Path("{id}/estado")
    @Override
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id,
                                  @Valid CambiarEstadoDto dto) {
        svc.cambiarEstado(id, dto);
        return noContent();
    }

    @GET
    @Path("{id}/historial")
    @Override
    public List<HistorialDto> historialPorCliente(@PathParam("id") @NotNull Integer id,
                                                  @QueryParam("d") java.time.LocalDateTime desde,
                                                  @QueryParam("h") java.time.LocalDateTime hasta,
                                                  @QueryParam("cat") Integer categoria,
                                                  @QueryParam("prod") Integer producto) {
        svc.obtener(id);
        return historialService.historialPorCliente(id, desde, hasta, categoria, producto);
    }

    @GET
    @Path("{id}/eliminable")
    @Override
    public Response obtenerDependencias(@PathParam("id") @NotNull Integer id) {
        List<String> deps = svc.obtenerDependencias(id);
        return ok(deps);
    }
}

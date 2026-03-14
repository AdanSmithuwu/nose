package com.comercialvalerio.application.rest;
import java.time.LocalDate;
import java.util.List;

import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.ClienteCreateDto;
import com.comercialvalerio.application.dto.HistorialDto;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ClienteResourceApi {
    @GET
    Response listar(@QueryParam("nombre") String nombre,
                    @QueryParam("telefono") String telefono);

    /** Sobrecarga de conveniencia para proxies de UI. */
    default Response listar() {
        return listar(null, null);
    }

    /** Envoltura de conveniencia usada por {@link com.comercialvalerio.application.service.ClienteService#buscarPorNombre}. */
    default Response buscarPorNombre(String nombre) {
        return listar(nombre, null);
    }

    /** Envoltura de conveniencia usada por {@link com.comercialvalerio.application.service.ClienteService#buscarPorTelefono}. */
    default Response buscarPorTelefono(String telefono) {
        return listar(null, telefono);
    }
    @GET
    @Path("activos")
    public Response listarActivos();
    @GET
    @Path("estado/{nombre}")
    public Response findByEstado(@PathParam("nombre") @NotBlank String nombre);
    @GET
    @Path("{id}")
    public Response obtener(@PathParam("id") @NotNull Integer id);
    @GET
    @Path("dni/{dni}")
    public Response obtenerPorDni(@PathParam("dni") @NotBlank String dni);
    @GET
    @Path("rango")
    public Response listarPorRangoRegistro( @QueryParam("desde") @NotNull LocalDate desde, @QueryParam("hasta") @NotNull LocalDate hasta );
    @POST
    public Response registrar(@Valid ClienteCreateDto dto);
    @PUT
    @Path("{id}")
    public Response actualizar( @PathParam("id") @NotNull Integer id, @Valid ClienteCreateDto dto );
    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @PUT
    @Path("{id}/estado")
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id, @Valid CambiarEstadoDto dto);

    @GET
    @Path("{id}/historial")
    public List<HistorialDto> historialPorCliente(@PathParam("id") @NotNull Integer id,
                                                  @QueryParam("d") java.time.LocalDateTime desde,
                                                  @QueryParam("h") java.time.LocalDateTime hasta,
                                                  @QueryParam("cat") Integer categoria,
                                                  @QueryParam("prod") Integer producto);

    @GET
    @Path("{id}/eliminable")
    public Response obtenerDependencias(@PathParam("id") @NotNull Integer id);
}

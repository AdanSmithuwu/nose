package com.comercialvalerio.application.rest;
import java.util.List;
import com.comercialvalerio.application.dto.HistorialTransaccionDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.constraints.NotNull;

@Path("/historial")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HistorialTransaccionResourceApi {
    @GET
    @Path("/cliente/{id}")
    public List<HistorialTransaccionDto> listarPorCliente(@PathParam("id") @NotNull Integer id);
}

package com.comercialvalerio.application.rest;
import java.util.List;
import com.comercialvalerio.application.dto.DetalleDto;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/transacciones/{idTx}/detalles")
@Produces(MediaType.APPLICATION_JSON)
public interface DetalleResourceApi {
    @GET
    public List<DetalleDto> listar(@PathParam("idTx") @NotNull Integer idTx);
}

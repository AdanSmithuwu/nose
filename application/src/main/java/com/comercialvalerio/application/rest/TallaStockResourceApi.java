package com.comercialvalerio.application.rest;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.dto.TallaStockCreateDto;
import com.comercialvalerio.application.dto.TallaStockDto;
import com.comercialvalerio.common.DbConstraints;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tallas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TallaStockResourceApi {
    @GET
    public List<TallaStockDto> listarPorProducto( @QueryParam("producto") Integer idProd);
    @GET @Path("{id}")
    public TallaStockDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid TallaStockCreateDto in);
    @PUT @Path("{id}")
    public TallaStockDto actualizar(@PathParam("id") @NotNull Integer id, @Valid TallaStockCreateDto in);
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/activar")
    public Response activar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") @NotNull Integer id);
    @GET @Path("{id}/eliminable")
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id);
    @GET @Path("all")
    public List<TallaStockDto> listarTodosPorProducto(
            @QueryParam("producto") Integer idProd);
    @PATCH @Path("{id}")
    public Response ajustarStock(@PathParam("id") @NotNull Integer id, AjusteStockDto body);
    public static final class AjusteStockDto {
        @NotNull
        @Digits(integer = DbConstraints.STOCK_INTEGER,
                fraction = DbConstraints.STOCK_SCALE)
        public BigDecimal delta;
    }
}

package com.comercialvalerio.application.rest;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.ProductoMasVendidoDto;
import com.comercialvalerio.application.dto.ProductoVentaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.service.ProductoService;
import com.comercialvalerio.application.dto.TipoPedido;

import jakarta.enterprise.context.RequestScoped;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.comercialvalerio.application.rest.util.Responses;
import jakarta.annotation.security.RolesAllowed;

@Path("/productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProductoResource implements ProductoResourceApi {

    @Inject ProductoService svc;

    /* ---------- CRUD base ---------- */
    @GET
    @Override
    public List<ProductoDto> listar(@QueryParam("nombre")   String nombre,
                                    @QueryParam("categoria") Integer categoria,
                                    @QueryParam("tipo")      Integer tipo,
                                    @QueryParam("talla")     String talla,
                                    @QueryParam("unidad")    String unidad) {
        return svc.listar(nombre, categoria, tipo, talla, unidad);
    }
    @GET @Path("bajo-stock")
    @Override
    public List<ProductoDto> listarBajoStock() { return svc.listarBajoStock(); }
    @GET @Path("venta")
    @Override
    public List<ProductoVentaDto> listarParaVenta() { return svc.listarParaVenta(); }
    @GET @Path("pedido")
    @Override
    public List<ProductoDto> listarParaPedido(@QueryParam("nombre") String nombre,
                                              @QueryParam("tipo") TipoPedido tipoPedidoDefault) {
        return svc.listarParaPedido(nombre, tipoPedidoDefault);
    }
    @GET @Path("mas-vendidos")
    @Override
    public List<ProductoMasVendidoDto> listarMasVendidos(@QueryParam("top") @jakarta.ws.rs.DefaultValue("5") int top) {
        return svc.listarMasVendidos(top);
    }
    @GET @Path("{id}")
    @Override
    public ProductoDto obtener(@PathParam("id") @NotNull Integer id) {
        return svc.obtener(id);
    }

    @POST
    @Override
    public Response crear(@Valid ProductoCUDto dto) {
        ProductoDto p = svc.crear(dto);
        return Response.status(Response.Status.CREATED).entity(p).build();
    }

    @PUT @Path("{id}")
    @Override
    public ProductoDto actualizar(@PathParam("id") @NotNull Integer id, @Valid ProductoCUDto dto) {
        return svc.actualizar(id, dto);
    }

    @DELETE @Path("{id}") public Response eliminar(@PathParam("id") @NotNull Integer id) {
        svc.eliminar(id); return Responses.noContent(); }

    /* Ajuste directo de stock (inventario físico) */
    @PUT @Path("{id}/stock/{cantidad}")
    @RolesAllowed("Administrador")
    @Override
    public Response ajustar(@PathParam("id") @NotNull Integer id,
                            @PathParam("cantidad") @NotNull BigDecimal cantidad) {
        svc.ajustarStock(id, cantidad); return Responses.noContent();
    }

    @PUT @Path("{id}/estado")
    @RolesAllowed({"Administrador", "Empleado"})
    @Override
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id,
                                  @Valid CambiarEstadoDto dto) {
        svc.cambiarEstado(id, dto);
        return Responses.noContent();
    }

    @POST @Path("/recalcular-stock")
    @RolesAllowed("Administrador")
    @Override
    public Response recalcularStockGlobal() {
        svc.recalcularStockGlobal();
        return Responses.noContent();
    }

    @GET @Path("{id}/eliminable")
    @Override
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id) {
        return svc.obtenerDependencias(id);
    }
}

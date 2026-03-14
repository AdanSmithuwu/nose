package com.comercialvalerio.application.rest;
import java.math.BigDecimal;
import java.util.List;
import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.ProductoMasVendidoDto;
import com.comercialvalerio.application.dto.ProductoVentaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.TipoPedido;
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
import jakarta.annotation.security.RolesAllowed;

@Path("/productos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProductoResourceApi {
    @GET
    public List<ProductoDto> listar(@QueryParam("nombre")   String nombre,
                                    @QueryParam("categoria") Integer categoria,
                                    @QueryParam("tipo")      Integer tipo,
                                    @QueryParam("talla")     String talla,
                                    @QueryParam("unidad")    String unidad);
    @GET @Path("bajo-stock")
    public List<ProductoDto> listarBajoStock();
    @GET @Path("venta")
    public List<ProductoVentaDto> listarParaVenta();
    @GET @Path("pedido")
    public List<ProductoDto> listarParaPedido(@QueryParam("nombre") String nombre,
                                              @QueryParam("tipo") TipoPedido tipoPedidoDefault);
    @GET @Path("mas-vendidos")
    public List<ProductoMasVendidoDto> listarMasVendidos(@QueryParam("top") @jakarta.ws.rs.DefaultValue("5") int top);
    @GET @Path("{id}")
    public ProductoDto obtener(@PathParam("id") @NotNull Integer id);
    @POST
    public Response crear(@Valid ProductoCUDto dto);
    @PUT @Path("{id}")
    public ProductoDto actualizar(@PathParam("id") @NotNull Integer id, @Valid ProductoCUDto dto);
    @DELETE @Path("{id}")
    public Response eliminar(@PathParam("id") @NotNull Integer id);
    @PUT @Path("{id}/stock/{cantidad}")
    public Response ajustar(@PathParam("id") @NotNull Integer id, @PathParam("cantidad") @NotNull BigDecimal cantidad);
    @PUT @Path("{id}/estado")
    @RolesAllowed({"Administrador", "Empleado"})
    public Response cambiarEstado(@PathParam("id") @NotNull Integer id, @Valid CambiarEstadoDto dto);

    @POST @Path("/recalcular-stock")
    @RolesAllowed("Administrador")
    public Response recalcularStockGlobal();

    @GET @Path("{id}/eliminable")
    public List<String> obtenerDependencias(@PathParam("id") @NotNull Integer id);
}

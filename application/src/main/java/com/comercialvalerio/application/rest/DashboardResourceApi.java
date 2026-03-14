package com.comercialvalerio.application.rest;
import com.comercialvalerio.application.dto.DashboardMetricasDto;
import com.comercialvalerio.application.dto.ClienteFrecuenteDto;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
public interface DashboardResourceApi {
    @GET
    @Path("/indicadores")
    public DashboardMetricasDto indicadores();
    @GET
    @Path("/clientes-frecuentes")
    public java.util.List<ClienteFrecuenteDto> clientesFrecuentes( @QueryParam("top") @DefaultValue("5") int top);
}

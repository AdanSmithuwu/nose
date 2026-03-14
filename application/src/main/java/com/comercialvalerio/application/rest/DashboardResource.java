package com.comercialvalerio.application.rest;

import com.comercialvalerio.application.dto.DashboardMetricasDto;
import com.comercialvalerio.application.dto.ClienteFrecuenteDto;
import com.comercialvalerio.application.service.DashboardService;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class DashboardResource implements DashboardResourceApi {

    @Inject
    DashboardService svc;

    @GET
    @Path("/indicadores")
    @Override
    public DashboardMetricasDto indicadores() {
        return svc.indicadores();
    }

    @GET
    @Path("/clientes-frecuentes")
    @Override
    public java.util.List<ClienteFrecuenteDto> clientesFrecuentes(
            @QueryParam("top") @DefaultValue("5") int top) {
        return svc.clientesFrecuentes(top);
    }
}

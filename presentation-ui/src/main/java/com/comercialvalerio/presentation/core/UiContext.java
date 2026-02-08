package com.comercialvalerio.presentation.core;

import com.comercialvalerio.application.dto.EmpleadoDto;
import com.comercialvalerio.presentation.client.ServiceProxies; // Proxies de servicio
import com.comercialvalerio.application.service.*;
import com.comercialvalerio.application.rest.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class UiContext {
    private static EmpleadoDto usuarioActual;
    private static final Map<Class<?>, AtomicReference<Object>> PROXIES = new HashMap<>();

    static {
        PROXIES.put(BitacoraLoginService.class, new AtomicReference<>());
        PROXIES.put(DashboardService.class, new AtomicReference<>());
        PROXIES.put(HistorialTransaccionService.class, new AtomicReference<>());
        PROXIES.put(CategoriaService.class, new AtomicReference<>());
        PROXIES.put(ClienteService.class, new AtomicReference<>());
        PROXIES.put(ComprobanteService.class, new AtomicReference<>());
        PROXIES.put(DetalleService.class, new AtomicReference<>());
        PROXIES.put(EmpleadoService.class, new AtomicReference<>());
        PROXIES.put(EstadoService.class, new AtomicReference<>());
        PROXIES.put(MetodoPagoService.class, new AtomicReference<>());
        PROXIES.put(MovimientoInventarioService.class, new AtomicReference<>());
        PROXIES.put(PagoService.class, new AtomicReference<>());
        PROXIES.put(ParametroSistemaService.class, new AtomicReference<>());
        PROXIES.put(PedidoService.class, new AtomicReference<>());
        PROXIES.put(PresentacionService.class, new AtomicReference<>());
        PROXIES.put(ProductoService.class, new AtomicReference<>());
        PROXIES.put(ReporteService.class, new AtomicReference<>());
        PROXIES.put(RolService.class, new AtomicReference<>());
        PROXIES.put(TallaStockService.class, new AtomicReference<>());
        PROXIES.put(TipoMovimientoService.class, new AtomicReference<>());
        PROXIES.put(TipoProductoService.class, new AtomicReference<>());
        PROXIES.put(AlertaStockService.class, new AtomicReference<>());
        PROXIES.put(VentaService.class, new AtomicReference<>());
        PROXIES.put(HistorialService.class, new AtomicReference<>());
        PROXIES.put(AppShutdownService.class, new AtomicReference<>());
    }

    private UiContext() {}

    public static void resetProxies() {
        for (AtomicReference<Object> ref : PROXIES.values()) {
            ref.set(null);
        }
    }

    // usuario autenticado
    public static EmpleadoDto getUsuarioActual()    { return usuarioActual; }
    public static void setUsuarioActual(EmpleadoDto dto) { usuarioActual = dto; }

    // --------- exponga aquí sus proxies ---------

    private static <T> T getProxy(Class<T> iface, Class<?> api) {
        AtomicReference<Object> ref = PROXIES.computeIfAbsent(iface, k -> new AtomicReference<>());
        Object current = ref.get();
        if (current == null) {
            current = ServiceProxies.create(iface, api);
            ref.set(current);
        }
        return iface.cast(current);
    }

    public static BitacoraLoginService bitacoraLoginSvc() {
        return getProxy(BitacoraLoginService.class, BitacoraLoginResourceApi.class);
    }

    public static DashboardService dashboardSvc() {
        return getProxy(DashboardService.class, DashboardResourceApi.class);
    }

    public static HistorialTransaccionService historialTransaccionSvc() {
        return getProxy(HistorialTransaccionService.class, HistorialTransaccionResourceApi.class);
    }

    public static CategoriaService categoriaSvc() {
        return getProxy(CategoriaService.class, CategoriaResourceApi.class);
    }

    public static ClienteService clienteSvc() {
        return getProxy(ClienteService.class, ClienteResourceApi.class);
    }

    public static ComprobanteService comprobanteSvc() {
        return getProxy(ComprobanteService.class, ComprobanteResourceApi.class);
    }

    public static DetalleService detalleSvc() {
        return getProxy(DetalleService.class, DetalleResourceApi.class);
    }

    public static EmpleadoService empleadoSvc() {
        return getProxy(EmpleadoService.class, EmpleadoResourceApi.class);
    }

    public static EstadoService estadoSvc() {
        return getProxy(EstadoService.class, EstadoResourceApi.class);
    }

    public static MetodoPagoService metodoPagoSvc() {
        return getProxy(MetodoPagoService.class, MetodoPagoResourceApi.class);
    }

    public static MovimientoInventarioService movimientoInventarioSvc() {
        return getProxy(MovimientoInventarioService.class, MovimientoInventarioResourceApi.class);
    }

    public static PagoService pagoSvc() {
        return getProxy(PagoService.class, PagoResourceApi.class);
    }

    public static ParametroSistemaService parametroSistemaSvc() {
        return getProxy(ParametroSistemaService.class, ParametroSistemaResourceApi.class);
    }

    public static PedidoService pedidoSvc() {
        return getProxy(PedidoService.class, PedidoResourceApi.class);
    }

    public static PresentacionService presentacionSvc() {
        return getProxy(PresentacionService.class, PresentacionResourceApi.class);
    }

    public static ProductoService productoSvc() {
        return getProxy(ProductoService.class, ProductoResourceApi.class);
    }

    public static ReporteService reporteSvc() {
        return getProxy(ReporteService.class, ReporteResourceApi.class);
    }

    public static RolService rolSvc() {
        return getProxy(RolService.class, RolResourceApi.class);
    }

    public static TallaStockService tallaStockSvc() {
        return getProxy(TallaStockService.class, TallaStockResourceApi.class);
    }

    public static TipoMovimientoService tipoMovimientoSvc() {
        return getProxy(TipoMovimientoService.class, TipoMovimientoResourceApi.class);
    }

    public static TipoProductoService tipoProductoSvc() {
        return getProxy(TipoProductoService.class, TipoProductoResourceApi.class);
    }

    public static AlertaStockService alertaStockSvc() {
        return getProxy(AlertaStockService.class, AlertaStockResourceApi.class);
    }

    public static VentaService ventaSvc() {
        return getProxy(VentaService.class, VentaResourceApi.class);
    }

    public static HistorialService historialSvc() {
        return getProxy(HistorialService.class, ClienteResourceApi.class);
    }

    public static AppShutdownService appShutdownSvc() {
        return getProxy(AppShutdownService.class, AppShutdownResourceApi.class);
    }
}

package com.comercialvalerio.application.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.comercialvalerio.application.dto.VentaCreateDto;
import com.comercialvalerio.application.dto.DetalleCreateDto;
import com.comercialvalerio.application.service.ClienteService;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.MetodoPago;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.Venta;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.repository.ClienteRepository;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.MetodoPagoRepository;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;
import com.comercialvalerio.application.service.util.ServiceChecks;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Construye una {@link Venta} completa a partir de su DTO de creación.
 */
@ApplicationScoped
public class VentaMapper {

    @Inject EmpleadoRepository       repoEmp;
    @Inject ClienteRepository        repoCli;
    @Inject ClienteService           cliService;
    @Inject ProductoRepository       repoProd;
    @Inject TallaStockRepository     repoTs;
    @Inject MetodoPagoRepository     repoMp;
    @Inject PresentacionRepository   repoPres;

    /**
     * Constructor por defecto requerido por CDI.
     */
    public VentaMapper() {
    }

    public Venta toVenta(VentaCreateDto dto) {
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(dto.idEmpleado()), "Empleado inexistente");

        Cliente cli = null;
        if (dto.idCliente() != null) {
            cli = ServiceChecks.requireFound(
                    repoCli.findById(dto.idCliente()), "Cliente inexistente");
        } else if (dto.nuevoCliente() != null) {
            var creado = cliService.registrar(dto.nuevoCliente());
            cli = ServiceChecks.requireFound(
                    repoCli.findById(creado.idPersona()), "Cliente inexistente");
        }
        if (cli == null)
            throw new IllegalArgumentException("idCliente obligatorio");

        Venta v = new Venta();
        v.setFecha(LocalDateTime.now());
        v.setEmpleado(emp);
        v.setCliente(cli);
        v.setObservacion(dto.observacion());

        // Cargar en bloque las entidades necesarias
        var prodIds = dto.detalles().stream()
                .map(d -> d.idProducto())
                .distinct()
                .toList();
        var tallaIds = dto.detalles().stream()
                .map(DetalleCreateDto::idTallaStock)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        var pagoIds = dto.pagos().stream()
                .map(p -> p.idMetodoPago())
                .distinct()
                .toList();

        var productos = repoProd.findAllById(prodIds).stream()
                .collect(Collectors.toMap(
                        Producto::getIdProducto,
                        Function.identity()));
        var tallas = repoTs.findAllById(tallaIds).stream()
                .collect(Collectors.toMap(
                        TallaStock::getIdTallaStock,
                        Function.identity()));
        var presentaciones = repoPres.findByProductos(prodIds).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProducto().getIdProducto()));
        var metodosPago = repoMp.findAllById(pagoIds).stream()
                .collect(Collectors.toMap(
                        MetodoPago::getIdMetodoPago,
                        Function.identity()));

        // Detalle de productos
        for (var d : dto.detalles()) {
            Producto prod = productos.get(d.idProducto());
            if (prod == null)
                throw new EntityNotFoundException("Producto " + d.idProducto() + " no existe");
            List<Presentacion> pres = null;
            if (TipoProductoNombre.FRACCIONABLE.equalsNombre(prod.getTipoProducto().getNombre()))
                pres = presentaciones.getOrDefault(prod.getIdProducto(), List.of());
            TallaStock ts = null;
            if (d.idTallaStock() != null) {
                ts = tallas.get(d.idTallaStock());
                if (ts == null)
                    throw new EntityNotFoundException("TallaStock " + d.idTallaStock() + " no existe");
            }
            DetalleTransaccion line = new DetalleTransaccion();
            line.setTransaccion(v);
            line.setProducto(prod);
            line.setTallaStock(ts);
            line.setCantidad(d.cantidad());
            line.setPrecioUnitario(d.precioUnitario());
            line.validarPresentacion(pres);
            v.agregarDetalle(line);
        }

        // Pagos
        for (var p : dto.pagos()) {
            MetodoPago mp = metodosPago.get(p.idMetodoPago());
            if (mp == null)
                throw new EntityNotFoundException("MétodoPago " + p.idMetodoPago() + " no existe");
            PagoTransaccion pago = new PagoTransaccion();
            pago.setTransaccion(v);
            pago.setMetodoPago(mp);
            pago.setMonto(p.monto());
            v.agregarPago(pago);
        }

        // Los totales de las ventas se derivan del detalle. Ignorar los valores
        // calculados en el cliente para evitar desajustes de redondeo.

        return v;
    }
}

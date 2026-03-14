package com.comercialvalerio.application.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.comercialvalerio.application.dto.PedidoCreateDto;
import com.comercialvalerio.application.service.util.ServiceChecks;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.model.Cliente;
import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.Empleado;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.repository.ClienteRepository;
import com.comercialvalerio.domain.repository.EmpleadoRepository;
import com.comercialvalerio.domain.repository.PresentacionRepository;
import com.comercialvalerio.domain.repository.ProductoRepository;
import com.comercialvalerio.domain.repository.TallaStockRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Construye un {@link Pedido} completo a partir de su DTO de creación.
 */
@ApplicationScoped
public class PedidoMapper {

    @Inject EmpleadoRepository       repoEmp;
    @Inject ClienteRepository        repoCli;
    @Inject ProductoRepository       repoProd;
    @Inject TallaStockRepository     repoTs;
    @Inject PresentacionRepository   repoPres;

    public Pedido toPedido(PedidoCreateDto dto) {
        Empleado emp = ServiceChecks.requireFound(
                repoEmp.findById(dto.idEmpleado()), "Empleado inexistente");
        if (dto.idCliente() == null)
            throw new BusinessRuleViolationException("Cliente obligatorio");

        Cliente cli = ServiceChecks.requireFound(
                repoCli.findById(dto.idCliente()), "Cliente inexistente");

        Pedido p = new Pedido();
        p.setFecha(LocalDateTime.now());
        p.setEmpleado(emp);
        p.setCliente(cli);
        p.setObservacion(dto.observacion());
        p.setDireccionEntrega(dto.direccionEntrega());
        p.setUsaValeGas(dto.usaValeGas());

        for (var d : dto.detalles()) {
            Producto prod = ServiceChecks.requireFound(
                    repoProd.findById(d.idProducto()),
                    "Producto " + d.idProducto() + " no existe");
            if (!prod.isParaPedido())
                throw new BusinessRuleViolationException("Producto no disponible para pedidos");
            com.comercialvalerio.domain.model.TipoPedido tipo =
                    com.comercialvalerio.domain.model.TipoPedido.valueOf(
                            dto.tipoPedido().name());
            if (tipo == com.comercialvalerio.domain.model.TipoPedido.ESPECIAL &&
                    com.comercialvalerio.domain.model.TipoPedido.ESPECIAL !=
                        prod.getTipoPedidoDefault())
                throw new BusinessRuleViolationException(
                        "Producto no apto para pedidos Especial");
            List<Presentacion> presentaciones = null;
            if (TipoProductoNombre.FRACCIONABLE.equalsNombre(prod.getTipoProducto().getNombre()))
                presentaciones = repoPres.findByProducto(prod.getIdProducto());
            TallaStock ts = null;
            if (d.idTallaStock() != null) {
                ts = ServiceChecks.requireFound(
                        repoTs.findById(d.idTallaStock()),
                        "TallaStock " + d.idTallaStock() + " no existe");
            }
            if (d.cantidad() != null
                    && d.cantidad().remainder(java.math.BigDecimal.ONE)
                            .compareTo(java.math.BigDecimal.ZERO) != 0
                    && !TipoProductoNombre.FRACCIONABLE.equalsNombre(
                            prod.getTipoProducto().getNombre()))
                throw new BusinessRuleViolationException(
                        "La cantidad debe ser entera");
            DetalleTransaccion line = new DetalleTransaccion();
            line.setTransaccion(p);
            line.setProducto(prod);
            line.setTallaStock(ts);
            line.setCantidad(d.cantidad());
            line.setPrecioUnitario(d.precioUnitario());
            line.validarPresentacion(presentaciones);
            p.agregarDetalle(line);

        }

        p.setTipoPedido(
                com.comercialvalerio.domain.model.TipoPedido.valueOf(
                        dto.tipoPedido().name()));

        // Los totales calculados en la interfaz deben persistir en la transacción
        // para que los valores de descuento y cargo se almacenen correctamente.
        p.setTotales(dto.totalBruto(), dto.descuento(),
                     dto.cargo(), dto.totalNeto());

        return p;
    }
}

package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Pedido;
import com.comercialvalerio.domain.model.PagoTransaccion;
import java.time.LocalDateTime;
import java.util.List;

/* Pedidos programados a futuro o con entrega a domicilio */
public interface PedidoRepository {
    List<Pedido> findAll();
    java.util.Optional<Pedido> findById(Integer id);
    List<Pedido> findPendientesEntrega();
    List<Pedido> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta);
    void save(Pedido pedido);
    void update(Pedido pedido);
    void cancelar(Integer idPedido, String motivoCancelacion);

    /* Marca el pedido como entregado y registra los pagos correspondientes */
    void marcarEntregado(Integer idPedido, List<PagoTransaccion> pagos,
                         LocalDateTime fechaHoraEntrega, Integer idEmpleadoEntrega);

    /** Verifica si un cliente tiene pedidos asociados. */
    boolean existsByCliente(Integer idCliente);
    /** Verifica si el empleado indicado entregó algún pedido. */
    boolean existsByEmpleadoEntrega(Integer idEmpleado);

    /**
     * Limpia el contexto de persistencia actual asegurando que los cambios se
     * sincronicen con la base de datos.
     */
    void clearContext();
}

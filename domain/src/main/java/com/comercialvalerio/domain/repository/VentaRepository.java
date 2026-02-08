package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Venta;
import java.time.LocalDateTime;
import java.util.List;

/* Operaciones sobre ventas realizadas */
public interface VentaRepository {
    List<Venta> findAll();
    java.util.Optional<Venta> findById(Integer id);
    /* Ventas en un periodo de tiempo */
    List<Venta> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta);
    /* Ventas realizadas por un cliente */
    List<Venta> findByCliente(Integer idCliente);
    boolean existsById(Integer idTx);
    void save(Venta venta);
    void cancelar(Integer idVenta, String motivo);
}

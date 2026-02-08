package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.MovimientoInventario;
import java.time.LocalDateTime;
import java.util.List;

/* Kardex de inventario */
public interface MovimientoInventarioRepository {
    List<MovimientoInventario> findByProducto(Integer idProducto);
    /** Verifica si existen movimientos para el producto indicado. */
    boolean existsByProducto(Integer idProducto);
    /**
     * Verifica si existen movimientos para el producto cuyo motivo sea
     * diferente al indicado.
     */
    boolean existsByProductoAndMotivoNot(Integer idProducto, String motivo);
    /**
     * Verifica si existen movimientos para la talla indicada cuyo motivo sea
     * diferente al indicado.
     */
    boolean existsByTallaStockAndMotivoNot(Integer idTallaStock, String motivo);
    /** Verifica si existen movimientos registrados por el empleado indicado. */
    boolean existsMovimientosByEmpleado(Integer idEmpleado);
    /* Movimientos realizados en un rango de fechas. */
    List<MovimientoInventario> findByRangoFecha(LocalDateTime desde, LocalDateTime hasta);
    void save(MovimientoInventario movimiento);
}

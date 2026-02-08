package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.TallaStock;
import java.util.List;
import java.math.BigDecimal;

/* Stock por talla o presentación específica */
public interface TallaStockRepository {
    /* ---------- Lectura ---------- */
    List<TallaStock> findByProducto(Integer idProducto);
    /** Obtiene todos los registros de TallaStock para los ids indicados. */
    List<TallaStock> findAllById(java.util.Collection<Integer> ids);
    java.util.Optional<TallaStock> findByProductoAndTalla(Integer idProducto, String talla);
    java.util.Optional<TallaStock> findById(Integer idTallaStock);
    List<TallaStock> findByProductos(List<Integer> ids);

    /* ---------- Escritura ---------- */
    void save(TallaStock ts);
    void delete(Integer idTallaStock);
    void ajustarStock(Integer idTallaStock, BigDecimal delta);
    void updateEstado(Integer idTallaStock, String estado);
    /** Cambia el estado de todas las tallas de un producto. */
    void updateEstadoByProducto(Integer idProducto, String estado);
    /** Ejecuta el procedimiento que recalcula el stock de todos los productos. */
    void recalcularStocks();
}

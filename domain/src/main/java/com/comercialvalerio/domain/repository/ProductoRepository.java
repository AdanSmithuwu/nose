package com.comercialvalerio.domain.repository;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TipoPedido;
import java.util.List;
import java.math.BigDecimal;

/* Acceso a los productos vendidos por Comercial's Valerio */
public interface ProductoRepository {
    List<Producto> findAll();
    /** Obtiene todos los productos para los ids indicados. */
    List<Producto> findAllById(java.util.Collection<Integer> ids);
    java.util.Optional<Producto> findById(Integer id);
    /* Busca productos cuyo nombre contenga el patrón indicado */
    List<Producto> findByNombreLike(String patron);
    List<Producto> findByCategoria(Integer idCategoria);
    /* Productos con stock por debajo de su umbral mínimo */
    List<Producto> findBajoStock();
    /**
     * Verifica si existe un producto con el nombre indicado.
     *
     * @param nombre    nombre a buscar
     * @param excludeId identificador de un producto que se excluirá de la
     *                  comparación (puede ser {@code null})
     * @return {@code true} si existe otro producto con ese nombre
     */
    boolean existsByNombre(String nombre, Integer excludeId);
    /** Verifica si existen productos para la categoría indicada. */
    boolean existsByCategoria(Integer idCategoria);
    void save(Producto producto);
    void delete(Integer id);
    /* Ajusta el stock actual de un producto */
    void actualizarStock(Integer idProducto, BigDecimal nuevoStock);
    void updateEstado(Integer idProducto, String estado);
    /** Actualiza la bandera para ignorar el umbral de stock hasta que la cantidad llegue a cero. */
    void setIgnorarUmbralHastaCero(Integer idProducto, boolean ignorar);
    /** Recalcula el stockActual de todos los productos según sus tallas. */
    void recalcularStocks();
    /* Busca productos aplicando filtros opcionales */
    List<Producto> findByFiltros(String nombre, Integer idCategoria,
                                 Integer idTipoProducto,
                                 String talla, String unidad);
    /* Productos con colecciones cargadas para ventas */
    List<Producto> findWithTallasAndPresentaciones();
    /**
     * Lista productos disponibles para pedidos filtrando por su tipo por
     * defecto. Un {@code null} como tipo devuelve todos los que no son
     * "Especial". El nombre es opcional y se aplica como coincidencia parcial.
     */
    List<Producto> findParaPedido(String nombre, TipoPedido tipoPedidoDefault);

    /**
     * Ajusta el valor {@code minMayorista} de todos los productos
     * que comienzan con "Ovillo de hilo".
     */
    void actualizarMinMayoristaHilo(int nuevoMin);
}

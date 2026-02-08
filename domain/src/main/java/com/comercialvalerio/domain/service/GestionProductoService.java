package com.comercialvalerio.domain.service;

import com.comercialvalerio.domain.model.Producto;

/**
 * Servicio de dominio para operaciones de mantenimiento de productos.
 */
public interface GestionProductoService {
    /**
     * Guarda un producto aplicando las validaciones de negocio necesarias.
     *
     * @param producto entidad a persistir
     * @return producto con identificador asignado
     */
    Producto guardarProducto(Producto producto);

    /**
     * Elimina un producto verificando que no existan movimientos previos.
     *
     * @param idProducto identificador del producto
     */
    void eliminarProducto(Integer idProducto);

    /** Recalcula el stock de todos los productos según tallas existentes. */
    void recalcularStockGlobal();

    /**
     * Devuelve las descripciones de entidades que impiden eliminar un producto.
     *
     * @param idProducto identificador del producto
     * @return lista de dependencias encontradas
     */
    java.util.List<String> findDependencias(Integer idProducto);
}

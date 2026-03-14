package com.comercialvalerio.application.service;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.dto.ProductoCUDto;
import com.comercialvalerio.application.dto.ProductoDto;
import com.comercialvalerio.application.dto.ProductoMasVendidoDto;
import com.comercialvalerio.application.dto.ProductoVentaDto;
import com.comercialvalerio.application.dto.CambiarEstadoDto;
import com.comercialvalerio.application.dto.TipoPedido;

public interface ProductoService {
    List<ProductoDto> listar(String nombre, Integer idCategoria,
                             Integer idTipoProducto,
                             String talla, String unidad);
    ProductoDto obtener(Integer id);
    ProductoDto crear(ProductoCUDto dto);
    ProductoDto actualizar(Integer id, ProductoCUDto dto);
    void eliminar(Integer id);
    /* Productos con stock bajo */
    List<ProductoDto> listarBajoStock();
    /* Listado para ventas con tallas y presentaciones */
    List<ProductoVentaDto> listarParaVenta();
    /**
     * Productos disponibles para pedidos filtrando por su tipo por defecto.
     * El nombre es opcional y se compara por coincidencia parcial.
     */
    List<ProductoDto> listarParaPedido(String nombre, TipoPedido tipoPedidoDefault);
    // Ajustes puntuales
    void ajustarStock(Integer idProducto, BigDecimal nuevoStock);
    void cambiarEstado(Integer idProducto, CambiarEstadoDto dto);
    /** Recalcula el stock de todos los productos. */
    void recalcularStockGlobal();
    /**
     * Obtiene las entidades que impiden eliminar un producto.
     *
     * @param idProducto identificador del producto
     * @return lista de descripciones de dependencias existentes
     */
    List<String> obtenerDependencias(Integer idProducto);
    /* Ranking de productos más vendidos */
    List<ProductoMasVendidoDto> listarMasVendidos(int limite);
}

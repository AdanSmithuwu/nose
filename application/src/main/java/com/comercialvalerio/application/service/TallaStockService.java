package com.comercialvalerio.application.service;
import java.math.BigDecimal;
import java.util.List;

import com.comercialvalerio.application.dto.TallaStockCreateDto;
import com.comercialvalerio.application.dto.TallaStockDto;

public interface TallaStockService {
    List<TallaStockDto> listarPorProducto(Integer idProducto);
    TallaStockDto       obtener(Integer id);
    TallaStockDto       crear(TallaStockCreateDto dto);
    TallaStockDto       actualizar(Integer id, TallaStockCreateDto dto);
    void                eliminar(Integer id);
    void                ajustarStock(Integer idTallaStock, BigDecimal delta);
    void                activar(Integer id);
    void                desactivar(Integer id);
    /**
     * Lista las entidades que referencian la talla e impiden su eliminación.
     */
    List<String>        obtenerDependencias(Integer idTallaStock);
    /** Lista todas las tallas, incluyendo inactivas. */
    List<TallaStockDto> listarTodosPorProducto(Integer idProducto);
    /** Ejecuta manualmente el SP que recalcula el stock de todos los productos. */
    void                recalcularStockGlobal();
}

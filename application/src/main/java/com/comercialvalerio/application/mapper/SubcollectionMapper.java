package com.comercialvalerio.application.mapper;

import java.math.BigDecimal;

import com.comercialvalerio.application.dto.PresentacionCreateDto;
import com.comercialvalerio.application.dto.PresentacionCUDto;
import com.comercialvalerio.application.dto.TallaStockCreateDto;
import com.comercialvalerio.application.dto.TallaStockCUDto;
import com.comercialvalerio.domain.model.Presentacion;
import com.comercialvalerio.domain.model.Producto;
import com.comercialvalerio.domain.model.TallaStock;

/** Utilidades de mapeo para tallas y presentaciones. */
public final class SubcollectionMapper {
    private SubcollectionMapper() {}

    public static TallaStock fromDto(TallaStockCreateDto dto, Producto prod) {
        TallaStock ts = new TallaStock();
        ts.setProducto(prod);
        ts.setTalla(dto.talla());
        ts.setStock(dto.stock());
        return ts;
    }

    public static void updateFromDto(TallaStockCreateDto dto, Producto prod, TallaStock ts) {
        ts.setProducto(prod);
        ts.setTalla(dto.talla());
        ts.setStock(dto.stock());
    }

    public static void apply(TallaStockCUDto dto, Producto prod, TallaStock ts,
                             boolean nuevoProducto) {
        ts.setProducto(prod);
        ts.setTalla(dto.talla());
        if (dto.idTallaStock() == null) {
            // Siempre se inicia el stock en cero para registrar los
            // movimientos iniciales por separado
            ts.setStock(BigDecimal.ZERO);
        }
    }

    public static Presentacion fromDto(PresentacionCreateDto dto, Producto prod) {
        Presentacion p = new Presentacion();
        p.setProducto(prod);
        p.setCantidad(dto.cantidad());
        p.setPrecio(dto.precio());
        return p;
    }

    public static void updateFromDto(PresentacionCreateDto dto, Producto prod, Presentacion p) {
        p.setProducto(prod);
        p.setCantidad(dto.cantidad());
        p.setPrecio(dto.precio());
    }

    public static void apply(PresentacionCUDto dto, Producto prod, Presentacion p) {
        p.setProducto(prod);
        p.setCantidad(dto.cantidad());
        p.setPrecio(dto.precio());
    }
}

package com.comercialvalerio.application.service.impl;

import com.comercialvalerio.application.dto.ProductoCUDto;
import java.math.BigDecimal;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import com.comercialvalerio.domain.exception.EntityNotFoundException;
import com.comercialvalerio.domain.model.Categoria;
import com.comercialvalerio.domain.model.Estado;
import com.comercialvalerio.domain.model.TipoProducto;
import com.comercialvalerio.domain.model.TipoProductoNombre;
import com.comercialvalerio.domain.model.EstadoNombre;
import com.comercialvalerio.domain.repository.CategoriaRepository;
import com.comercialvalerio.application.cache.EstadoCache;
import com.comercialvalerio.domain.repository.TipoProductoRepository;

/** Ayudante con utilidades de validación para operaciones de Producto. */
public final class ProductoValidator {
    private ProductoValidator() {}

    public static FkRefs validateForeignKeys(ProductoCUDto in,
                                             CategoriaRepository repoCat,
                                             TipoProductoRepository repoTipo,
                                             EstadoCache estadoCache) {
        Categoria cat = getCat(in.idCategoria(), repoCat);
        TipoProducto tipo = getTipo(in.idTipoProducto(), repoTipo);
        Estado estActivo = estadoCache.get("Producto", EstadoNombre.ACTIVO);
        return new FkRefs(cat, tipo, estActivo);
    }

    public static void validarPrecioStock(ProductoCUDto in, TipoProducto tipo,
                                          boolean updating) {
        TipoProductoNombre nombre = TipoProductoNombre.fromNombre(
                tipo.getNombre());
        if (nombre == TipoProductoNombre.UNIDAD_FIJA) {
            if (in.precioUnitario() == null) {
                throw new BusinessRuleViolationException("Unidad fija requiere precioUnitario");
            }
            if (!updating && in.stockActual() == null) {
                throw new BusinessRuleViolationException("Unidad fija requiere stockActual");
            }
        } else if (nombre == TipoProductoNombre.FRACCIONABLE && !updating) {
            if (in.stockActual() == null || in.stockActual().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleViolationException("Fraccionable requiere stockActual positivo");
            }
        }
        // Vestimenta: sin validaciones adicionales
    }

    /**
     * Valida que solo los productos cuyo nombre inicia con
     * {@code "Ovillo de hilo"} puedan configurarse con el tipo de pedido
     * {@link com.comercialvalerio.application.dto.TipoPedido#ESPECIAL}.
     */
    public static void validarTipoPedido(ProductoCUDto in) {
        if (in.tipoPedidoDefault() == com.comercialvalerio.application.dto.TipoPedido.ESPECIAL
                && !in.nombre().toLowerCase().startsWith("ovillo de hilo")) {
            throw new BusinessRuleViolationException(
                    "Solo productos que empiezan con 'Ovillo de hilo' pueden ser Especial");
        }
    }

    private static Categoria getCat(Integer id, CategoriaRepository repoCat) {
        return repoCat.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoría id=" + id + " no existe"));
    }

    private static TipoProducto getTipo(Integer id, TipoProductoRepository repoTipo) {
        return repoTipo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "TipoProducto id=" + id + " no existe"));
    }

    public record FkRefs(Categoria cat, TipoProducto tipo, Estado estActivo) {}
}

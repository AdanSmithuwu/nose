package com.comercialvalerio.domain.model;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.comercialvalerio.common.DbConstraints;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationMessages.DESCRIPTION_TOO_LONG;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_CHARS_ONLY;
import static com.comercialvalerio.domain.util.ValidationMessages.STATE_REQUIRED;
import static com.comercialvalerio.domain.util.ValidationMessages.UNIT_PRICE_POSITIVE;
import static com.comercialvalerio.domain.util.ValidationMessages.NAME_REQUIRED_MAX_LENGTH;
import static com.comercialvalerio.domain.util.ValidationUtils.requireIdNotSet;
import static com.comercialvalerio.domain.util.ValidationUtils.requireMaxLength;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNonNegative;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotBlank;
import static com.comercialvalerio.domain.util.ValidationUtils.requireNotNull;
import static com.comercialvalerio.domain.util.ValidationUtils.requirePrecision;

/*
 * Producto de inventario y venta.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idProducto</code></li>
 *   <li>Nombre único (<code>UNIQUE(nombre)</code>)</li>
 *   <li>FK a <b>categoria</b>, <b>tipo_producto</b> y <b>estado</b></li>
 * </ul>
 */
public class Producto extends BaseEntity<Integer> {

    private static final Pattern NOMBRE_PATTERN = Pattern.compile("[^\\p{Cntrl}]+");
    private static final String  MODULO = "Producto";

    /* ---------- Atributos ---------- */
    private Integer    idProducto;
    private String     nombre;          // único
    private String     descripcion;     // opcional máx. 120
    private Categoria  categoria;       // FK obligatorio
    private TipoProducto tipoProducto;  // FK obligatorio
    private String     unidadMedida;    // ej. “kg”, “lt”, “unid”
    private BigDecimal precioUnitario;  // ≥ 0 o null
    private boolean    mayorista;       // ¿aplica precio mayorista?
    private boolean    paraPedido;      // Disponible para pedidos
    private TipoPedido tipoPedidoDefault; // Domicilio | Especial o null
    private Integer    minMayorista;    // >0 si mayorista=true
    private BigDecimal precioMayorista; // < precioUnitario si mayorista=true
    /** Cuando es verdadero se ignora el umbral de stock hasta que la cantidad llegue a cero. */
    private boolean    ignorarUmbralHastaCero;
    private BigDecimal stockActual;     // ≥0 o null
    private BigDecimal umbral;          // ≥0 (punto de reorden)
    private Estado     estado;          // FK obligatorio
    /* Relaciones opcionales para carga con JOIN FETCH */
    private java.util.List<TallaStock> tallas = java.util.List.of();
    private java.util.List<Presentacion> presentaciones = java.util.List.of();

    /* ---------- Constructor con invariantes ---------- */
    public Producto(Integer idProducto, String nombre, String descripcion,
                    Categoria categoria, TipoProducto tipoProducto,
                    String unidadMedida, BigDecimal precioUnitario,
                    boolean mayorista, Integer minMayorista,
                    BigDecimal precioMayorista, boolean paraPedido,
                    TipoPedido tipoPedidoDefault,
                    BigDecimal stockActual, BigDecimal umbral,
                    Estado estado, boolean ignorarUmbralHastaCero) {

        validarNombre(nombre);
        validarDescripcion(descripcion);
        validarCategoria(categoria);
        validarTipoProducto(tipoProducto);
        validarUnidadMedida(unidadMedida);
        validarPrecios(precioUnitario, mayorista, precioMayorista);
        validarMayorista(mayorista, minMayorista);
        validarTipoPedidoDefault(tipoPedidoDefault);
        validarStock(stockActual);
        validarUmbral(umbral);
        validarEstado(estado);

        this.idProducto      = idProducto;
        this.nombre          = nombre.trim();
        this.descripcion     = descripcion == null ? null : descripcion.trim();
        this.categoria       = categoria;
        this.tipoProducto    = tipoProducto;
        this.unidadMedida    = unidadMedida.trim();
        this.precioUnitario  = precioUnitario;
        this.mayorista       = mayorista;
        this.paraPedido      = paraPedido;
        this.tipoPedidoDefault = tipoPedidoDefault;
        this.minMayorista    = minMayorista;
        this.precioMayorista = precioMayorista;
        this.stockActual     = stockActual;
        this.umbral          = umbral;
        this.estado          = estado;
        this.ignorarUmbralHastaCero = ignorarUmbralHastaCero;
    }

    public Producto() {}

    /* ---------- Getters ---------- */
    public Integer   getIdProducto()      { return idProducto; }
    @Override
    public Integer   getId()              { return idProducto; }
    public String    getNombre()          { return nombre; }
    public String    getDescripcion()     { return descripcion; }
    public Categoria getCategoria()       { return categoria; }
    public TipoProducto getTipoProducto() { return tipoProducto; }
    public String    getUnidadMedida()    { return unidadMedida; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public boolean   isMayorista()        { return mayorista; }
    public boolean   isParaPedido()       { return paraPedido; }
    public TipoPedido getTipoPedidoDefault() { return tipoPedidoDefault; }
    public Integer   getMinMayorista()    { return minMayorista; }
    public BigDecimal getPrecioMayorista(){ return precioMayorista; }
    public BigDecimal getStockActual()    { return stockActual; }
    public BigDecimal getUmbral()         { return umbral; }
    public Estado    getEstado()          { return estado; }
    public boolean   isIgnorarUmbralHastaCero() { return ignorarUmbralHastaCero; }
    public java.util.List<TallaStock> getTallas() {
        return java.util.Collections.unmodifiableList(tallas);
    }

    public java.util.List<Presentacion> getPresentaciones() {
        return java.util.Collections.unmodifiableList(presentaciones);
    }

    /* ---------- Setters con validaciones ---------- */

    public void setIdProducto(Integer idProducto) {
        requireIdNotSet(this.idProducto, idProducto,
                "El idProducto ya fue asignado y no puede modificarse");
        this.idProducto = idProducto;
    }

    public void setNombre(String nombre) {
        validarNombre(nombre);
        this.nombre = nombre.trim();
    }

    public void setDescripcion(String descripcion) {
        validarDescripcion(descripcion);
        this.descripcion = descripcion == null ? null : descripcion.trim();
    }

    public void setCategoria(Categoria categoria) {
        validarCategoria(categoria);
        this.categoria = categoria;
    }

    public void setTipoProducto(TipoProducto tipoProducto) {
        validarTipoProducto(tipoProducto);
        this.tipoProducto = tipoProducto;
    }

    public void setUnidadMedida(String unidadMedida) {
        validarUnidadMedida(unidadMedida);
        this.unidadMedida = unidadMedida.trim();
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        validarPrecios(precioUnitario, mayorista, precioMayorista);
        this.precioUnitario = precioUnitario;
    }

    public void setMayorista(boolean mayorista) {
        if (!mayorista) {
            this.minMayorista = null;
            this.precioMayorista = null;
        }
        validarMayorista(mayorista, this.minMayorista);
        validarPrecios(this.precioUnitario, mayorista, this.precioMayorista);
        this.mayorista = mayorista;
    }

    /**
     * Establece los valores de venta al por mayor en una sola operación.
     */
    public void setMayorista(boolean mayorista, Integer minMayorista,
                             BigDecimal precioMayorista) {
        this.minMayorista = minMayorista;
        this.precioMayorista = precioMayorista;
        setMayorista(mayorista);
    }

    public void setParaPedido(boolean paraPedido) {
        this.paraPedido = paraPedido;
    }

    public void setTipoPedidoDefault(TipoPedido tipoPedidoDefault) {
        validarTipoPedidoDefault(tipoPedidoDefault);
        this.tipoPedidoDefault = tipoPedidoDefault;
    }

    public void setMinMayorista(Integer minMayorista) {
        validarMayorista(this.mayorista, minMayorista);
        this.minMayorista = minMayorista;
    }

    public void setPrecioMayorista(BigDecimal precioMayorista) {
        validarPrecios(precioUnitario, mayorista, precioMayorista);
        this.precioMayorista = precioMayorista;
    }

    public void setStockActual(BigDecimal stockActual) {
        validarStock(stockActual);
        this.stockActual = stockActual;
    }

    public void setUmbral(BigDecimal umbral) {
        validarUmbral(umbral);
        this.umbral = umbral;
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        this.estado = estado;
    }
    public void setIgnorarUmbralHastaCero(boolean ignorar) {
        this.ignorarUmbralHastaCero = ignorar;
    }
    public void setTallas(java.util.List<TallaStock> tallas) {
        this.tallas = tallas == null ? java.util.List.of() : tallas;
    }

    public void setPresentaciones(java.util.List<Presentacion> presentaciones) {
        this.presentaciones = presentaciones == null ? java.util.List.of() : presentaciones;
    }

    /* ---------- Operaciones de dominio ---------- */

    /**
     * Ajusta el stock sumando {@code delta} al valor actual.
     * El resultado no puede ser negativo.
     */
    public void ajustarStock(BigDecimal delta) {
        if (delta == null)
            throw new BusinessRuleViolationException("Delta de stock obligatorio");

        BigDecimal actual = this.stockActual == null
                ? BigDecimal.ZERO
                : this.stockActual;
        BigDecimal nuevo = actual.add(delta);
        validarStock(nuevo);
        this.stockActual = nuevo;
    }

    /**
     * Cambia el precio unitario validando restricciones de mayorista.
     */
    public void cambiarPrecio(BigDecimal nuevoPrecio) {
        validarPrecios(nuevoPrecio, this.mayorista, this.precioMayorista);
        this.precioUnitario = nuevoPrecio;
    }

    /**
     * Retorna el precio aplicable según la cantidad.
     */
    public BigDecimal precioParaCantidad(BigDecimal cant) {
        boolean tieneMayorista = mayorista
                && minMayorista != null
                && precioMayorista != null;
        if (tieneMayorista && cant != null) {
            boolean cumpleMinimo =
                    cant.compareTo(BigDecimal.valueOf(minMayorista)) >= 0;
            if (cumpleMinimo)
                return precioMayorista;
        }
        return precioUnitario;
    }

    /* ---------- Validaciones internas ---------- */

    private void validarNombre(String n) {
        requireNotBlank(n,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "del producto", DbConstraints.LEN_NOMBRE_PRODUCTO));
        requireMaxLength(n, DbConstraints.LEN_NOMBRE_PRODUCTO,
                String.format(NAME_REQUIRED_MAX_LENGTH,
                        "del producto", DbConstraints.LEN_NOMBRE_PRODUCTO));
        if (!NOMBRE_PATTERN.matcher(n).matches())
            throw new BusinessRuleViolationException(NAME_CHARS_ONLY);
    }

    private void validarDescripcion(String d) {
        if (d != null && d.length() > DbConstraints.LEN_DESCRIPCION)
            throw new BusinessRuleViolationException(
                DESCRIPTION_TOO_LONG);
    }

    private void validarCategoria(Categoria c) {
        requireNotNull(c, "La categoría es obligatoria");
    }

    private void validarTipoProducto(TipoProducto t) {
        requireNotNull(t, "El tipo de producto es obligatorio");
    }

    private void validarUnidadMedida(String u) {
        requireNotBlank(u, "Unidad de medida obligatoria (máx. " + DbConstraints.LEN_UNIDAD_MEDIDA + " caracteres)");
        requireMaxLength(u, DbConstraints.LEN_UNIDAD_MEDIDA,
                "Unidad de medida obligatoria (máx. " + DbConstraints.LEN_UNIDAD_MEDIDA + " caracteres)");
    }

    private void validarPrecios(BigDecimal unit, boolean may, BigDecimal mayPrice) {
        if (unit != null) {
            requireNonNegative(unit, UNIT_PRICE_POSITIVE);
            requirePrecision(unit, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                    "precioUnitario fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                            + DbConstraints.PRECIO_SCALE + ')');
        }
        if (may) {
            requireNotNull(mayPrice,
                    "Debe indicar precioMayorista cuando mayorista = true");
            requireNonNegative(mayPrice, "El precio mayorista debe ser \u2265 0");
            requirePrecision(mayPrice, DbConstraints.PRECIO_PRECISION, DbConstraints.PRECIO_SCALE,
                    "precioMayorista fuera de rango (" + DbConstraints.PRECIO_PRECISION + ','
                            + DbConstraints.PRECIO_SCALE + ')');
            if (unit != null && mayPrice.compareTo(unit) >= 0)
                throw new BusinessRuleViolationException(
                    "El precio mayorista debe ser menor al precio unitario");
        } else if (mayPrice != null) {
            throw new BusinessRuleViolationException(
                    "precioMayorista debe ser nulo cuando mayorista = false");
        }
    }

    private void validarMayorista(boolean may, Integer minMay) {
        if (may) {
            if (minMay == null || minMay <= 0)
                throw new BusinessRuleViolationException(
                        "Debe indicar minMayorista (>0) cuando mayorista = true");
        } else if (minMay != null) {
            throw new BusinessRuleViolationException(
                    "minMayorista debe ser nulo cuando mayorista = false");
        }
    }

    private void validarStock(BigDecimal s) {
        if (s != null) {
            requireNonNegative(s, "El stock actual debe ser ≥ 0");
            requirePrecision(s, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                    "stockActual fuera de rango (" + DbConstraints.STOCK_PRECISION + ','
                            + DbConstraints.STOCK_SCALE + ')');
        }
    }

    private void validarUmbral(BigDecimal u) {
        requireNonNegative(u, "El umbral de stock debe ser ≥ 0");
        requirePrecision(u, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                "umbral fuera de rango (" + DbConstraints.STOCK_PRECISION + ',' + DbConstraints.STOCK_SCALE + ")");
    }

    private void validarEstado(Estado e) {
        requireNotNull(e, STATE_REQUIRED);
        if (!MODULO.equalsIgnoreCase(e.getModulo()))
            throw new BusinessRuleViolationException(
                    "Estado inválido para Producto");
    }

    private void validarTipoPedidoDefault(TipoPedido t) {
        // se permite null; la enumeración ya valida los valores permitidos
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Producto other)) return false;
        if (idProducto != null && other.idProducto != null)
            return idProducto.equals(other.idProducto);
        if (idProducto != null || other.idProducto != null)
            return false;
        return java.util.Objects.equals(nombre, other.nombre);
    }

    @Override
    public int hashCode() {
        return idProducto != null
                ? java.util.Objects.hash(idProducto)
                : java.util.Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + "'}";
    }
}

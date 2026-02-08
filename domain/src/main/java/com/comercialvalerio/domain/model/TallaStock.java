package com.comercialvalerio.domain.model;
import com.comercialvalerio.domain.exception.BusinessRuleViolationException;
import static com.comercialvalerio.domain.util.ValidationUtils.*;
import static com.comercialvalerio.domain.util.ValidationMessages.STATE_REQUIRED;
import com.comercialvalerio.common.DbConstraints;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/*
 * Stock por talla (o variante) de un producto.
 *
 * <p>Del DDL:</p>
 * <ul>
 *   <li>PK <code>idTallaStock</code></li>
 *   <li>Clave compuesta única <code>(idProducto,talla)</code></li>
 * </ul>
 */
public class TallaStock extends BaseEntity<Integer> {

    private static final Pattern TALLA_PATTERN = Pattern.compile("[\\p{Alnum}+-]+");
    private static final String  MODULO = "Producto";

    private Integer    idTallaStock; // PK autogenerada
    private Producto   producto;    // FK obligatorio
    private String     talla;       // código de talla (XS-3XL, 36, 37…)
    private BigDecimal stock;       // ≥ 0
    private Estado     estado;      // FK obligatorio

    /* ---------- Constructor con invariantes ---------- */
    public TallaStock(Integer idTallaStock, Producto producto,
                      String talla, BigDecimal stock, Estado estado) {

        validarProducto(producto);
        validarTalla(talla);
        validarStock(stock);
        validarEstado(estado);

        this.idTallaStock = idTallaStock;
        this.producto     = producto;
        this.talla        = talla.trim();
        this.stock        = stock;
        this.estado       = estado;
    }

    public TallaStock() {}

    /* ---------- Getters ---------- */
    public Integer    getIdTallaStock() { return idTallaStock; }
    @Override
    public Integer getId() { return idTallaStock; }
    public Producto   getProducto()     { return producto; }
    public String     getTalla()        { return talla; }
    public BigDecimal getStock()        { return stock; }
    public Estado     getEstado()       { return estado; }

    /* ---------- Setters con validaciones ---------- */

    public void setIdTallaStock(Integer id) {
        requireIdNotSet(this.idTallaStock, id,
                "El idTallaStock ya fue asignado y no puede modificarse");
        this.idTallaStock = id;
    }

    public void setProducto(Producto producto) {
        validarProducto(producto);
        this.producto = producto;
    }

    public void setTalla(String talla) {
        validarTalla(talla);
        this.talla = talla.trim();
    }

    public void setStock(BigDecimal stock) {
        validarStock(stock);
        this.stock = stock;
    }

    public void setEstado(Estado estado) {
        validarEstado(estado);
        this.estado = estado;
    }

    /* ---------- Validaciones internas ---------- */

    private void validarProducto(Producto p) {
        requireNotNull(p, "El producto es obligatorio en TallaStock");
    }

    private void validarTalla(String t) {
        requireNotBlank(t, "La talla es obligatoria (máx. "
                + DbConstraints.LEN_TALLA + " caracteres)");
        requireMaxLength(t, DbConstraints.LEN_TALLA,
                "La talla es obligatoria (máx. "
                + DbConstraints.LEN_TALLA + " caracteres)");
        if (!TALLA_PATTERN.matcher(t).matches())
            throw new BusinessRuleViolationException(
                "La talla contiene caracteres no válidos");
    }

    private void validarStock(BigDecimal s) {
        requireNonNegative(s, "El stock de la talla debe ser ≥ 0");
        requirePrecision(s, DbConstraints.STOCK_PRECISION, DbConstraints.STOCK_SCALE,
                "stock fuera de rango (" + DbConstraints.STOCK_PRECISION + ',' + DbConstraints.STOCK_SCALE + ")");
    }

    private void validarEstado(Estado e) {
        requireNotNull(e, STATE_REQUIRED);
        if (!MODULO.equalsIgnoreCase(e.getModulo()))
            throw new BusinessRuleViolationException(
                    "Estado inválido para TallaStock");
    }
}

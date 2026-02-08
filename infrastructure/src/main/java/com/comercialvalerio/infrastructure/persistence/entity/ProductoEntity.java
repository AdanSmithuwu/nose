package com.comercialvalerio.infrastructure.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

import static com.comercialvalerio.common.DbConstraints.LEN_DESCRIPCION;
import static com.comercialvalerio.common.DbConstraints.LEN_NOMBRE_PRODUCTO;
import static com.comercialvalerio.common.DbConstraints.LEN_TIPO_PEDIDO;
import static com.comercialvalerio.common.DbConstraints.LEN_UNIDAD_MEDIDA;
import static com.comercialvalerio.common.DbConstraints.PRECIO_PRECISION;
import static com.comercialvalerio.common.DbConstraints.PRECIO_SCALE;
import static com.comercialvalerio.common.DbConstraints.STOCK_PRECISION;
import static com.comercialvalerio.common.DbConstraints.STOCK_SCALE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "Producto")
@Table(name = "Producto",
       uniqueConstraints = @UniqueConstraint(name = "uk_producto_nombre",
                                             columnNames = "nombre"))
@NamedQueries({
    /* Sólo las consultas requeridas por el repositorio */
    @NamedQuery(name = "Producto.findAll",
                query = "FROM Producto p ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.findByNombreLike",
                query = "FROM Producto p WHERE UPPER(p.nombre) LIKE :patron "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.findByCategoria",
                query = "FROM Producto p WHERE p.categoria.idCategoria = :idCat "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.bajoStock",
                query = "FROM Producto p WHERE p.stockActual < p.umbral "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.existsByNombre",
                query = "SELECT COUNT(p) FROM Producto p "
                      + "WHERE UPPER(p.nombre)=:n "
                      + "AND (:id IS NULL OR p.idProducto <> :id) "
                      + "AND p.estado.nombre = 'Activo'"),
    @NamedQuery(name = "Producto.existsByCategoria",
                query = "SELECT COUNT(p) FROM Producto p "
                      + "WHERE p.categoria.idCategoria = :id"),
    @NamedQuery(name = "Producto.findByIds",
                query = "FROM Producto p WHERE p.idProducto IN :ids ORDER BY p.idProducto"),
    @NamedQuery(name = "Producto.withTallasAndPresentaciones",
                query = "SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.tallas LEFT JOIN FETCH p.presentaciones "
                      + "WHERE p.estado.nombre = 'Activo' "
                      + "AND (p.paraPedido = false OR p.tipoPedidoDefault = 'Especial') "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.byFiltros",
                query = "SELECT DISTINCT p FROM Producto p LEFT JOIN p.tallas ts "
                      + "WHERE (:nombre IS NULL OR UPPER(p.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))) "
                      + "AND (:idCat IS NULL OR p.categoria.idCategoria = :idCat) "
                      + "AND (:idTipo IS NULL OR p.tipoProducto.idTipoProducto = :idTipo) "
                      + "AND (:unidad IS NULL OR UPPER(p.unidadMedida) = UPPER(:unidad)) "
                      + "AND (:talla IS NULL OR UPPER(ts.talla) = UPPER(:talla)) "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.paraPedido",
                query = "FROM Producto p WHERE p.paraPedido = true "
                      + "AND p.estado.nombre = 'Activo' "
                      + "AND ((:tipo IS NOT NULL AND (UPPER(p.tipoPedidoDefault) = UPPER(:tipo) OR p.tipoPedidoDefault IS NULL)) "
                      + "OR (:tipo IS NULL AND (p.tipoPedidoDefault IS NULL OR p.tipoPedidoDefault <> 'Especial'))) "
                      + "AND (:nombre IS NULL OR UPPER(p.nombre) LIKE UPPER(CONCAT('%', :nombre, '%'))) "
                      + "ORDER BY p.nombre"),
    @NamedQuery(name = "Producto.stockActualById",
                query = "SELECT p.stockActual FROM Producto p WHERE p.idProducto = :id")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Producto.recalcularStock",
        procedureName = "dbo.sp_RecalcularStockProductos")
})
public class ProductoEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProducto")
    private Integer idProducto;
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_PRODUCTO)
    private String  nombre;
    @Column(name = "descripcion", length = LEN_DESCRIPCION)
    private String  descripcion;
    @Column(name = "unidadMedida", nullable = false, length = LEN_UNIDAD_MEDIDA)
    private String  unidadMedida;
    @Column(name = "precioUnitario", nullable = true,
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal precioUnitario;
    @Column(name = "mayorista", nullable = false)
    private boolean mayorista;
    @Column(name = "paraPedido", nullable = false)
    private boolean paraPedido;
    @Column(name = "ignorarUmbralHastaCero", nullable = false)
    private boolean ignorarUmbralHastaCero;
    @Column(name = "tipoPedidoDefault", length = LEN_TIPO_PEDIDO)
    private String  tipoPedidoDefault;
    @Column(name = "minMayorista")
    private Integer minMayorista;
    @Column(name = "precioMayorista",
            precision = PRECIO_PRECISION, scale = PRECIO_SCALE)
    private BigDecimal precioMayorista;
    @Column(name = "stockActual", nullable = true,
            precision = STOCK_PRECISION, scale = STOCK_SCALE)
    private BigDecimal stockActual;
    @Column(name = "umbral", nullable = false,
            precision = STOCK_PRECISION, scale = STOCK_SCALE)
    private BigDecimal umbral;
    /* FK */
    @ManyToOne(optional = false) @JoinColumn(name = "idCategoria")
    private CategoriaEntity    categoria;
    @ManyToOne(optional = false) @JoinColumn(name = "idEstado")
    private EstadoEntity       estado;
    @ManyToOne(optional = false) @JoinColumn(name = "idTipoProducto")
    private TipoProductoEntity tipoProducto;
    /* Relaciones inversas */
    @jakarta.persistence.OneToMany(mappedBy = "producto")
    private java.util.Collection<TallaStockEntity> tallas;
    @jakarta.persistence.OneToMany(mappedBy = "producto")
    private java.util.Collection<PresentacionEntity> presentaciones;
    public ProductoEntity() {
    }
    public ProductoEntity(Integer idProducto) {
        this.idProducto = idProducto;
    }
    public ProductoEntity(Integer idProducto, String nombre, String unidadMedida,
                          BigDecimal precioUnitario, boolean mayorista,
                          boolean paraPedido, String tipoPedidoDefault,
                          BigDecimal stockActual, BigDecimal umbral) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.precioUnitario = precioUnitario;
        this.mayorista = mayorista;
        this.paraPedido = paraPedido;
        this.tipoPedidoDefault = tipoPedidoDefault;
        this.stockActual = stockActual;
        this.umbral = umbral;
    }
    public Integer getIdProducto() {
        return idProducto;
    }
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    public boolean getMayorista() {
        return mayorista;
    }
    public void setMayorista(boolean mayorista) {
        this.mayorista = mayorista;
    }
    public boolean getParaPedido() {
        return paraPedido;
    }
    public void setParaPedido(boolean paraPedido) {
        this.paraPedido = paraPedido;
    }
    public boolean isIgnorarUmbralHastaCero() { return ignorarUmbralHastaCero; }
    public void setIgnorarUmbralHastaCero(boolean ignorar) { this.ignorarUmbralHastaCero = ignorar; }
    public String getTipoPedidoDefault() {
        return tipoPedidoDefault;
    }
    public void setTipoPedidoDefault(String tipoPedidoDefault) {
        this.tipoPedidoDefault = tipoPedidoDefault;
    }
    public Integer getMinMayorista() {
        return minMayorista;
    } 
    public void setMinMayorista(Integer minMayorista) {
        this.minMayorista = minMayorista;
    }
    public BigDecimal getPrecioMayorista() {
        return precioMayorista;
    }
    public void setPrecioMayorista(BigDecimal precioMayorista) {
        this.precioMayorista = precioMayorista;
    }
    public BigDecimal getStockActual() {
        return stockActual;
    }
    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }
    public BigDecimal getUmbral() {
        return umbral;
    }
    public void setUmbral(BigDecimal umbral) {
        this.umbral = umbral;
    }
    public CategoriaEntity getCategoria() {
        return categoria;
    }
    public void setCategoria(CategoriaEntity categoria) {
        this.categoria = categoria;
    }
    public EstadoEntity getEstado() {
        return estado;
    }
    public void setEstado(EstadoEntity estado) {
        this.estado = estado;
    }
    public TipoProductoEntity getTipoProducto() {
        return tipoProducto;
    }
    public void setTipoProducto(TipoProductoEntity tipoProducto) {
        this.tipoProducto = tipoProducto;
    }
    public Collection<TallaStockEntity> getTallas() { return tallas; }
    public void setTallas(Collection<TallaStockEntity> tallas) { this.tallas = tallas; }
    public Collection<PresentacionEntity> getPresentaciones() { return presentaciones; }
    public void setPresentaciones(Collection<PresentacionEntity> pres) { this.presentaciones = pres; }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Producto[ idProducto=" + idProducto + " ]";
    } 
}

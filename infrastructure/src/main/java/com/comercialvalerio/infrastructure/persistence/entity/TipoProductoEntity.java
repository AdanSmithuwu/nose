package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.LEN_NOMBRE_CORTO;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Collection;

@Entity(name = "TipoProducto")
@Table(name = "TipoProducto",
       uniqueConstraints = @UniqueConstraint(name = "uk_tipo_producto_nombre",
                                             columnNames = "nombre"))
@NamedQueries({
    @NamedQuery(name = "TipoProducto.findAll",
                query = "FROM TipoProducto t ORDER BY t.nombre"),
    @NamedQuery(name = "TipoProducto.findByNombre",
                query = "FROM TipoProducto t WHERE UPPER(t.nombre)=UPPER(:nombre)"),
    @NamedQuery(name = "TipoProducto.countByNombreNotId",
                query = "SELECT COUNT(t) FROM TipoProducto t "
                      + "WHERE UPPER(t.nombre)=:n "
                      + "AND (:id IS NULL OR t.idTipoProducto <> :id)")
})
public class TipoProductoEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoProducto")
    private Integer idTipoProducto;
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CORTO)
    private String  nombre;
    /* Relación inversa opcional: un tipo puede tener muchos productos.
       No ponemos cascade REMOVE para impedir borrado accidental */
    @OneToMany(mappedBy = "tipoProducto")
    private Collection<ProductoEntity> productos;
    public TipoProductoEntity() {
    }
    public TipoProductoEntity(Integer idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }
    public TipoProductoEntity(Integer idTipoProducto, String nombre) {
        this.idTipoProducto = idTipoProducto;
        this.nombre = nombre;
    }
    public Integer getIdTipoProducto() {
        return idTipoProducto;
    }
    public void setIdTipoProducto(Integer idTipoProducto) {
        this.idTipoProducto = idTipoProducto;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Collection<ProductoEntity> getProductos() {
        return productos;
    }
    public void setProductos(Collection<ProductoEntity> productos) {
        this.productos = productos;
    } 
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.TipoProducto[ idTipoProducto=" + idTipoProducto + " ]";
    }
}

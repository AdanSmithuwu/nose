package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQueries;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "Categoria")
@Table(name = "Categoria",
       uniqueConstraints = @UniqueConstraint(name = "uk_categoria_nombre",
                                             columnNames = "nombre"))
@NamedQueries({
    @NamedQuery(name = "Categoria.findAll",
                query = "SELECT c FROM Categoria c ORDER BY c.nombre"),
    @NamedQuery(name = "Categoria.findByNombre",
                query = "SELECT c FROM Categoria c WHERE UPPER(c.nombre) = UPPER(:nombre)")
})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "Categoria.insert",
        procedureName = "dbo.sp_InsertCategoria",
        parameters = {
            @StoredProcedureParameter(name = "nombre",      mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "descripcion", mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "newIdCategoria", mode = ParameterMode.OUT, type = Integer.class)
        }),
    @NamedStoredProcedureQuery(name = "Categoria.update",
        procedureName = "dbo.sp_UpdateCategoria",
        parameters = {
            @StoredProcedureParameter(name = "idCategoria", mode = ParameterMode.IN, type = Integer.class),
            @StoredProcedureParameter(name = "nombre",      mode = ParameterMode.IN, type = String.class),
            @StoredProcedureParameter(name = "descripcion", mode = ParameterMode.IN, type = String.class)
        }),
    @NamedStoredProcedureQuery(name = "Categoria.cambiarEstado",
        procedureName = "dbo.sp_CambiarEstadoCategoria",
        parameters = {
            @StoredProcedureParameter(name = "idCategoria",        mode = ParameterMode.IN,  type = Integer.class),
            @StoredProcedureParameter(name = "nuevoEstado",        mode = ParameterMode.IN,  type = String.class),
            @StoredProcedureParameter(name = "actualizarProductos", mode = ParameterMode.IN,  type = Boolean.class),
            @StoredProcedureParameter(name = "numProductos",       mode = ParameterMode.OUT, type = Integer.class)
        }),
    @NamedStoredProcedureQuery(name = "Categoria.delete",
        procedureName = "dbo.sp_DeleteCategoria",
        parameters = {
            @StoredProcedureParameter(name = "idCategoria", mode = ParameterMode.IN, type = Integer.class)
        })
})
public class CategoriaEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria")
    private Integer idCategoria;
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CATEGORIA)
    private String  nombre;
    @Column(name = "descripcion", length = LEN_DESCRIPCION)
    private String  descripcion;
    @ManyToOne(optional = false) @JoinColumn(name = "idEstado")
    private EstadoEntity estado;
    /* Relación inversa: sin cascada de REMOVE para impedir borrado accidental */
    @OneToMany(mappedBy = "categoria")
    private Collection<ProductoEntity> productos;
    public CategoriaEntity() {
    }
    public CategoriaEntity(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    public CategoriaEntity(Integer idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }
    public Integer getIdCategoria() {
        return idCategoria;
    }
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoEntity getEstado() { return estado; }
    public void setEstado(EstadoEntity estado) { this.estado = estado; }
    public Collection<ProductoEntity> getProductos() {
        return productos;
    }
    public void setProductos(Collection<ProductoEntity> prods) {
        this.productos = prods;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.Categoria[ idCategoria=" + idCategoria + " ]";
    }  
}

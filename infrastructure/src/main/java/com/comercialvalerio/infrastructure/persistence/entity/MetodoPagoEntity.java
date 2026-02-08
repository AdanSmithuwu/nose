package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.*;

import jakarta.persistence.CascadeType;
import java.io.Serializable;
import java.util.Collection;
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

@Entity(name = "MetodoPago")
@Table(name = "MetodoPago",
       uniqueConstraints = @UniqueConstraint(name = "uk_metodo_pago_nombre",
                                             columnNames = "nombre"))
@NamedQueries({
    @NamedQuery(name = "MetodoPago.findAll",
                query = "FROM MetodoPago m ORDER BY m.nombre"),
    @NamedQuery(name = "MetodoPago.findByNombre",
                query = "FROM MetodoPago m WHERE UPPER(m.nombre)=UPPER(:nombre)"),
    @NamedQuery(name = "MetodoPago.countByNombreNotId",
                query = "SELECT COUNT(m) FROM MetodoPago m "
                      + "WHERE UPPER(m.nombre)=:n "
                      + "AND (:id IS NULL OR m.idMetodoPago <> :id)"),
    @NamedQuery(name = "MetodoPago.findAllById",
                query = "FROM MetodoPago m "
                      + "WHERE m.idMetodoPago IN :ids "
                      + "ORDER BY m.idMetodoPago")
})
public class MetodoPagoEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMetodoPago")
    private Integer idMetodoPago;
    @Column(name = "nombre", nullable = false, length = LEN_NOMBRE_CORTO)
    private String  nombre;
    @OneToMany(mappedBy="metodoPago", cascade=CascadeType.ALL, orphanRemoval=true)
    private Collection<PagoTransaccionEntity> pagos;
    public MetodoPagoEntity() {
    }
    public MetodoPagoEntity(Integer idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }
    public MetodoPagoEntity(Integer idMetodoPago, String nombre) {
        this.idMetodoPago = idMetodoPago;
        this.nombre = nombre;
    }
    public Integer getIdMetodoPago() {
        return idMetodoPago;
    }
    public void setIdMetodoPago(Integer idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Collection<PagoTransaccionEntity> getPagos() {
        return pagos;
    }
    public void setPagos(Collection<PagoTransaccionEntity> pagos) {
        this.pagos = pagos;
    }
    @Override
    public String toString() {
        return "com.comercialvalerio.infrastructure.persistence.entity.MetodoPago[ idMetodoPago=" + idMetodoPago + " ]";
    }
}

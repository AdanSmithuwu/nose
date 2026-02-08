package com.comercialvalerio.infrastructure.persistence.entity;

import static com.comercialvalerio.common.DbConstraints.LEN_NOMBRE_COMPLETO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;

@ReadOnly
@Entity(name = "ClienteFrecuente")
@Table(name = "vw_ClientesFrecuentes")
@NamedNativeQuery(name = "ClienteFrecuente.top",
    query = "EXEC dbo.sp_ListarClientesFrecuentes ?",
    resultClass = ClienteFrecuenteEntity.class)
public class ClienteFrecuenteEntity implements Serializable {
    @Id
    @Column(name = "idCliente")
    private Integer idCliente;
    @Column(name = "nombre", length = LEN_NOMBRE_COMPLETO)
    private String nombre;
    @Column(name = "numCompras")
    private Integer numCompras;
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer id) { this.idCliente = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getNumCompras() { return numCompras; }
    public void setNumCompras(Integer numCompras) { this.numCompras = numCompras; }
}

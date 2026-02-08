package com.comercialvalerio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.ReadOnly;
import java.io.Serializable;

@ReadOnly
@Entity(name = "ClienteActivo")
@Table(name = "vw_ClientesActivos")
public class ClienteActivoEntity implements Serializable {
    @Id
    @Column(name = "idPersona")
    private Integer idPersona;

    public Integer getIdPersona() {
        return idPersona;
    }
    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }
}

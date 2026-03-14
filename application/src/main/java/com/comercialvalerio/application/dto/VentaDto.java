package com.comercialvalerio.application.dto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import com.comercialvalerio.common.json.OffsetDateTimeAdapter;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record VentaDto(
    Integer       idTransaccion,
    @JsonbDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSXXX")
    @JsonbTypeAdapter(OffsetDateTimeAdapter.class)
    OffsetDateTime fecha,
    String        estado,
    BigDecimal    totalNeto,
    String        observacion,
    Integer       empleadoId,
    String        empleadoUsuario,
    Integer       clienteId,
    String        clienteNombre
) {
    @JsonbCreator
    public VentaDto(
        @JsonbProperty("idTransaccion") Integer idTransaccion,
        @JsonbProperty("fecha")
        @JsonbDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSXXX")
        @JsonbTypeAdapter(OffsetDateTimeAdapter.class) OffsetDateTime fecha,
        @JsonbProperty("estado") String estado,
        @JsonbProperty("totalNeto") BigDecimal totalNeto,
        @JsonbProperty("observacion") String observacion,
        @JsonbProperty("empleadoId") Integer empleadoId,
        @JsonbProperty("empleadoUsuario") String empleadoUsuario,
        @JsonbProperty("clienteId") Integer clienteId,
        @JsonbProperty("clienteNombre") String clienteNombre
    ) {
        this.idTransaccion = idTransaccion;
        this.fecha = fecha;
        this.estado = estado;
        this.totalNeto = totalNeto;
        this.observacion = observacion;
        this.empleadoId = empleadoId;
        this.empleadoUsuario = empleadoUsuario;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
    }
}

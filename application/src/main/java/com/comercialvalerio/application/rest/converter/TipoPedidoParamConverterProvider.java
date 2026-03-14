package com.comercialvalerio.application.rest.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.comercialvalerio.application.dto.TipoPedido;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/** Convierte valores de TipoPedido usando su nombre legible. */
@Provider
public class TipoPedidoParamConverterProvider implements ParamConverterProvider {
    private final ParamConverter<TipoPedido> converter = new ParamConverter<>() {
        @Override
        public TipoPedido fromString(String value) {
            if (value == null || value.isBlank()) return null;
            try {
                return TipoPedido.fromNombre(value);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(ex.getMessage(), ex);
            }
        }
        @Override
        public String toString(TipoPedido value) {
            return value == null ? null : value.getNombre();
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(TipoPedido.class)) {
            return (ParamConverter<T>) converter;
        }
        return null;
    }
}

package com.comercialvalerio.application.rest.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.comercialvalerio.domain.model.NombreComparable;
import com.comercialvalerio.domain.util.EnumByName;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

/** Convierte parámetros de enumeraciones que implementan {@link NombreComparable}. */
@Provider
public class NombreComparableParamConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.isEnum() && NombreComparable.class.isAssignableFrom(rawType)) {
            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) rawType;
            return new ParamConverter<>() {
                @Override
                public T fromString(String value) {
                    if (value == null || value.isBlank()) return null;
                    try {
                        return (T) EnumByName.fromNombre((Class) enumType, value);
                    } catch (IllegalArgumentException ex) {
                        throw new BadRequestException(ex.getMessage(), ex);
                    }
                }

                @Override
                public String toString(T value) {
                    return value == null ? null : ((NombreComparable) value).getNombre();
                }
            };
        }
        return null;
    }
}

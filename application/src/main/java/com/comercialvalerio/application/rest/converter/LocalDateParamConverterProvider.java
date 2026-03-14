package com.comercialvalerio.application.rest.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LocalDateParamConverterProvider implements ParamConverterProvider {
    private final ParamConverter<LocalDate> converter = new ParamConverter<>() {
        @Override
        public LocalDate fromString(String value) {
            if (value == null || value.isBlank()) return null;
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("Formato de fecha inválido. Use ISO-8601.", ex);
            }
        }
        @Override
        public String toString(LocalDate value) {
            return value == null ? null : value.toString();
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(LocalDate.class)) {
            return (ParamConverter<T>) converter;
        }
        return null;
    }
}

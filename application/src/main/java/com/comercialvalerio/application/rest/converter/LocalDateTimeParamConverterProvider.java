package com.comercialvalerio.application.rest.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LocalDateTimeParamConverterProvider implements ParamConverterProvider {
    private final ParamConverter<LocalDateTime> converter = new ParamConverter<>() {
        @Override
        public LocalDateTime fromString(String value) {
            if (value == null || value.isBlank()) return null;
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException ex) {
                throw new BadRequestException("Formato de fecha y hora inválido. Use ISO-8601.", ex);
            }
        }
        @Override
        public String toString(LocalDateTime value) {
            return value == null ? null : value.toString();
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(LocalDateTime.class)) {
            return (ParamConverter<T>) converter;
        }
        return null;
    }
}

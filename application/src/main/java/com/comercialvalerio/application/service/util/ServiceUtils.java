package com.comercialvalerio.application.service.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** Utilidades de apoyo para las implementaciones de servicio. */
public final class ServiceUtils {
    private ServiceUtils() {
    }

    /**
     * Mapea la colección proporcionada usando la función indicada.
     *
     * @param models  elementos a mapear
     * @param mapper  función de mapeo
     * @return lista mapeada
     */
    public static <M, D> List<D> mapList(Collection<M> models, Function<M, D> mapper) {
        if (models == null) {
            return List.of();
        }
        return models.stream().map(mapper).toList();
    }
}

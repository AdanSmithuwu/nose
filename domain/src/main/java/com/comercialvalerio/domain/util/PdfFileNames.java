package com.comercialvalerio.domain.util;

import java.time.format.DateTimeFormatter;

import com.comercialvalerio.domain.model.Comprobante;
import com.comercialvalerio.domain.model.OrdenCompraPdf;

/** Utilidades para construir nombres de archivos PDF. */
public final class PdfFileNames {
    private PdfFileNames() {}

    /** Formateador estándar de fecha y hora. */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Devuelve el nombre de archivo para una orden de compra.
     * Formato: {@code orden-&lt;id&gt;-&lt;dni&gt;-&lt;fecha&gt;.pdf}
     */
    public static String ordenCompra(OrdenCompraPdf oc) {
        var pedido = oc.getPedido();
        var cliente = pedido.getCliente();
        String dni = cliente == null ? "sindni" : cliente.getDni();
        String id = String.valueOf(pedido.getIdTransaccion());
        String ts = oc.getFechaGeneracion().format(FMT);
        return "orden-" + id + '-' + dni + '-' + ts + ".pdf";
    }

    /**
     * Devuelve el nombre de archivo para un comprobante.
     * Formato: {@code comprobante-&lt;id&gt;-&lt;dni&gt;-&lt;fecha&gt;.pdf}
     */
    public static String comprobante(Comprobante c) {
        var tx = c.getTransaccion();
        var cliente = tx.getCliente();
        String dni = cliente == null ? "sindni" : cliente.getDni();
        String id = String.valueOf(tx.getIdTransaccion());
        String ts = c.getFechaEmision().format(FMT);
        return "comprobante-" + id + '-' + dni + '-' + ts + ".pdf";
    }
}

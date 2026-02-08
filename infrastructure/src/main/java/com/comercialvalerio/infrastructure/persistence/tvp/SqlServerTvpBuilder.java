package com.comercialvalerio.infrastructure.persistence.tvp;
import java.sql.SQLException;
import java.util.List;

import com.comercialvalerio.domain.model.DetalleTransaccion;
import com.comercialvalerio.domain.model.PagoTransaccion;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.comercialvalerio.common.DbConstraints;

/*
 * Construye los TVP que el procedimiento almacenado espera.
 */
public final class SqlServerTvpBuilder {
    private SqlServerTvpBuilder() { }

    /* tvp_DetalleTx */
    public static SQLServerDataTable detalle(List<DetalleTransaccion> det) throws SQLException {
        SQLServerDataTable tvp = new SQLServerDataTable();
        tvp.addColumnMetadata("idProducto"   , java.sql.Types.INTEGER);
        tvp.addColumnMetadata("idTallaStock" , java.sql.Types.INTEGER);
        tvp.addColumnMetadata("cantidad", java.sql.Types.DECIMAL);
        tvp.addColumnMetadata("precioUnitario", java.sql.Types.DECIMAL);
        for (DetalleTransaccion d : det) {
            tvp.addRow(d.getProducto().getIdProducto(),
                       d.getTallaStock()==null ? null : d.getTallaStock().getIdTallaStock(),
                       d.getCantidad().setScale(DbConstraints.STOCK_SCALE,
                           java.math.RoundingMode.HALF_UP),
                       d.getPrecioUnitario().setScale(DbConstraints.PRECIO_SCALE,
                           java.math.RoundingMode.HALF_UP));
        }
        return tvp;
    }
    /* tvp_PagoTx */
    public static SQLServerDataTable pagos(List<PagoTransaccion> pagos) throws SQLException {
        SQLServerDataTable tvp = new SQLServerDataTable();
        tvp.addColumnMetadata("idMetodoPago", java.sql.Types.INTEGER);
        tvp.addColumnMetadata("monto", java.sql.Types.DECIMAL);
        for (PagoTransaccion p : pagos) {
            tvp.addRow(p.getMetodoPago().getIdMetodoPago(),
                       p.getMonto().setScale(
                           DbConstraints.PRECIO_SCALE,
                           java.math.RoundingMode.HALF_UP));
        }
        return tvp;
    }
}

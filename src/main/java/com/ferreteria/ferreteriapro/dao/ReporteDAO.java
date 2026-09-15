package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReporteDAO {

    public Map<String, Double> obtenerResumenVentasMensuales(String mesAno) throws SQLException {
        Map<String, Double> resumen = new HashMap<>();
        // Consultar sumatorias agrupadas por método de pago de ventas en historico_ventas y ventas
        String sql = "SELECT metodo_pago, SUM(total) as total_ventas " +
                     "FROM (" +
                     "  SELECT metodo_pago, total, fecha FROM ventas " +
                     "  UNION ALL " +
                     "  SELECT metodo_pago, total, fecha FROM historico_ventas" +
                     ") WHERE fecha LIKE ? " +
                     "GROUP BY metodo_pago";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mesAno + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                double total = 0;
                while (rs.next()) {
                    String metodo = rs.getString("metodo_pago");
                    double totalMetodo = rs.getDouble("total_ventas");
                    resumen.put(metodo, totalMetodo);
                    total += totalMetodo;
                }
                resumen.put("TOTAL", total);
            }
        }
        return resumen;
    }

    public double obtenerCostoMercanciaMes(String mesAno) throws SQLException {
        // Calcular el costo total acumulado
        String sql = "SELECT SUM(costo_unitario * cantidad) as total_costo " +
                     "FROM (" +
                     "  SELECT costo_unitario, cantidad, fecha FROM historico_ventas " +
                     "  UNION ALL " +
                     "  SELECT total / cantidad as costo_unitario, cantidad, fecha FROM ventas" + // costo aproximado si no se guardó costo_unitario
                     ") WHERE fecha LIKE ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mesAno + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_costo");
                }
            }
        }
        return 0;
    }
}

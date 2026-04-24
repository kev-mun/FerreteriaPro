package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.CierreCaja;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CierreCajaDAO {

    public CierreCajaDAO() {
        String sql = "CREATE TABLE IF NOT EXISTS cierres_caja (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "fecha TEXT UNIQUE," +
                     "total_ventas REAL," +
                     "total_costos REAL," +
                     "ganancia REAL," +
                     "efectivo REAL," +
                     "transferencia REAL" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void guardar(CierreCaja c) throws SQLException {
        String sql = "INSERT OR REPLACE INTO cierres_caja (fecha, total_ventas, total_costos, ganancia, efectivo, transferencia) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFecha());
            pstmt.setDouble(2, c.getTotalVentas());
            pstmt.setDouble(3, c.getTotalCostos());
            pstmt.setDouble(4, c.getGanancia());
            pstmt.setDouble(5, c.getEfectivo());
            pstmt.setDouble(6, c.getTransferencia());
            pstmt.executeUpdate();
        }
    }

    public List<CierreCaja> listarTodo() throws SQLException {
        List<CierreCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM cierres_caja ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new CierreCaja(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getDouble("total_ventas"),
                        rs.getDouble("total_costos"),
                        rs.getDouble("ganancia"),
                        rs.getDouble("efectivo"),
                        rs.getDouble("transferencia")));
            }
        }
        return lista;
    }
}

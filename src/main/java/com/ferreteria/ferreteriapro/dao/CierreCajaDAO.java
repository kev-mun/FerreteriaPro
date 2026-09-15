package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.CierreCaja;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CierreCajaDAO {

    public CierreCajaDAO() {
        String sql = "CREATE TABLE IF NOT EXISTS cierres_caja (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fecha TEXT, " +
                "total_ventas REAL, " +
                "total_costos REAL, " +
                "ganancia REAL, " +
                "efectivo REAL, " +
                "transferencia REAL, " +
                "estado TEXT, " +
                "base_inicial REAL, " +
                "base_siguiente REAL" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Migrar columnas si la tabla ya existía sin ellas
            try {
                stmt.execute("ALTER TABLE cierres_caja ADD COLUMN estado TEXT DEFAULT 'CERRADO'");
            } catch (SQLException ignored) {
            }
            try {
                stmt.execute("ALTER TABLE cierres_caja ADD COLUMN base_inicial REAL DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.execute("ALTER TABLE cierres_caja ADD COLUMN base_siguiente REAL DEFAULT 0");
            } catch (SQLException ignored) {
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Guarda (INSERT OR REPLACE) un cierre ya procesado. */
    public void guardar(CierreCaja c) throws SQLException {
        String sql = "INSERT OR REPLACE INTO cierres_caja " +
                "(fecha, total_ventas, total_costos, ganancia, efectivo, transferencia, estado, base_inicial, base_siguiente) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getFecha());
            pstmt.setDouble(2, c.getTotalVentas());
            pstmt.setDouble(3, c.getTotalCostos());
            pstmt.setDouble(4, c.getGanancia());
            pstmt.setDouble(5, c.getEfectivo());
            pstmt.setDouble(6, c.getTransferencia());
            pstmt.setString(7, c.getEstado());
            pstmt.setDouble(8, c.getBaseInicial());
            pstmt.setDouble(9, c.getBaseSiguiente());
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
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /** Retorna el registro más reciente (cualquier estado). */
    public CierreCaja obtenerUltimoCierre() throws SQLException {
        String sql = "SELECT * FROM cierres_caja ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    /**
     * Retorna el último cierre en estado 'CERRADO' (excluyendo turnos ABIERTOS).
     * Útil para recuperar la baseSiguiente del día anterior.
     */
    public CierreCaja obtenerUltimoCierreCerrado() throws SQLException {
        String sql = "SELECT * FROM cierres_caja WHERE estado = 'CERRADO' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    /**
     * Registra un turno con estado ABIERTO al iniciar el día.
     * Es idempotente: si ya existe un turno ABIERTO para hoy, no hace nada.
     */
    public void abrirTurno(double baseInicial) throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        // Verificar si ya hay un turno ABIERTO para hoy
        String sqlCheck = "SELECT COUNT(*) FROM cierres_caja WHERE fecha = ? AND estado = 'ABIERTO'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, hoy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Ya hay turno abierto hoy, no duplicar
                }
            }
        }
        String sql = "INSERT INTO cierres_caja " +
                "(fecha, total_ventas, total_costos, ganancia, efectivo, transferencia, estado, base_inicial, base_siguiente) "
                +
                "VALUES (?, 0, 0, 0, 0, 0, 'ABIERTO', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoy);
            ps.setDouble(2, baseInicial);
            ps.setDouble(3, baseInicial); // base_siguiente provisional
            ps.executeUpdate();
        }
    }

    /**
     * Retorna el turno ABIERTO de hoy (si existe), o null.
     * Permite detectar cierres accidentales vs. cierres reales.
     */
    public CierreCaja obtenerTurnoAbierto() throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        String sql = "SELECT * FROM cierres_caja WHERE fecha = ? AND estado = 'ABIERTO' ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    private CierreCaja mapRow(ResultSet rs) throws SQLException {
        return new CierreCaja(
                rs.getInt("id"),
                rs.getString("fecha"),
                rs.getDouble("total_ventas"),
                rs.getDouble("total_costos"),
                rs.getDouble("ganancia"),
                rs.getDouble("efectivo"),
                rs.getDouble("transferencia"),
                rs.getString("estado"),
                rs.getDouble("base_inicial"),
                rs.getDouble("base_siguiente"));
    }
}

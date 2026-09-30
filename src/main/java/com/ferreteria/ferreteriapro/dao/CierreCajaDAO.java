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
            try {
                stmt.execute("ALTER TABLE cierres_caja ADD COLUMN fecha_cierre TEXT");
            } catch (SQLException ignored) {
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Guarda o actualiza un cierre ya procesado. */
    public void guardar(CierreCaja c) throws SQLException {
        String hoy = (c.getFecha() != null && !c.getFecha().isEmpty()) ? c.getFecha() : java.time.LocalDate.now().toString();
        String hoyHora = java.time.LocalDateTime.now().toString();

        // Buscar si existe un registro para esta fecha
        String sqlCheck = "SELECT id FROM cierres_caja WHERE fecha = ? OR fecha LIKE ? ORDER BY id DESC LIMIT 1";
        Integer idExistente = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, hoy);
            ps.setString(2, hoy + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idExistente = rs.getInt("id");
                }
            }
        }

        if (idExistente != null) {
            String sqlUpdate = "UPDATE cierres_caja SET total_ventas = ?, total_costos = ?, ganancia = ?, " +
                    "efectivo = ?, transferencia = ?, estado = ?, base_inicial = ?, base_siguiente = ?, fecha_cierre = ? " +
                    "WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, c.getTotalVentas());
                pstmt.setDouble(2, c.getTotalCostos());
                pstmt.setDouble(3, c.getGanancia());
                pstmt.setDouble(4, c.getEfectivo());
                pstmt.setDouble(5, c.getTransferencia());
                pstmt.setString(6, c.getEstado() != null ? c.getEstado() : "CERRADO");
                pstmt.setDouble(7, c.getBaseInicial());
                pstmt.setDouble(8, c.getBaseSiguiente());
                pstmt.setString(9, hoyHora);
                pstmt.setInt(10, idExistente);
                pstmt.executeUpdate();
            }
        } else {
            String sqlInsert = "INSERT INTO cierres_caja " +
                    "(fecha, total_ventas, total_costos, ganancia, efectivo, transferencia, estado, base_inicial, base_siguiente, fecha_cierre) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setString(1, hoy);
                pstmt.setDouble(2, c.getTotalVentas());
                pstmt.setDouble(3, c.getTotalCostos());
                pstmt.setDouble(4, c.getGanancia());
                pstmt.setDouble(5, c.getEfectivo());
                pstmt.setDouble(6, c.getTransferencia());
                pstmt.setString(7, c.getEstado() != null ? c.getEstado() : "CERRADO");
                pstmt.setDouble(8, c.getBaseInicial());
                pstmt.setDouble(9, c.getBaseSiguiente());
                pstmt.setString(10, hoyHora);
                pstmt.executeUpdate();
            }
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
     * Retorna el último cierre en estado 'CERRADO' de un día anterior (excluyendo turnos de hoy).
     * Útil para recuperar la baseSiguiente del día anterior como sugerencia.
     */
    public CierreCaja obtenerUltimoCierreCerrado() throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        String sql = "SELECT * FROM cierres_caja WHERE UPPER(COALESCE(estado, '')) IN ('CERRADO', 'CERRADA') " +
                "AND fecha != ? AND fecha NOT LIKE ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoy);
            ps.setString(2, hoy + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        // Fallback: si no hay cierre anterior a hoy, retornar el último cerrado
        String sqlFallback = "SELECT * FROM cierres_caja WHERE UPPER(COALESCE(estado, '')) IN ('CERRADO', 'CERRADA') ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlFallback)) {
            if (rs.next())
                return mapRow(rs);
        }
        return null;
    }

    /**
     * Registra o reactiva un turno con estado ABIERTO al iniciar el día.
     * Es idempotente: si ya existe un turno ABIERTO para hoy, no hace nada.
     * Si existía un registro previo para hoy, lo actualiza a ABIERTO respetando el constraint UNIQUE de fecha.
     */
    public void abrirTurno(double baseInicial) throws SQLException {
        CierreCaja existente = obtenerTurnoAbierto();
        if (existente != null) {
            return; // Ya hay turno abierto para hoy, no duplicar
        }

        String hoy = java.time.LocalDate.now().toString();

        // Verificar si ya existe un registro para hoy en la BD (para actualizarlo en vez de fallar por UNIQUE)
        String sqlCheck = "SELECT id FROM cierres_caja WHERE fecha = ? OR fecha LIKE ? ORDER BY id DESC LIMIT 1";
        Integer idExistente = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, hoy);
            ps.setString(2, hoy + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idExistente = rs.getInt("id");
                }
            }
        }

        if (idExistente != null) {
            String sqlUpdate = "UPDATE cierres_caja SET estado = 'ABIERTO', base_inicial = ?, base_siguiente = ?, fecha_cierre = NULL WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, baseInicial);
                ps.setDouble(2, baseInicial);
                ps.setInt(3, idExistente);
                ps.executeUpdate();
            }
        } else {
            String sqlInsert = "INSERT INTO cierres_caja " +
                    "(fecha, total_ventas, total_costos, ganancia, efectivo, transferencia, estado, base_inicial, base_siguiente, fecha_cierre) "
                    +
                    "VALUES (?, 0, 0, 0, 0, 0, 'ABIERTO', ?, ?, NULL)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, hoy);
                ps.setDouble(2, baseInicial);
                ps.setDouble(3, baseInicial);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Retorna el turno ABIERTO de hoy (si existe), o null.
     * Busca si existe un registro de apertura de caja para el día de hoy cuyo estado sea ABIERTO/ABIERTA (o fecha_cierre IS NULL).
     */
    public CierreCaja obtenerTurnoAbierto() throws SQLException {
        String hoy = java.time.LocalDate.now().toString();
        String sql = "SELECT * FROM cierres_caja " +
                "WHERE (fecha = ? OR fecha LIKE ?) " +
                "AND (UPPER(COALESCE(estado, '')) IN ('ABIERTO', 'ABIERTA') OR fecha_cierre IS NULL) " +
                "AND UPPER(COALESCE(estado, '')) NOT IN ('CERRADO', 'CERRADA') " +
                "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoy);
            ps.setString(2, hoy + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    private CierreCaja mapRow(ResultSet rs) throws SQLException {
        CierreCaja c = new CierreCaja(
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
        try {
            c.setFechaCierre(rs.getString("fecha_cierre"));
        } catch (SQLException ignored) {
        }
        return c;
    }
}

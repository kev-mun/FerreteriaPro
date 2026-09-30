package com.ferreteria.ferreteriapro.adapter.out.persistence;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.domain.model.ConsumoInterno;
import com.ferreteria.ferreteriapro.domain.port.out.ConsumoRepositoryPort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de persistencia para consumos internos.
 * Implementa el puerto de salida usando JDBC/SQLite,
 * siguiendo la misma estructura de manejo de conexiones que EntradaDAO.
 */
public class ConsumoDaoAdapter implements ConsumoRepositoryPort {

    /**
     * Crea la tabla consumos_internos si no existe.
     * Debe invocarse al inicializar la aplicación (ej. en DatabaseConnection.inicializarBaseDeDatos).
     */
    public static void crearTabla() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS consumos_internos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    producto_codigo TEXT NOT NULL,
                    cantidad INTEGER NOT NULL,
                    motivo TEXT NOT NULL,
                    usuario_id TEXT,
                    fecha DATETIME DEFAULT CURRENT_TIMESTAMP
                );
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void guardar(Connection conn, ConsumoInterno consumo) throws SQLException {
        String sql = "INSERT INTO consumos_internos (producto_codigo, cantidad, motivo, usuario_id) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, consumo.getProductoCodigo());
            pstmt.setInt(2, consumo.getCantidad());
            pstmt.setString(3, consumo.getMotivo());
            pstmt.setString(4, consumo.getUsuarioId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("busy") || e.getErrorCode() == 5) {
                throw new SQLException("La base de datos está bloqueada. Cierra otros programas que la usen.", e);
            }
            throw e;
        }
    }

    @Override
    public List<ConsumoInterno> listarTodo() throws SQLException {
        List<ConsumoInterno> lista = new ArrayList<>();
        String sql = "SELECT c.*, p.nombre AS producto_nombre " +
                     "FROM consumos_internos c " +
                     "LEFT JOIN productos p ON c.producto_codigo = p.codigo " +
                     "ORDER BY c.fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearConsumo(rs));
            }
        }
        return lista;
    }

    @Override
    public List<ConsumoInterno> listarPorFecha(String fechaInicio, String fechaFin) throws SQLException {
        List<ConsumoInterno> lista = new ArrayList<>();
        String sql = "SELECT c.*, p.nombre AS producto_nombre " +
                     "FROM consumos_internos c " +
                     "LEFT JOIN productos p ON c.producto_codigo = p.codigo " +
                     "WHERE DATE(c.fecha) BETWEEN ? AND ? " +
                     "ORDER BY c.fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearConsumo(rs));
                }
            }
        }
        return lista;
    }

    private ConsumoInterno mapearConsumo(ResultSet rs) throws SQLException {
        return new ConsumoInterno(
                rs.getInt("id"),
                rs.getString("producto_codigo"),
                rs.getString("producto_nombre"),
                rs.getInt("cantidad"),
                rs.getString("motivo"),
                rs.getString("usuario_id"),
                rs.getString("fecha")
        );
    }
}

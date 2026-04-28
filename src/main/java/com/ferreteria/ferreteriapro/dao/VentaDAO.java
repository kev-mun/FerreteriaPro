package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Venta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public VentaDAO() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Verificación y creación de columnas necesarias
            try {
                stmt.execute("ALTER TABLE ventas ADD COLUMN costo_unitario REAL DEFAULT 0");
            } catch (SQLException ignored) {
            }
            try {
                stmt.execute("ALTER TABLE ventas ADD COLUMN estado TEXT DEFAULT 'ACTIVA'");
            } catch (SQLException ignored) {
            }
            try {
                stmt.execute("ALTER TABLE historico_ventas ADD COLUMN estado TEXT DEFAULT 'ACTIVA'");
            } catch (SQLException ignored) {
            }

            // Tabla de notificaciones para auditoría
            stmt.execute("CREATE TABLE IF NOT EXISTS notificaciones (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "mensaje TEXT, " +
                    "fecha DATETIME DEFAULT CURRENT_TIMESTAMP)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void guardar(Venta v) throws SQLException {
        String sql = "INSERT INTO ventas (fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVA')";
        ejecutarUpdate(sql, v.getFecha(), v.getProductoCodigo(), v.getProductoNombre(), v.getCantidad(), v.getTotal(),
                v.getMetodoPago(), v.getCostoUnitario(), v.getUsuarioNombre());
    }

    private void ejecutarUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public List<Venta> listarTodo() throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM ventas ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Venta(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getString("producto_codigo"),
                        rs.getString("producto_nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("total"),
                        rs.getString("metodo_pago"),
                        rs.getDouble("costo_unitario"),
                        rs.getString("usuario_nombre"),
                        rs.getString("estado")));
            }
        }
        return lista;
    }

    // --- MÉTODOS REQUERIDOS POR INVENTARIOSERVICE ---

    public void archivarVentas(List<Venta> ventas) throws SQLException {
        String sql = "INSERT INTO historico_ventas (fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVA')";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Venta v : ventas) {
                    pstmt.setString(1, v.getFecha());
                    pstmt.setString(2, v.getProductoCodigo());
                    pstmt.setString(3, v.getProductoNombre());
                    pstmt.setInt(4, v.getCantidad());
                    pstmt.setDouble(5, v.getTotal());
                    pstmt.setString(6, v.getMetodoPago());
                    pstmt.setDouble(7, v.getCostoUnitario());
                    pstmt.setString(8, v.getUsuarioNombre());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    public void limpiarVentas() throws SQLException {
        ejecutarUpdate("DELETE FROM ventas");
    }

    public List<Venta> obtenerVentasPorMes(String mesAno) throws SQLException {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT * FROM (" +
                "  SELECT id, fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre, estado FROM ventas "
                +
                "  UNION ALL " +
                "  SELECT id, fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre, estado FROM historico_ventas "
                +
                ") AS combinadas " +
                "WHERE fecha LIKE ? " +
                "ORDER BY fecha DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mesAno + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Venta(
                            rs.getInt("id"),
                            rs.getString("fecha"),
                            rs.getString("producto_codigo"),
                            rs.getString("producto_nombre"),
                            rs.getInt("cantidad"),
                            rs.getDouble("total"),
                            rs.getString("metodo_pago"),
                            rs.getDouble("costo_unitario"),
                            rs.getString("usuario_nombre"),
                            rs.getString("estado")));
                }
            }
        }
        return lista;
    }

    public boolean revertirVenta(Venta v) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Marcar como ANULADA
                String sqlAnular = "UPDATE ventas SET estado = 'ANULADA' WHERE id = ?";
                try (PreparedStatement psAnular = conn.prepareStatement(sqlAnular)) {
                    psAnular.setInt(1, v.getId());
                    psAnular.executeUpdate();
                }

                // 2. Devolver stock
                String sqlStock = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";
                try (PreparedStatement psStock = conn.prepareStatement(sqlStock)) {
                    psStock.setInt(1, v.getCantidad());
                    psStock.setString(2, v.getProductoCodigo());
                    psStock.executeUpdate();
                }

                // 3. Notificación
                String sqlNotif = "INSERT INTO notificaciones (mensaje) VALUES (?)";
                try (PreparedStatement psNotif = conn.prepareStatement(sqlNotif)) {
                    psNotif.setString(1, "Venta #" + v.getId() + " (" + v.getProductoNombre() + ") anulada. Stock devuelto.");
                    psNotif.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
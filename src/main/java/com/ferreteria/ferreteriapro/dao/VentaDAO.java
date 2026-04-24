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
            stmt.execute("ALTER TABLE ventas ADD COLUMN costo_unitario REAL DEFAULT 0");
        } catch (SQLException ignored) {}
    }

    public void guardar(Venta v) throws SQLException {
        String sql = "INSERT INTO ventas (fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
                        rs.getString("usuario_nombre")));
            }
        }
        return lista;
    }

    public void archivarVentas(List<Venta> ventas) throws SQLException {
        String sql = "INSERT INTO historico_ventas (fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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
                }
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
                     "  SELECT id, fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre FROM ventas " +
                     "  UNION ALL " +
                     "  SELECT id, fecha, producto_codigo, producto_nombre, cantidad, total, metodo_pago, costo_unitario, usuario_nombre FROM historico_ventas " +
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
                            rs.getString("usuario_nombre")));
                }
            }
        }
        return lista;
    }
}

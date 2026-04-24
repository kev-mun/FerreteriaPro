package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.EntradaInventario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntradaDAO {

    public void guardar(EntradaInventario e) throws SQLException {
        String sql = "INSERT INTO entradas_inventario (producto_codigo, cantidad, costo_unitario, fecha, proveedor, usuario_nombre) VALUES (?, ?, ?, ?, ?, ?)";
        ejecutarUpdate(sql, e.getProductoCodigo(), e.getCantidad(), e.getCostoUnitario(), e.getFecha(),
                e.getProveedor(), e.getUsuarioNombre());
    }

    public void actualizar(EntradaInventario e) throws SQLException {
        String sql = "UPDATE entradas_inventario SET cantidad = ?, costo_unitario = ?, fecha = ?, proveedor = ? WHERE id = ?";
        ejecutarUpdate(sql, e.getCantidad(), e.getCostoUnitario(), e.getFecha(), e.getProveedor(), e.getId());
    }

    private void ejecutarUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("busy") || e.getErrorCode() == 5) {
                throw new SQLException("La base de datos está bloqueada. Cierra otros programas que la usen.", e);
            }
            throw e;
        }
    }

    public List<EntradaInventario> listarTodo() throws SQLException {
        List<EntradaInventario> lista = new ArrayList<>();
        String sql = "SELECT e.*, p.nombre as producto_nombre " +
                     "FROM entradas_inventario e " +
                     "LEFT JOIN productos p ON e.producto_codigo = p.codigo " +
                     "ORDER BY e.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new EntradaInventario(
                        rs.getInt("id"),
                        rs.getString("producto_codigo"),
                        rs.getString("producto_nombre"),
                        rs.getInt("cantidad"),
                        rs.getDouble("costo_unitario"),
                        rs.getString("fecha"),
                        rs.getString("proveedor"),
                        rs.getString("usuario_nombre")));
            }
        }
        return lista;
    }

    public void limpiarEntradas() throws SQLException {
        ejecutarUpdate("DELETE FROM entradas_inventario");
    }

    public void archivarHistorico(List<EntradaInventario> lista) throws SQLException {
        String sql = "INSERT INTO historico_compras (producto_codigo, cantidad, costo_unitario, fecha, proveedor, usuario_nombre) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (EntradaInventario e : lista) {
                    pstmt.setString(1, e.getProductoCodigo());
                    pstmt.setInt(2, e.getCantidad());
                    pstmt.setDouble(3, e.getCostoUnitario());
                    pstmt.setString(4, e.getFecha());
                    pstmt.setString(5, e.getProveedor());
                    pstmt.setString(6, e.getUsuarioNombre());
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
}

package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void guardar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, precio_venta, stock) VALUES (?, ?, ?, ?)";
        ejecutarUpdate(sql, p.getCodigo(), p.getNombre(), p.getPrecioVenta(), p.getStock());
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, precio_venta = ?, stock = ? WHERE codigo = ?";
        ejecutarUpdate(sql, p.getNombre(), p.getPrecioVenta(), p.getStock(), p.getCodigo());
    }

    public void eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM productos WHERE codigo = ?";
        ejecutarUpdate(sql, codigo);
    }

    // Método privado para evitar repetir el manejo de errores de base de datos ocupada
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

    public List<Producto> listarTodo() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Producto(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("stock")
                ));
            }
        }
        return lista;
    }
}
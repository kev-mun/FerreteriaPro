package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void guardar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, precio_compra, precio_venta, stock, activo, proveedor_nombre) VALUES (?, ?, ?, ?, ?, ?, ?)";
        ejecutarUpdate(sql, p.getCodigo(), p.getNombre(), p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(), 1, p.getProveedorNombre());
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, precio_compra = ?, precio_venta = ?, stock = ?, proveedor_nombre = ? WHERE codigo = ?";
        ejecutarUpdate(sql, p.getNombre(), p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(), p.getProveedorNombre(), p.getCodigo());
    }

    public void eliminar(String codigo) throws SQLException {
        String sql = "UPDATE productos SET activo = 0 WHERE codigo = ?";
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
        String sql = "SELECT * FROM productos WHERE activo = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio_compra"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("stock"),
                        rs.getString("proveedor_nombre")
                );
                lista.add(p);
            }
        }
        return lista;
    }
    public int obtenerUltimoCodigoNumeric() throws SQLException {
        String sql = "SELECT codigo FROM productos WHERE codigo LIKE 'ART-%' ORDER BY CAST(SUBSTR(codigo, 5) AS INTEGER) DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String codigo = rs.getString("codigo");
                try {
                    return Integer.parseInt(codigo.substring(4));
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
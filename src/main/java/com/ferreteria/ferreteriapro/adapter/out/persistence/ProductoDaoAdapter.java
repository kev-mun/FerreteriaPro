package com.ferreteria.ferreteriapro.adapter.out.persistence;

import com.ferreteria.ferreteriapro.domain.port.out.ProductoRepositoryPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Adaptador de persistencia que implementa ProductoRepositoryPort
 * para operaciones de stock sobre la tabla productos existente.
 *
 * No modifica la estructura de ProductoDAO: solo expone lectura/escritura
 * de stock a través del puerto definido en el dominio.
 */
public class ProductoDaoAdapter implements ProductoRepositoryPort {

    @Override
    public int obtenerStock(Connection conn, String codigo) throws SQLException {
        String sql = "SELECT stock FROM productos WHERE codigo = ? AND activo = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock");
                } else {
                    throw new SQLException("Producto no encontrado con código: " + codigo);
                }
            }
        }
    }

    @Override
    public void actualizarStock(Connection conn, String codigo, int nuevoStock) throws SQLException {
        String sql = "UPDATE productos SET stock = ? WHERE codigo = ? AND activo = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevoStock);
            pstmt.setString(2, codigo);
            int filas = pstmt.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo actualizar el stock. Producto no encontrado: " + codigo);
            }
        }
    }
}

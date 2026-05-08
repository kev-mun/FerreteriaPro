package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (documento, nombre, telefono, saldo_pendiente) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, c.getDocumento());
            pstmt.setString(2, c.getNombre());
            pstmt.setString(3, c.getTelefono());
            pstmt.setDouble(4, c.getSaldoPendiente());
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET documento = ?, nombre = ?, telefono = ?, saldo_pendiente = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getDocumento());
            pstmt.setString(2, c.getNombre());
            pstmt.setString(3, c.getTelefono());
            pstmt.setDouble(4, c.getSaldoPendiente());
            pstmt.setInt(5, c.getId());
            pstmt.executeUpdate();
        }
    }

    public void actualizarSaldo(int clienteId, double cambio) throws SQLException {
        String sql = "UPDATE clientes SET saldo_pendiente = saldo_pendiente + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, cambio);
            pstmt.setInt(2, clienteId);
            pstmt.executeUpdate();
        }
    }

    public List<Cliente> listarTodo() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getInt("id"),
                        rs.getString("documento"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getDouble("saldo_pendiente")
                ));
            }
        }
        return lista;
    }
    
    public List<Cliente> buscar(String termino) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE nombre LIKE ? OR documento LIKE ? ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + termino + "%");
            pstmt.setString(2, "%" + termino + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Cliente(
                            rs.getInt("id"),
                            rs.getString("documento"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getDouble("saldo_pendiente")
                    ));
                }
            }
        }
        return lista;
    }
}

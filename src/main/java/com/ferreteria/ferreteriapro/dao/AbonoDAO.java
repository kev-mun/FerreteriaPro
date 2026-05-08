package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Abono;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonoDAO {

    public void insertar(Abono a) throws SQLException {
        String sql = "INSERT INTO abonos_credito (cliente_id, monto, fecha, metodo_pago, usuario_nombre) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, a.getClienteId());
            pstmt.setDouble(2, a.getMonto());
            pstmt.setString(3, a.getFecha());
            pstmt.setString(4, a.getMetodoPago());
            pstmt.setString(5, a.getUsuarioNombre());
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Abono> listarPorCliente(int clienteId) throws SQLException {
        List<Abono> lista = new ArrayList<>();
        String sql = "SELECT * FROM abonos_credito WHERE cliente_id = ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Abono(
                            rs.getInt("id"),
                            rs.getInt("cliente_id"),
                            rs.getDouble("monto"),
                            rs.getString("fecha"),
                            rs.getString("metodo_pago"),
                            rs.getString("usuario_nombre")
                    ));
                }
            }
        }
        return lista;
    }
    
    public List<Abono> listarPorFecha(String fechaInicio, String fechaFin) throws SQLException {
        List<Abono> lista = new ArrayList<>();
        String sql = "SELECT * FROM abonos_credito WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaInicio + " 00:00:00");
            pstmt.setString(2, fechaFin + " 23:59:59");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Abono(
                            rs.getInt("id"),
                            rs.getInt("cliente_id"),
                            rs.getDouble("monto"),
                            rs.getString("fecha"),
                            rs.getString("metodo_pago"),
                            rs.getString("usuario_nombre")
                    ));
                }
            }
        }
        return lista;
    }
}

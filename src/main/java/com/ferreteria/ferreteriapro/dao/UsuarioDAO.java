package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario login(String username, String password) throws Exception {
        String hash = hashPassword(password);
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            rs.getString("password_hash"),
                            rs.getString("nombre"),
                            rs.getString("rol")
                    );
                }
            }
        }
        return null;
    }

    public List<Usuario> listarTodo() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("usuario"),
                        rs.getString("password_hash"),
                        rs.getString("nombre"),
                        rs.getString("rol")
                ));
            }
        }
        return lista;
    }

    public void guardar(Usuario u, String passwordPlana) throws SQLException {
        String hash = hashPassword(passwordPlana);
        String sql = "INSERT INTO usuarios (usuario, password_hash, nombre, rol) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsuario());
            pstmt.setString(2, hash);
            pstmt.setString(3, u.getNombre());
            pstmt.setString(4, u.getRol());
            pstmt.executeUpdate();
        }
    }

    public void actualizar(Usuario u, String nuevaPasswordPlana) throws SQLException {
        String sql;
        boolean cambiarPass = nuevaPasswordPlana != null && !nuevaPasswordPlana.trim().isEmpty();
        
        if (cambiarPass) {
            sql = "UPDATE usuarios SET usuario = ?, password_hash = ?, nombre = ?, rol = ? WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET usuario = ?, nombre = ?, rol = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsuario());
            if (cambiarPass) {
                pstmt.setString(2, hashPassword(nuevaPasswordPlana));
                pstmt.setString(3, u.getNombre());
                pstmt.setString(4, u.getRol());
                pstmt.setInt(5, u.getId());
            } else {
                pstmt.setString(2, u.getNombre());
                pstmt.setString(3, u.getRol());
                pstmt.setInt(4, u.getId());
            }
            pstmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

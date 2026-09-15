package com.ferreteria.ferreteriapro.dao;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KardexDAO {

    public static class MovimientoKardex {
        private int id;
        private String fecha;
        private String productoCodigo;
        private String tipo; // "ENTRADA" o "SALIDA"
        private int cantidad;
        private double valor;
        private String detalle;

        public MovimientoKardex(int id, String fecha, String productoCodigo, String tipo, int cantidad, double valor, String detalle) {
            this.id = id;
            this.fecha = fecha;
            this.productoCodigo = productoCodigo;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.valor = valor;
            this.detalle = detalle;
        }

        public int getId() { return id; }
        public String getFecha() { return fecha; }
        public String getProductoCodigo() { return productoCodigo; }
        public String getTipo() { return tipo; }
        public int getCantidad() { return cantidad; }
        public double getValor() { return valor; }
        public String getDetalle() { return detalle; }
    }

    public List<MovimientoKardex> obtenerMovimientos(String productoCodigo) throws SQLException {
        List<MovimientoKardex> movimientos = new ArrayList<>();
        
        // 1. Consultar entradas activas e históricas
        String sqlEntradas = "SELECT id, fecha, producto_codigo, cantidad, costo_unitario, proveedor FROM entradas_inventario WHERE producto_codigo = ? " +
                             "UNION ALL " +
                             "SELECT id, fecha, producto_codigo, cantidad, costo_unitario, proveedor FROM historico_compras WHERE producto_codigo = ? " +
                             "ORDER BY fecha DESC";
                             
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlEntradas)) {
            pstmt.setString(1, productoCodigo);
            pstmt.setString(2, productoCodigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new MovimientoKardex(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getString("producto_codigo"),
                        "ENTRADA",
                        rs.getInt("cantidad"),
                        rs.getDouble("costo_unitario"),
                        "Proveedor: " + rs.getString("proveedor")
                    ));
                }
            }
        }

        // 2. Consultar ventas activas e históricas
        String sqlVentas = "SELECT id, fecha, producto_codigo, cantidad, total, metodo_pago FROM ventas WHERE producto_codigo = ? " +
                           "UNION ALL " +
                           "SELECT id, fecha, producto_codigo, cantidad, total, metodo_pago FROM historico_ventas WHERE producto_codigo = ? " +
                           "ORDER BY fecha DESC";
                           
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlVentas)) {
            pstmt.setString(1, productoCodigo);
            pstmt.setString(2, productoCodigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new MovimientoKardex(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getString("producto_codigo"),
                        "SALIDA",
                        rs.getInt("cantidad"),
                        rs.getDouble("total") / rs.getInt("cantidad"),
                        "Venta - Pago: " + rs.getString("metodo_pago")
                    ));
                }
            }
        }

        // Ordenar movimientos por fecha desc
        movimientos.sort((m1, m2) -> m2.getFecha().compareTo(m1.getFecha()));
        return movimientos;
    }
}

package com.ferreteria.ferreteriapro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:ferreteria_nueva.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000;");
            stmt.execute("PRAGMA foreign_keys = ON;");
            return conn;
        } catch (SQLException e) {
            if (conn != null)
                conn.close();
            throw e;
        }
    }

    public static void inicializarBaseDeDatos() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Crear tabla de productos
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS productos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        codigo TEXT UNIQUE NOT NULL,
                        nombre TEXT NOT NULL,
                        precio_compra REAL DEFAULT 0,
                        precio_venta REAL NOT NULL,
                        stock INTEGER NOT NULL,
                        activo INTEGER DEFAULT 1,
                        proveedor_nombre TEXT
                    );
                    """);

            // Crear tabla de ventas
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS ventas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fecha TEXT NOT NULL,
                        producto_codigo TEXT NOT NULL,
                        producto_nombre TEXT,
                        cantidad INTEGER NOT NULL,
                        total REAL NOT NULL,
                        metodo_pago TEXT NOT NULL,
                        usuario_nombre TEXT,
                        FOREIGN KEY (producto_codigo) REFERENCES productos(codigo)
                    );
                    """);

            // Crear tabla de entradas_inventario
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS entradas_inventario (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        producto_codigo TEXT NOT NULL,
                        cantidad INTEGER NOT NULL,
                        costo_unitario REAL NOT NULL,
                        fecha TEXT NOT NULL,
                        proveedor TEXT,
                        usuario_nombre TEXT,
                        FOREIGN KEY (producto_codigo) REFERENCES productos(codigo)
                    );
                    """);

            // Crear tabla de historico_compras
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS historico_compras (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        producto_codigo TEXT NOT NULL,
                        cantidad INTEGER NOT NULL,
                        costo_unitario REAL NOT NULL,
                        fecha TEXT NOT NULL,
                        proveedor TEXT,
                        usuario_nombre TEXT,
                        fecha_archivo TEXT DEFAULT CURRENT_TIMESTAMP
                    );
                    """);

            // Crear tabla de histórico de ventas
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS historico_ventas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fecha TEXT,
                        producto_codigo TEXT,
                        producto_nombre TEXT,
                        cantidad INTEGER,
                        total REAL,
                        metodo_pago TEXT,
                        costo_unitario REAL,
                        usuario_nombre TEXT
                    );
                    """);

            // Crear tabla de proveedores
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS proveedores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre TEXT UNIQUE NOT NULL
                    );
                    """);
            stmt.execute(
                    "INSERT OR IGNORE INTO proveedores (nombre) VALUES ('Genérico'), ('Aceros S.A.'), ('FerreExpress'), ('Herramientas Pro');");

            // Crear tabla de usuarios
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        usuario TEXT UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        rol TEXT NOT NULL
                    );
                    """);

            // Sembrar usuarios iniciales con REPLACE para asegurar que la contraseña sea la
            // correcta
            // admin / admin123 (Hash detectado en sistema:
            // 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
            String adminHash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
            stmt.executeUpdate("REPLACE INTO usuarios (id, usuario, password_hash, nombre, rol) " +
                    "VALUES (1, 'admin', '" + adminHash + "', 'Administrador', 'Administrador');");

            // vendedor / vendedor123 (Hash detectado:
            // 56976bf24998ca63e35fe4f1e2469b5751d1856003e8d16fef0aafef496ed044)
            String vendedorHash = "56976bf24998ca63e35fe4f1e2469b5751d1856003e8d16fef0aafef496ed044";
            stmt.executeUpdate("REPLACE INTO usuarios (id, usuario, password_hash, nombre, rol) " +
                    "VALUES (2, 'vendedor', '" + vendedorHash + "', 'Vendedor Test', 'Vendedor');");

            // Migraciones adicionales
            try {
                stmt.execute("ALTER TABLE productos ADD COLUMN precio_compra REAL DEFAULT 0;");
            } catch (SQLException e) {
            }

            try {
                stmt.execute("ALTER TABLE productos ADD COLUMN activo INTEGER DEFAULT 1;");
            } catch (SQLException e) {
            }

            try {
                stmt.execute("ALTER TABLE ventas ADD COLUMN producto_nombre TEXT;");
            } catch (SQLException e) {
            }

            try {
                stmt.execute("ALTER TABLE productos ADD COLUMN proveedor_nombre TEXT;");
            } catch (SQLException e) {
            }

            try {
                stmt.execute("ALTER TABLE entradas_inventario ADD COLUMN proveedor TEXT;");
            } catch (SQLException e) {
            }

            // Columnas de Auditoría
            try {
                stmt.execute("ALTER TABLE ventas ADD COLUMN usuario_nombre TEXT;");
            } catch (SQLException e) {
            }
            try {
                stmt.execute("ALTER TABLE entradas_inventario ADD COLUMN usuario_nombre TEXT;");
            } catch (SQLException e) {
            }
            try {
                stmt.execute("ALTER TABLE historico_compras ADD COLUMN usuario_nombre TEXT;");
            } catch (SQLException e) {
            }
            try {
                stmt.execute("ALTER TABLE historico_ventas ADD COLUMN usuario_nombre TEXT;");
            } catch (SQLException e) {
            }

            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA synchronous = NORMAL;");

            System.out.println("✅ Base de datos configurada correctamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error al configurar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

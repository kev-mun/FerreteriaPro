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
            // Mantenemos el timeout que configuramos antes
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
        return conn;
    }

    public static void inicializarBaseDeDatos() {
        String sqlTabla = """
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL,
                precio_venta REAL NOT NULL,
                stock INTEGER NOT NULL
            );
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Crear la tabla
            stmt.execute(sqlTabla);

            // 2. Activar el modo WAL
            // Este comando devuelve una fila con el modo actual, pero solo nos interesa ejecutarlo
            stmt.execute("PRAGMA journal_mode = WAL;");

            // 3. Opcional: Sincronización normal (más rápido que el modo FULL)
            stmt.execute("PRAGMA synchronous = NORMAL;");

            System.out.println("✅ Base de datos en modo WAL y lista para multitaréa.");

        } catch (SQLException e) {
            System.err.println("❌ Error al configurar WAL: " + e.getMessage());
        }
    }
}
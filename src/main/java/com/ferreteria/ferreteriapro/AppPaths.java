package com.ferreteria.ferreteriapro;

import java.io.File;

/**
 * Gestor centralizado de rutas para almacenamiento de datos en Windows y otros SO.
 * Evita errores de permisos de escritura en la carpeta de instalación (Program Files).
 *
 * Estructura estándar:
 * - Base de Datos:         %APPDATA%/FerreteriaPro/database/
 * - Facturas/Comprobantes: %APPDATA%/FerreteriaPro/facturas/
 * - Respaldos:             %APPDATA%/FerreteriaPro/backups/
 * - Reportes:              %APPDATA%/FerreteriaPro/reportes/
 */
public class AppPaths {

    private static final String APP_NAME = "FerreteriaPro";

    public static File getAppDir() {
        String baseDir = System.getenv("APPDATA");
        if (baseDir == null || baseDir.isBlank()) {
            baseDir = System.getProperty("user.home");
        }
        File appDir = new File(baseDir, APP_NAME);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return appDir;
    }

    public static File getDatabaseDir() {
        File dir = new File(getAppDir(), "database");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getDatabaseFile() {
        return new File(getDatabaseDir(), "ferreteria_nueva.db");
    }

    public static File getFacturasDir() {
        File dir = new File(getAppDir(), "facturas");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getFacturasSubdir(String subcarpeta) {
        File dir = new File(getFacturasDir(), subcarpeta);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getBackupsDir() {
        File dir = new File(getAppDir(), "backups");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getBackupsSubdir(String subcarpeta) {
        File dir = new File(getBackupsDir(), subcarpeta);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getReportesDir(String subcarpeta) {
        File dir = (subcarpeta != null && !subcarpeta.isBlank())
                ? new File(new File(getAppDir(), "reportes"), subcarpeta)
                : new File(getAppDir(), "reportes");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}

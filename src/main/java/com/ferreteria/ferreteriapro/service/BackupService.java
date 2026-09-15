package com.ferreteria.ferreteriapro.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    // Archivo de la base de datos a respaldar (nombre relativo al directorio de ejecución)
    private static final String DB_FILENAME = "ferreteria_nueva.db";

    // Carpetas de respaldo (nombres de directorio)
    private static final String NOMBRE_CARPETA_DRIVE = "backups_drive";
    private static final String NOMBRE_CARPETA_PENDIENTES = "backups_pendientes_usb";

    // Ruta de la USB destino (configurable)
    private static final String CARPETA_USB = "E:/BackupsFerreteria";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Ejecuta el respaldo híbrido de forma asíncrona en un hilo secundario
     * para no bloquear la interfaz gráfica de JavaFX.
     */
    public void realizarRespaldoHibridoAsync() {
        Thread hiloBackup = new Thread(() -> {
            // Raíz dinámica del proyecto donde se ejecuta la aplicación
            String projectRoot = System.getProperty("user.dir");

            // Creación automática de directorios usando la raíz dinámica
            File carpetaDrive = new File(projectRoot, NOMBRE_CARPETA_DRIVE);
            if (!carpetaDrive.exists()) {
                carpetaDrive.mkdirs();
            }

            File carpetaPendientes = new File(projectRoot, NOMBRE_CARPETA_PENDIENTES);
            if (!carpetaPendientes.exists()) {
                carpetaPendientes.mkdirs();
            }

            String fechaHora = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String nombreArchivo = "ferreteria_backup_" + fechaHora + ".db";

            File archivoOrigen = new File(projectRoot, DB_FILENAME);
            if (!archivoOrigen.exists()) {
                // Fallback por si la DB está en la raíz directa de trabajo
                archivoOrigen = new File(DB_FILENAME);
            }

            if (!archivoOrigen.exists()) {
                final String rutaBuscada = archivoOrigen.getAbsolutePath();
                Platform.runLater(() -> mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Error de Respaldo",
                        "No se encontró el archivo de base de datos: " + rutaBuscada));
                return;
            }

            boolean exitoDrive = false;
            boolean exitoUsb = false;
            boolean usbConectada = true;

            // ── 1. RESPALDO EN CARPETA DE GOOGLE DRIVE ──────────────────────
            try {
                Path destino = new File(carpetaDrive, nombreArchivo).toPath();
                Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                exitoDrive = true;
                System.out.println("✅ Respaldo en Drive completado: " + destino.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("❌ Error al respaldar en Drive: " + e.getMessage());
            }

            // ── 2. RESPALDO EN USB O CARPETA DE CONTINGENCIA ────────────────
            File unidadUsb = new File(CARPETA_USB);

            if (unidadUsb.exists() || unidadUsb.getParentFile() != null && unidadUsb.getParentFile().exists()) {
                // La unidad USB está conectada
                try {
                    if (!unidadUsb.exists()) {
                        unidadUsb.mkdirs();
                    }
                    Path destinoUsb = new File(unidadUsb, nombreArchivo).toPath();
                    Files.copy(archivoOrigen.toPath(), destinoUsb, StandardCopyOption.REPLACE_EXISTING);
                    exitoUsb = true;
                    System.out.println("✅ Respaldo en USB completado: " + destinoUsb.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("❌ Error al respaldar en USB: " + e.getMessage());
                }
            } else {
                // La USB NO está conectada → copia de contingencia local
                usbConectada = false;
                try {
                    Path destinoPendiente = new File(carpetaPendientes, nombreArchivo).toPath();
                    Files.copy(archivoOrigen.toPath(), destinoPendiente, StandardCopyOption.REPLACE_EXISTING);
                    exitoUsb = true; // Se guardó en contingencia
                    System.out.println("⚠️ USB no conectada. Respaldo guardado en pendientes: " + destinoPendiente.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("❌ Error al respaldar en carpeta de pendientes: " + e.getMessage());
                }
            }

            // ── 3. NOTIFICACIÓN AL USUARIO ──────────────────────────────────
            final boolean driveOk = exitoDrive;
            final boolean usbOk = exitoUsb;
            final boolean usbPresente = usbConectada;

            Platform.runLater(() -> {
                if (driveOk && usbOk && usbPresente) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Respaldo Completado",
                            "✅ Se realizó el respaldo correctamente en Google Drive y en la USB.");
                } else if (driveOk && usbOk && !usbPresente) {
                    mostrarAlerta(Alert.AlertType.WARNING,
                            "Respaldo Parcial",
                            "Respaldo en Drive completado.\n\n" +
                                    "⚠️ La USB no estaba conectada, por lo que se guardó una copia temporal " +
                                    "en la carpeta de pendientes del sistema ('" + NOMBRE_CARPETA_PENDIENTES + "/').\n\n" +
                                    "Recuerde conectar la USB y copiar manualmente los respaldos pendientes.");
                } else if (driveOk && !usbOk) {
                    mostrarAlerta(Alert.AlertType.WARNING,
                            "Respaldo Parcial",
                            "✅ Respaldo en Drive completado.\n\n" +
                                    "❌ No se pudo completar el respaldo en la USB ni en la carpeta de pendientes.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR,
                            "Error de Respaldo",
                            "❌ No se pudo completar ningún respaldo. Verifique los permisos y el espacio disponible.");
                }
            });
        });

        hiloBackup.setDaemon(true);
        hiloBackup.setName("Hilo-Backup-Hibrido");
        hiloBackup.start();
    }

    /**
     * Verifica si la USB está conectada actualmente.
     */
    public boolean isUsbConectada() {
        File unidad = new File(CARPETA_USB);
        return unidad.exists() || (unidad.getParentFile() != null && unidad.getParentFile().exists());
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}

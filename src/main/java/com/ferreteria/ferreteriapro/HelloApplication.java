package com.ferreteria.ferreteriapro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // 1. Inicializamos la base de datos primero
            // Si esto falla, el programa no debería intentar abrir la ventana
            DatabaseConnection.inicializarBaseDeDatos();

            // 2. Cargamos el archivo FXML
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

            // 3. Configuramos la escena con un tamaño adecuado
            Scene scene = new Scene(fxmlLoader.load(), 900, 700);

            // 4. Configuración visual de la ventana
            stage.setTitle("Sistema de Inventario - Ferretería Pro");
            stage.setScene(scene);
            stage.setMinWidth(800); // Evitamos que la ventana se encoja demasiado
            stage.setMinHeight(600);

            stage.show();

            System.out.println("🚀 Aplicación iniciada con éxito.");

        } catch (IOException e) {
            System.err.println("❌ Error crítico: No se pudo cargar el archivo hello-view.fxml.");
            System.err.println("Verifica que el archivo esté en src/main/resources/com/ferreteria/ferreteriapro/");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Ocurrió un error inesperado al iniciar:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
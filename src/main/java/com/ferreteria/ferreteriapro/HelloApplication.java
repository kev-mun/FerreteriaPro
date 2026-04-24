package com.ferreteria.ferreteriapro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // 1. Inicializamos la base de datos primero
            // Si esto falla, el programa no debería intentar abrir la ventana
            DatabaseConnection.inicializarBaseDeDatos();

            // 2. Cargamos el archivo FXML del LOGIN primero
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));

            // 3. Configuramos la escena con un tamaño adecuado para login
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);

            // 4. Configuración visual de la ventana
            stage.setTitle("Sistema de Inventario - Ferretería pro");
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
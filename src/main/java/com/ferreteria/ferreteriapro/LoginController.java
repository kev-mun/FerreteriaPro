package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.dao.UsuarioDAO;
import com.ferreteria.ferreteriapro.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMensaje;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    protected void onLoginClick() {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Por favor complete todos los campos.");
            return;
        }

        try {
            Usuario u = usuarioDAO.login(user, pass);
            if (u != null) {
                Session.setCurrentUser(u);
                abrirVentanaPrincipal();
            } else {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            lblMensaje.setText("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 750);

            stage.setTitle("Ferretería Pro - " + Session.getCurrentUser().getNombre());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            lblMensaje.setText("Error al cargar la interfaz principal.");
            e.printStackTrace();
        }
    }
}

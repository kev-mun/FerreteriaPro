package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class HelloController {
    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtStock;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo, colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private final InventarioService service = new InventarioService();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Vinculación básica de columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // --- MEJORA 1: Formato de Moneda para la Tabla ---
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    // Formato Colombiano: Signo $, puntos para miles y 0 decimales
                    setText(String.format("$ %,.0f", precio));
                }
            }
        });

        // Configuración estética del campo Código (Automático)
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #666666;");

        cargarDatos();
        nuevoRegistro();

        // Listener para seleccionar productos de la tabla
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtCodigo.setText(newSel.getCodigo());
                txtNombre.setText(newSel.getNombre());
                // Al editar, mostramos el número limpio para evitar errores de reconversión
                txtPrecio.setText(String.format("%.0f", newSel.getPrecioVenta()));
                txtStock.setText(String.valueOf(newSel.getStock()));
            }
        });
    }

    private void cargarDatos() {
        try {
            listaProductos.setAll(service.obtenerProductos());
            tablaProductos.setItems(listaProductos);
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo conectar a la base de datos.", Alert.AlertType.ERROR);
        }
    }

    private void nuevoRegistro() {
        try {
            txtCodigo.setText(service.generarSiguienteCodigo());
        } catch (Exception e) {
            txtCodigo.setText("ERR-000");
        }
    }

    @FXML
    protected void onGuardarClick() {
        try {
            // --- MEJORA 2: Limpieza de Formato al Guardar ---
            // Eliminamos puntos y comas para que Double.parseDouble funcione correctamente
            String precioLimpio = txtPrecio.getText().replace(".", "").replace(",", "").trim();
            String nombre = txtNombre.getText().trim();
            String stockTexto = txtStock.getText().trim();

            if (nombre.isEmpty() || precioLimpio.isEmpty() || stockTexto.isEmpty()) {
                throw new Exception("Todos los campos son obligatorios.");
            }

            // Conversión segura
            double precio = Double.parseDouble(precioLimpio);
            int stock = Integer.parseInt(stockTexto);

            Producto p = new Producto(txtCodigo.getText(), nombre, precio, stock);

            // Verificar si editamos o creamos
            boolean existe = listaProductos.stream()
                    .anyMatch(prod -> prod.getCodigo().equals(p.getCodigo()));

            if (existe) {
                service.editarProducto(p);
                mostrarAlerta("Éxito", "Producto actualizado.", Alert.AlertType.INFORMATION);
            } else {
                service.registrarProducto(p);
                mostrarAlerta("Éxito", "Producto registrado: " + p.getCodigo(), Alert.AlertType.INFORMATION);
            }

            cargarDatos();
            limpiarCampos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Precio y Stock deben ser números.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onEliminarClick() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Seleccione un producto para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            service.eliminarProducto(seleccionado.getCodigo());
            cargarDatos();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo eliminar de la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void limpiarCampos() {
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        tablaProductos.getSelectionModel().clearSelection();
        nuevoRegistro();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
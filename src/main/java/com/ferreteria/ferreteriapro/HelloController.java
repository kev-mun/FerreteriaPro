package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.CierreCaja;
import com.ferreteria.ferreteriapro.model.EntradaInventario;
import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.model.Proveedor;
import com.ferreteria.ferreteriapro.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.model.Usuario;
import com.ferreteria.ferreteriapro.dao.UsuarioDAO;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.controlsfx.control.SearchableComboBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;
import javafx.application.Platform;

public class HelloController {
    private boolean mostrandoAlertaProveedor = false;

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab tabGestion, tabHistorialVentas, tabHistorialCompras, tabUsuarios, tabProveedores;
    @FXML
    private Label lblSesionInfo;
    @FXML
    private SearchableComboBox<Producto> comboBusquedaGestion;
    @FXML
    private TextField txtBuscador;
    @FXML
    private TextField txtCodigo, txtNombre, txtPrecio, txtStock;
    @FXML
    private TextField txtPrecioCompra, txtPorcentajeGanancia, txtPorcentajeIva;
    @FXML
    private SearchableComboBox<Proveedor> comboProveedorGestion;
    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, String> colCodigo, colNombre;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private TableColumn<Producto, String> colProveedor;

    @FXML
    private TableView<Venta> tablaVentas;
    @FXML
    private TableColumn<Venta, Integer> colVentaId, colVentaCant;
    @FXML
    private TableColumn<Venta, String> colVentaFecha, colVentaProducto, colVentaMetodo;
    @FXML
    private TableColumn<Venta, Double> colVentaTotal;
    @FXML
    private TableColumn<Venta, String> colVentaUsuario;

    // --- ELEMENTOS DE ENTRADAS ---
    @FXML
    private TableView<EntradaInventario> tablaEntradas;
    @FXML
    private TableColumn<EntradaInventario, Integer> colEntradaId, colEntradaCant;
    @FXML
    private TableColumn<EntradaInventario, String> colEntradaFecha, colEntradaProducto, colEntradaProveedor;
    @FXML
    private TableColumn<EntradaInventario, Double> colEntradaCosto;
    @FXML
    private TableColumn<EntradaInventario, String> colEntradaUsuario;

    // --- ELEMENTOS DE PROVEEDORES ---
    @FXML
    private TableView<Proveedor> tablaProveedores;
    @FXML
    private TableColumn<Proveedor, Integer> colProvId;
    @FXML
    private TableColumn<Proveedor, String> colProvNombre;
    @FXML
    private TextField txtNombreProveedor;

    // --- ELEMENTOS DE USUARIOS ---
    @FXML
    private TableView<Usuario> tablaUsuarios;
    @FXML
    private TableColumn<Usuario, Integer> colUsuarioId;
    @FXML
    private TableColumn<Usuario, String> colUsuarioFull, colUsuarioNick, colUsuarioRol;
    @FXML
    private TextField txtUsuarioNombre, txtUsuarioLogin;
    @FXML
    private PasswordField txtUsuarioPass;
    @FXML
    private ComboBox<String> comboUsuarioRol;

    private final InventarioService service = new InventarioService();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();
    private final ObservableList<EntradaInventario> listaEntradas = FXCollections.observableArrayList();
    private final ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private final ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private FilteredList<Producto> filtroProductos;

    // --- ELEMENTOS STOCK BAJO ---
    private Tab tabStockBajo;
    private TableView<Producto> tablaStockBajo;
    private final ObservableList<Producto> listaStockBajo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            aplicarSeguridad();
            crearTabStockBajo();
            configurarColumnas();
            configurarColumnasVentas();
            configurarColumnasEntradas();
            cargarDatos();
            configurarBuscador();
            configurarCalculosAutomaticos();
            configurarComboGestion();
            configurarColumnasProveedores();
            actualizarListaProveedores();
            configurarComboProveedorGestion();
            nuevoRegistro();
        } catch (Exception e) {
            System.err.println("❌ Error en initialize de HelloController: " + e.getMessage());
            e.printStackTrace();
            // Mostrar alerta visual del error
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error Crítico");
                a.setHeaderText("No se pudo iniciar la interfaz");
                a.setContentText("Detalle: " + e.toString());
                a.showAndWait();
            });
        }
    }

    private void aplicarSeguridad() {
        if (Session.getCurrentUser() != null) {
            String nombre = Session.getCurrentUser().getNombre();
            String rol = Session.getCurrentUser().getRol();
            if (lblSesionInfo != null) {
                lblSesionInfo.setText("👤 Usuario: " + nombre + " | Rol: " + rol);
            }

            if ("Vendedor".equalsIgnoreCase(rol)) {
                // Restringir acceso a pestañas administrativas
                mainTabPane.getTabs().remove(tabGestion);
                mainTabPane.getTabs().remove(tabHistorialVentas);
                mainTabPane.getTabs().remove(tabHistorialCompras);
                mainTabPane.getTabs().remove(tabUsuarios);
                mainTabPane.getTabs().remove(tabProveedores);
            }
        }
    }

    @FXML
    protected void onLogoutClick() {
        Session.logout();
        try {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            stage.setTitle("Login - Ferretería Pro");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onNuevaVentaFlotanteClick() {
        new VentaFlotanteController(
                service,
                _v -> javafx.application.Platform.runLater(this::cargarDatos),
                null // Sin producto pre-seleccionado; el usuario busca dentro
        );
    }

    // --- SECCIÓN DE VENTA Y CAJA ---

    @FXML
    protected void onSalidaClick() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        // Abrir ventana flotante no bloqueante. Si hay un producto seleccionado,
        // se añade automáticamente al carrito.
        VentaFlotanteController ventana = new VentaFlotanteController(
                service,
                _v -> javafx.application.Platform.runLater(this::cargarDatos),
                seleccionado // puede ser null; el usuario busca dentro de la ventana
        );
        limpiarCampos();
    }

    private void generarFacturaDocumento(Producto p, int cant, double total, String metodo, double pago,
            double cambio) {
        String fecha = java.time.LocalDate.now().toString();
        String idFactura = "FAC-" + System.currentTimeMillis();

        // Crear carpeta si no existe
        File carpeta = new File("facturas");
        if (!carpeta.exists())
            carpeta.mkdir();

        File archivo = new File(carpeta, idFactura + ".txt");

        try (PrintWriter out = new PrintWriter(new FileWriter(archivo))) {
            out.println("==========================================");
            out.println("           FERRETERÍA                     ");
            out.println("        FACTURA DE VENTA                  ");
            out.println("==========================================");
            out.println("Fecha: " + fecha);
            out.println("Factura No: " + idFactura);
            out.println("------------------------------------------");
            out.println(String.format("%-20s %-5s %-10s", "Producto", "Cantidad", "Total"));
            out.println(String.format("%-20s %-5d $%,.0f", p.getNombre(), cant, total));
            out.println("------------------------------------------");
            out.println("Método de Pago: " + metodo);
            out.println("Pagó con:       $ " + String.format("%,.0f", pago));
            out.println("Cambio:         $ " + String.format("%,.0f", cambio));
            out.println("==========================================");
            out.println("      ¡Gracias por su compra!             ");
            out.println("==========================================");

            System.out.println("Factura generada: " + archivo.getAbsolutePath());
            mostrarAlerta("Factura", "Documento guardado en: " + archivo.getName(), Alert.AlertType.INFORMATION);

            // Abrir el archivo automáticamente (Opcional)
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("notepad.exe", archivo.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar la factura: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // --- GESTIÓN DE INVENTARIO ---

    @FXML
    protected void onGuardarClick() {
        try {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();

            if (nombre.isEmpty())
                throw new Exception("El nombre es obligatorio.");

            double costo = parseMoney(txtPrecioCompra.getText().trim());
            double precioVenta = parseMoney(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            if (precioVenta < costo) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Advertencia de Precio");
                alert.setHeaderText("Margen Negativo");
                alert.setContentText("El precio de venta es menor al costo. ¿Desea continuar?");
                Optional<ButtonType> res = alert.showAndWait();
                if (res.isEmpty() || res.get() == ButtonType.CANCEL)
                    return;
            }

            String provNombre = "Genérico";
            if (comboProveedorGestion.getValue() != null) {
                provNombre = comboProveedorGestion.getValue().getNombre();
            }

            Producto p = new Producto(codigo, nombre, costo, precioVenta, stock, provNombre);
            boolean existe = listaProductos.stream().anyMatch(prod -> prod.getCodigo().equals(p.getCodigo()));

            if (existe) {
                service.editarProducto(p);
            } else {
                service.registrarProducto(p);
            }

            finalizar("Guardado", "Operación exitosa.");
        } catch (Exception e) {
            mostrarAlerta("Validación", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configurarCalculosAutomaticos() {
        txtPrecioCompra.textProperty().addListener((obs, old, val) -> calcularPrecioVenta());
        txtPorcentajeGanancia.textProperty().addListener((obs, old, val) -> calcularPrecioVenta());
        txtPorcentajeIva.textProperty().addListener((obs, old, val) -> calcularPrecioVenta());
    }

    @FXML
    protected void onNuevoProductoClick() {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.setHeaderText("Configure el nuevo artículo");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField n_codigo = new TextField();
        try {
            n_codigo.setText(service.generarSiguienteCodigo());
        } catch (Exception e) {
            n_codigo.setText("HM-001");
        }
        n_codigo.setEditable(false);
        TextField n_nombre = new TextField();
        TextField n_costo = new TextField("0");
        TextField n_ganancia = new TextField("30");
        TextField n_iva = new TextField("19");
        TextField n_venta = new TextField("0");
        n_venta.setEditable(false);
        TextField n_stock = new TextField("0");

        SearchableComboBox<Proveedor> n_prov = new SearchableComboBox<>(listaProveedores);

        // Lógica de cálculo real-time en el diálogo
        Runnable calcular = () -> {
            try {
                double c = parseMoney(n_costo.getText());
                double g = parseMoney(n_ganancia.getText().isEmpty() ? "30" : n_ganancia.getText());
                double i = parseMoney(n_iva.getText().isEmpty() ? "0" : n_iva.getText());
                double v = c * (1 + g / 100) * (1 + i / 100);
                n_venta.setText(String.format("%.0f", v));
            } catch (Exception ignored) {
            }
        };
        n_costo.textProperty().addListener((o, ol, v) -> calcular.run());
        n_ganancia.textProperty().addListener((o, ol, v) -> calcular.run());
        n_iva.textProperty().addListener((o, ol, v) -> calcular.run());

        grid.add(new Label("Código:"), 0, 0);
        grid.add(n_codigo, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(n_nombre, 1, 1);
        grid.add(new Label("Costo:"), 0, 2);
        grid.add(n_costo, 1, 2);
        grid.add(new Label("Ganancia (%):"), 0, 3);
        grid.add(n_ganancia, 1, 3);
        grid.add(new Label("IVA (%):"), 0, 4);
        grid.add(n_iva, 1, 4);
        grid.add(new Label("Venta Final:"), 0, 5);
        grid.add(n_venta, 1, 5);
        grid.add(new Label("Stock Inicial:"), 0, 6);
        grid.add(n_stock, 1, 6);
        grid.add(new Label("Proveedor:"), 0, 7);
        grid.add(n_prov, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // --- VALIDACIÓN ESTRICTA ---
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                // 1. Campos Obligatorios
                if (n_nombre.getText().trim().isEmpty())
                    throw new Exception("El nombre es obligatorio.");
                if (n_costo.getText().trim().isEmpty())
                    throw new Exception("El costo es obligatorio.");
                if (n_stock.getText().trim().isEmpty())
                    throw new Exception("El stock inicial es obligatorio.");
                if (n_prov.getValue() == null)
                    throw new Exception("Debe seleccionar un proveedor.");

                // 2. Validación de Formatos (Números)
                try {
                    parseMoney(n_costo.getText());
                } catch (Exception e) {
                    throw new Exception("El campo 'Costo' solo permite números y puntos decimales.");
                }

                try {
                    Integer.parseInt(n_stock.getText().trim());
                } catch (Exception e) {
                    throw new Exception("El campo 'Stock' solo permite números enteros (sin letras).");
                }

                try {
                    parseMoney(n_ganancia.getText());
                    parseMoney(n_iva.getText());
                } catch (Exception e) {
                    throw new Exception("Los porcentajes de Ganancia/IVA deben ser numéricos.");
                }

            } catch (Exception e) {
                mostrarAlerta("Campo Inválido", e.getMessage(), Alert.AlertType.WARNING);
                event.consume(); // Evita que el diálogo se cierre
            }
        });

        dialog.setResultConverter(b -> {
            if (b == btnGuardar) {
                String prov = n_prov.getValue().getNombre();
                return new Producto(n_codigo.getText(), n_nombre.getText(),
                        parseMoney(n_costo.getText()), parseMoney(n_venta.getText()),
                        Integer.parseInt(n_stock.getText()), prov);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                service.registrarProducto(p);
                finalizar("Éxito", "Producto registrado correctamente.");
            } catch (Exception e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void configurarComboProveedorGestion() {
        try {
            comboProveedorGestion.setItems(listaProveedores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcularPrecioVenta() {
        try {
            double costo = parseMoney(txtPrecioCompra.getText());
            double gananciaPerc = parseMoney(
                    txtPorcentajeGanancia.getText().isEmpty() ? "30" : txtPorcentajeGanancia.getText());
            double ivaPerc = parseMoney(txtPorcentajeIva.getText().isEmpty() ? "0" : txtPorcentajeIva.getText());

            double subtotal = costo * (1 + gananciaPerc / 100);
            double total = subtotal * (1 + ivaPerc / 100);

            txtPrecio.setText(String.format("%.0f", total));
        } catch (Exception ignored) {
            // No hacemos nada si el formato aún no es válido mientras el usuario escribe
        }
    }

    @FXML
    protected void onEntradaClick() {
        // No obligamos a seleccionar en la tabla, pero si hay uno seleccionado, lo
        // pre-seleccionamos en el combo
        Producto preseleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        // Crear el diálogo personalizado
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Carga de Inventario");
        dialog.setHeaderText("Gestione la entrada de mercancía y precios");

        // Botones
        ButtonType btnRegistrar = new ButtonType("Registrar Entrada", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRegistrar, ButtonType.CANCEL);

        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        SearchableComboBox<Producto> comboProductos = new SearchableComboBox<>(listaProductos);
        comboProductos.setPromptText("Escriba para buscar un producto...");
        comboProductos.setPrefWidth(300);
        // Formateador para mostrar Nombre y Código en el Combo
        comboProductos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNombre() + " (" + item.getCodigo() + ")");
            }
        });
        comboProductos.setButtonCell(comboProductos.getCellFactory().call(null));

        comboProductos.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto p) {
                return p == null ? "" : p.getNombre() + " (" + p.getCodigo() + ")";
            }

            @Override
            public Producto fromString(String s) {
                return null;
            }
        });

        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Cantidad");
        TextField txtCosto = new TextField();
        txtCosto.setPromptText("Costo de Compra");
        TextField txtPrecioVenta = new TextField();
        txtPrecioVenta.setPromptText("Precio de Venta");
        txtPrecioVenta.setDisable(true);

        SearchableComboBox<Proveedor> comboProveedor = new SearchableComboBox<>(listaProveedores);
        comboProveedor.setPromptText("Seleccione proveedor...");
        comboProveedor.setPrefWidth(300);

        DatePicker dpFecha = new DatePicker(LocalDate.now());

        Label lblStockActual = new Label("Seleccione un producto...");

        // Al elegir producto, cargar su stock y proveedor
        comboProductos.setOnAction(e -> {
            Producto p = comboProductos.getValue();
            if (p != null) {
                lblStockActual.setText("Stock actual: " + p.getStock());

                // Auto-selección de proveedor
                comboProveedor.setValue(null);
                if (p.getProveedorNombre() != null && !p.getProveedorNombre().isEmpty()) {
                    comboProveedor.getItems().stream()
                            .filter(pr -> pr.getNombre().equalsIgnoreCase(p.getProveedorNombre()))
                            .findFirst()
                            .ifPresent(comboProveedor::setValue);
                } else {
                    mostrarAlerta("Atención",
                            "El producto '" + p.getNombre() + "' no tiene proveedor asignado. Por favor, selecciónelo.",
                            Alert.AlertType.WARNING);
                }
            }
        });

        // Pre-seleccionar si venía de la tabla
        if (preseleccionado != null)
            comboProductos.setValue(preseleccionado);

        grid.add(new Label("Producto:"), 0, 0);
        grid.add(comboProductos, 1, 0);
        grid.add(new Label("Info:"), 0, 1);
        grid.add(lblStockActual, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(txtCantidad, 1, 2);
        grid.add(new Label("Costo Unitario ($):"), 0, 3);
        grid.add(txtCosto, 1, 3);
        grid.add(new Label("Proveedor:"), 0, 4);
        grid.add(comboProveedor, 1, 4);
        grid.add(new Label("Fecha Llegada:"), 0, 5);
        grid.add(dpFecha, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == btnRegistrar) {
            try {
                Producto p = comboProductos.getValue();
                if (p == null)
                    throw new Exception("Debe seleccionar un producto.");

                String cantStr = txtCantidad.getText().trim();
                String costoStr = txtCosto.getText().trim();

                if (cantStr.isEmpty() || costoStr.isEmpty()) {
                    throw new Exception("La cantidad y el costo son obligatorios.");
                }

                int cantidad = Integer.parseInt(cantStr);
                double costo = parseMoney(costoStr);

                Proveedor provSel = comboProveedor.getValue();
                if (provSel == null)
                    throw new Exception("Debe seleccionar un proveedor.");
                String proveedor = provSel.getNombre();

                if (dpFecha.getValue() == null)
                    throw new Exception("La fecha de llegada es obligatoria.");
                String fechaLlegada = dpFecha.getValue().toString();

                if (cantidad <= 0 || costo <= 0) {
                    throw new Exception("Valores deben ser mayores a cero.");
                }

                // Registrar en el historial y actualizar producto (Solo Stock)
                String usuarioActual = Session.getCurrentUser() != null ? Session.getCurrentUser().getNombre()
                        : "Desconocido";
                EntradaInventario entrada = new EntradaInventario(p.getCodigo(), cantidad, costo, fechaLlegada,
                        proveedor, usuarioActual);

                service.registrarEntradaInventario(entrada, p.getPrecioVenta(), false);

                finalizar("Éxito", "Carga completada para " + p.getNombre());

            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Formato de número inválido. Use solo números.", Alert.AlertType.ERROR);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo procesar la entrada: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    protected void onEditarEntradaClick() {
        EntradaInventario seleccionada = tablaEntradas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Atención", "Seleccione un registro de la tabla para editar.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Compra");
        dialog.setHeaderText("Modificando registro: " + seleccionada.getProductoCodigo());

        ButtonType btnActualizar = new ButtonType("Actualizar Registro", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnActualizar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCantidad = new TextField(String.valueOf(seleccionada.getCantidad()));
        TextField txtCosto = new TextField(String.format("%.0f", seleccionada.getCostoUnitario()));

        SearchableComboBox<Proveedor> comboProveedor = new SearchableComboBox<>();
        try {
            comboProveedor.getItems().setAll(service.obtenerProveedores());
            comboProveedor.getItems().stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(seleccionada.getProveedor()))
                    .findFirst()
                    .ifPresent(comboProveedor::setValue);
        } catch (Exception e) {
        }

        DatePicker dpFecha = new DatePicker(LocalDate.parse(seleccionada.getFecha()));

        String displayName = (seleccionada.getProductoNombre() != null ? seleccionada.getProductoNombre()
                : "Desconocido")
                + " (" + seleccionada.getProductoCodigo() + ")";
        grid.add(new Label("Producto:"), 0, 0);
        grid.add(new Label(displayName), 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(txtCantidad, 1, 1);
        grid.add(new Label("Costo Unitario ($):"), 0, 2);
        grid.add(txtCosto, 1, 2);
        grid.add(new Label("Proveedor:"), 0, 3);
        grid.add(comboProveedor, 1, 3);
        grid.add(new Label("Fecha Llegada:"), 0, 4);
        grid.add(dpFecha, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnActualizar) {
            try {
                int nuevaCant = Integer.parseInt(txtCantidad.getText());
                double nuevoCosto = parseMoney(txtCosto.getText());
                String nuevoProv = comboProveedor.getValue() != null ? comboProveedor.getValue().getNombre()
                        : seleccionada.getProveedor();
                String nuevaFecha = dpFecha.getValue().toString();

                String usuarioActual = Session.getCurrentUser() != null ? Session.getCurrentUser().getNombre()
                        : "Desconocido";
                EntradaInventario editada = new EntradaInventario(seleccionada.getId(),
                        seleccionada.getProductoCodigo(), nuevaCant, nuevoCosto, nuevaFecha, nuevoProv, usuarioActual);

                service.editarEntradaInventario(editada, seleccionada.getCantidad());

                actualizarHistorialCompras();
                cargarDatos(); // Para actualizar stock en lista
                mostrarAlerta("Éxito", "Registro actualizado y stock sincronizado.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private double parseMoney(String input) {
        // Elimina símbolos de moneda y espacios
        String cleaned = input.replaceAll("[$\\s]", "");

        // Si el usuario usa coma como decimal (ej: 10,50) lo pasamos a punto
        // Pero primero removemos los puntos de miles (ej: 1.000,50 -> 1000,50)
        // Este es un enfoque común en Latinoamérica: . miles , decimales
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", ""); // Quita miles
            cleaned = cleaned.replace(",", "."); // Cambia decimal
        } else if (cleaned.contains(",")) {
            // Solo tiene coma, asumimos decimal
            cleaned = cleaned.replace(",", ".");
        }
        // Si solo tiene punto, asumimos que es decimal (estándar Java)

        return Double.parseDouble(cleaned);
    }

    @FXML
    protected void onCierreMensualClick() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Cierre Mensual");
        confirm.setHeaderText("¿Desea archivar los registros de este mes?");
        confirm.setContentText(
                "Los datos se guardarán en un reporte y en la tabla histórica, despejando la tabla principal.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String rutaReporte = service.procesarCierreMensual();
                mostrarAlerta("Cierre Exitoso",
                        "Los datos han sido archivados correctamente.\nReporte generado en: " + rutaReporte,
                        Alert.AlertType.INFORMATION);

                // Actualizar la tabla de entradas (ahora estará vacía)
                actualizarHistorialCompras();

            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo realizar el cierre: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    protected void onEliminarClick() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            mostrarAlerta("Atención", "No hay ningún producto seleccionado para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Está seguro de eliminar este producto?");
        confirm.setContentText("El producto se marcará como inactivo pero se conservará en el historial.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.eliminarProducto(codigo);
                finalizar("Eliminado", "El producto ha sido marcado como inactivo.");
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el producto: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // --- CONFIGURACIÓN UI ---

    private void cargarDatos() {
        try {
            listaProductos.setAll(service.obtenerProductos());
            if (tablaProductos != null) {
                tablaProductos.refresh();
            }
            if (comboBusquedaGestion != null) {
                // Forzar actualización del combo reasignando los ítems si es necesario
                comboBusquedaGestion.setItems(null);
                comboBusquedaGestion.setItems(listaProductos);
            }
            verificarStockBajo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- LÓGICA STOCK BAJO ---

    private void crearTabStockBajo() {
        tabStockBajo = new Tab("🚨 Stock Bajo");

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setPadding(new Insets(10));

        Label lblTitulo = new Label("Atención: Productos con inventario bajo o agotado.");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");

        tablaStockBajo = new TableView<>();
        TableColumn<Producto, String> colCod = new TableColumn<>("Código");
        colCod.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        TableColumn<Producto, String> colNom = new TableColumn<>("Producto");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Producto, Integer> colStk = new TableColumn<>("Stock");
        colStk.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Producto, String> colProv = new TableColumn<>("Proveedor");
        colProv.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        tablaStockBajo.getColumns().addAll(colCod, colNom, colStk, colProv);
        tablaStockBajo.setItems(listaStockBajo);
        tablaStockBajo.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        javafx.scene.layout.VBox.setVgrow(tablaStockBajo, javafx.scene.layout.Priority.ALWAYS);

        Button btnGenerarOrden = new Button("Generar Orden de Compra");
        btnGenerarOrden.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGenerarOrden.setOnAction(e -> generarOrdenCompra());

        vbox.getChildren().addAll(lblTitulo, tablaStockBajo, btnGenerarOrden);
        tabStockBajo.setContent(vbox);
    }

    private void verificarStockBajo() {
        // Filtrar productos con stock < 5
        List<Producto> bajos = listaProductos.stream()
                .filter(p -> p.getStock() < 5)
                .toList();

        Platform.runLater(() -> {
            listaStockBajo.setAll(bajos);
            if (tablaStockBajo != null) {
                tablaStockBajo.refresh();
            }

            if (bajos.size() > 0) {
                tabStockBajo.setText("🚨 Stock Bajo (" + bajos.size() + ")");
                if (!mainTabPane.getTabs().contains(tabStockBajo)) {
                    mainTabPane.getTabs().add(tabStockBajo);
                }
            } else {
                mainTabPane.getTabs().remove(tabStockBajo);
            }
        });
    }

    private void generarOrdenCompra() {
        if (listaStockBajo.isEmpty())
            return;

        String fecha = LocalDate.now().toString();
        File carpeta = new File("reportes/ordenes");
        if (!carpeta.exists())
            carpeta.mkdirs();

        File archivo = new File(carpeta, "orden_compra_" + fecha + ".txt");

        try (PrintWriter out = new PrintWriter(new FileWriter(archivo))) {
            out.println("==========================================");
            out.println("         ORDEN DE COMPRA SUGERIDA         ");
            out.println("==========================================");
            out.println("Fecha: " + fecha);
            out.println("------------------------------------------");
            out.println(String.format("%-15s %-25s %-10s %-20s", "Código", "Producto", "Stock Act.", "Proveedor"));
            out.println("------------------------------------------");

            for (Producto p : listaStockBajo) {
                out.println(String.format("%-15s %-25.25s %-10d %-20.20s",
                        p.getCodigo(), p.getNombre(), p.getStock(),
                        p.getProveedorNombre() != null ? p.getProveedorNombre() : "N/A"));
            }

            out.println("==========================================");
            mostrarAlerta("Orden Generada", "Archivo guardado en:\n" + archivo.getAbsolutePath(),
                    Alert.AlertType.INFORMATION);

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("notepad.exe", archivo.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo generar la orden: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void finalizar(String t, String m) {
        cargarDatos();
        limpiarCampos();
        mostrarAlerta(t, m, Alert.AlertType.INFORMATION);
    }

    @FXML
    protected void limpiarCampos() {
        txtNombre.clear();
        txtPrecioCompra.clear();
        txtPrecio.clear();
        txtStock.clear();
        if (txtBuscador != null)
            txtBuscador.clear();
        if (comboBusquedaGestion != null)
            comboBusquedaGestion.setValue(null);
        tablaProductos.getSelectionModel().clearSelection();
        nuevoRegistro();
    }

    private void configurarComboGestion() {
        comboBusquedaGestion.setItems(listaProductos);
        comboBusquedaGestion.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNombre() + " (" + item.getCodigo() + ")");
            }
        });
        comboBusquedaGestion.setButtonCell(comboBusquedaGestion.getCellFactory().call(null));

        comboBusquedaGestion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto p) {
                return p == null ? "" : p.getNombre() + " (" + p.getCodigo() + ")";
            }

            @Override
            public Producto fromString(String s) {
                return null;
            }
        });

        comboBusquedaGestion.setOnAction(e -> {
            Producto p = comboBusquedaGestion.getValue();
            if (p != null) {
                txtCodigo.setText(p.getCodigo());
                txtNombre.setText(p.getNombre());
                txtPrecioCompra.setText(String.format("%.0f", p.getPrecioCompra()));
                txtPrecio.setText(String.format("%.0f", p.getPrecioVenta()));
                txtStock.setText(String.valueOf(p.getStock()));

                cargarProveedorEnCombo(p);
            }
        });
    }

    private void cargarProveedorEnCombo(Producto p) {
        comboProveedorGestion.setValue(null);
        if (p.getProveedorNombre() == null || p.getProveedorNombre().isEmpty()) {
            if (!mostrandoAlertaProveedor) {
                mostrandoAlertaProveedor = true;
                Platform.runLater(() -> {
                    mostrarAlerta("Atención",
                            "Este producto no tiene un proveedor asignado. Por favor, seleccione uno.",
                            Alert.AlertType.WARNING);
                    mostrandoAlertaProveedor = false;
                });
            }
        } else {
            Optional<Proveedor> prov = comboProveedorGestion.getItems().stream()
                    .filter(pr -> pr.getNombre().equalsIgnoreCase(p.getProveedorNombre()))
                    .findFirst();

            if (prov.isPresent()) {
                comboProveedorGestion.setValue(prov.get());
            } else {
                if (!mostrandoAlertaProveedor) {
                    mostrandoAlertaProveedor = true;
                    Platform.runLater(() -> {
                        mostrarAlerta("Aviso",
                                "El proveedor '" + p.getProveedorNombre() + "' ya no existe. Asigne uno nuevo.",
                                Alert.AlertType.WARNING);
                        mostrandoAlertaProveedor = false;
                    });
                }
            }
        }
    }

    private void configurarBuscador() {
        filtroProductos = new FilteredList<>(listaProductos, p -> true);
        txtBuscador.textProperty().addListener((obs, old, val) -> {
            filtroProductos.setPredicate(p -> {
                if (val == null || val.isEmpty())
                    return true;
                String f = val.toLowerCase();
                return p.getNombre().toLowerCase().contains(f) || p.getCodigo().toLowerCase().contains(f);
            });
        });
        tablaProductos.setItems(filtroProductos);
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedorNombre"));

        colPrecio.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean e) {
                super.updateItem(p, e);
                setText(e || p == null ? null : String.format("$ %,.0f", p));
            }
        });

        tablaProductos.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>() {
                @Override
                protected void updateItem(Producto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty && item.getStock() < 5)
                        setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");
                    else
                        setStyle("");
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    onSalidaClick(); // Venta rápida por doble click
                }
            });
            return row;
        });
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #eeeeee;");
    }

    private void nuevoRegistro() {
        try {
            txtCodigo.setText(service.generarSiguienteCodigo());
        } catch (Exception e) {
            txtCodigo.setText("ERR-01");
        }
    }

    private void configurarColumnasEntradas() {
        colEntradaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEntradaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEntradaProducto.setCellValueFactory(cellData -> {
            EntradaInventario e = cellData.getValue();
            String name = e.getProductoNombre() != null ? e.getProductoNombre() : "Desconocido";
            return new javafx.beans.property.SimpleStringProperty(name + " (" + e.getProductoCodigo() + ")");
        });
        colEntradaCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEntradaCosto.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colEntradaProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colEntradaUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));

        colEntradaCosto.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean e) {
                super.updateItem(p, e);
                setText(e || p == null ? null : String.format("$ %,.0f", p));
            }
        });
    }

    @FXML
    protected void actualizarHistorialCompras() {
        try {
            listaEntradas.setAll(service.obtenerEntradas());
            tablaEntradas.setItems(listaEntradas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarColumnasVentas() {
        colVentaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colVentaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colVentaProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colVentaCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colVentaTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colVentaMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));

        colVentaTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean e) {
                super.updateItem(p, e);
                setText(e || p == null ? null : String.format("$ %,.0f", p));
            }
        });
    }

    @FXML
    protected void actualizarHistorial() {
        try {
            listaVentas.setAll(service.obtenerVentas());
            tablaVentas.setItems(listaVentas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void actualizarListaProveedores() {
        try {
            listaProveedores.setAll(service.obtenerProveedores());
            // tablaProveedores.setItems(listaProveedores); // Ya está seteado si se hizo en
            // initialize
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onGuardarProveedorClick() {
        try {
            String nombre = txtNombreProveedor.getText().trim();
            if (nombre.isEmpty())
                throw new Exception("El nombre es obligatorio.");

            Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                // Editar
                service.editarProveedor(new Proveedor(seleccionado.getId(), nombre));
            } else {
                // Nuevo
                service.registrarProveedor(new Proveedor(nombre));
            }

            actualizarListaProveedores();
            limpiarCamposProveedor();
            mostrarAlerta("Éxito", "Proveedor guardado correctamente.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onEliminarProveedorClick() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un proveedor de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("¿Eliminar proveedor?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                service.eliminarProveedor(seleccionado.getId());
                actualizarListaProveedores();
                limpiarCamposProveedor();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se puede eliminar (puede tener compras asociadas).", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    protected void limpiarCamposProveedor() {
        txtNombreProveedor.clear();
        tablaProveedores.getSelectionModel().clearSelection();
    }

    private void configurarColumnasProveedores() {
        tablaProveedores.setItems(listaProveedores);
        colProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                txtNombreProveedor.setText(newSel.getNombre());
            }
        });
    }

    @FXML
    protected void onCerrarCajaClick() {
        try {
            // 1. Confirmación obligatoria
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Cierre de Caja");
            confirm.setHeaderText("¿Desea cerrar la caja y reiniciar las ventas?");
            confirm.setContentText(
                    "Esto archivará las ventas actuales y dejará el historial en cero para el nuevo día.");

            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK)
                return;

            String hoy = LocalDate.now().toString();
            List<Venta> todas = service.obtenerVentas();

            if (todas.isEmpty()) {
                mostrarAlerta("Sin Ventas", "No hay ventas registradas para cerrar.", Alert.AlertType.INFORMATION);
                return;
            }

            double totalV = 0, totalC = 0, efectivo = 0, transferencia = 0;
            for (Venta v : todas) {
                totalV += v.getTotal();
                totalC += (v.getCostoUnitario() * v.getCantidad());
                if ("Efectivo".equalsIgnoreCase(v.getMetodoPago()))
                    efectivo += v.getTotal();
                else
                    transferencia += v.getTotal();
            }

            // 2. Guardar el resumen del cierre
            CierreCaja cierre = new CierreCaja(hoy, totalV, totalC, totalV - totalC, efectivo, transferencia);
            service.registrarCierreCaja(cierre);

            // 3. ARCHIVAR Y REINICIAR (Crucial)
            service.archivarVentasYReiniciar(todas);

            // 4. Generar reporte físico
            File carpeta = new File("reportes/cierres_diarios");
            if (!carpeta.exists())
                carpeta.mkdirs();
            File archivo = new File(carpeta, "cierre_" + hoy + ".txt");

            // Agrupar ventas por producto para el reporte detallado
            java.util.Map<String, Integer> cantPorProducto = new java.util.HashMap<>();
            java.util.Map<String, Double> totalPorProducto = new java.util.HashMap<>();

            for (Venta v : todas) {
                String nom = v.getProductoNombre() != null ? v.getProductoNombre()
                        : "Desconocido (" + v.getProductoCodigo() + ")";
                cantPorProducto.put(nom, cantPorProducto.getOrDefault(nom, 0) + v.getCantidad());
                totalPorProducto.put(nom, totalPorProducto.getOrDefault(nom, 0.0) + v.getTotal());
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(archivo))) {
                out.println("==================================================");
                out.println("          FERRETERÍA PRO - CIERRE DE CAJA         ");
                out.println("==================================================");
                out.println("Fecha y Hora: "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                out.println("Transacciones: " + todas.size());
                out.println("--------------------------------------------------");
                out.println(String.format("%-28s %6s %13s", "PRODUCTO", "CANT.", "TOTAL ($)"));
                out.println("--------------------------------------------------");

                for (String pNom : cantPorProducto.keySet()) {
                    out.println(String.format("%-28.28s %6d %13s",
                            pNom,
                            cantPorProducto.get(pNom),
                            String.format("%,.0f", totalPorProducto.get(pNom))));
                }

                out.println("==================================================");
                out.println("               RESUMEN FINANCIERO                 ");
                out.println("--------------------------------------------------");
                out.println(
                        String.format("%-25s %24s", "Ventas Totales (Bruto):", "$ " + String.format("%,.0f", totalV)));
                out.println(String.format("%-25s %24s", "Costo de Mercancía:", "$ " + String.format("%,.0f", totalC)));
                out.println(
                        String.format("%-25s %24s", "UTILIDAD NETA:", "$ " + String.format("%,.0f", totalV - totalC)));
                out.println("--------------------------------------------------");
                out.println("           DESGLOSE POR MÉTODO DE PAGO            ");
                out.println("--------------------------------------------------");
                out.println(String.format("%-25s %24s", "Efectivo:", "$ " + String.format("%,.0f", efectivo)));
                out.println(
                        String.format("%-25s %24s", "Transferencia:", "$ " + String.format("%,.0f", transferencia)));
                out.println("==================================================");
                out.println("        Cierre generado correctamente             ");
                out.println("==================================================");
            }

            actualizarHistorial(); // Refrescar la tabla (quedará vacía)
            mostrarAlerta("Cierre Exitoso",
                    "Caja cerrada. Las ventas se han archivado y reiniciado.\nEl reporte se abrirá a continuación.",
                    Alert.AlertType.INFORMATION);

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("notepad.exe", archivo.getAbsolutePath()).start();
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cerrar caja: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onReporteMensualClick() {
        String mesActual = LocalDate.now().toString().substring(0, 7);
        // Abrir la nueva interfaz dinámica en lugar de exportar un archivo de texto
        // simple
        new ReporteMensualController(service, mesActual);
    }

    @FXML
    protected void onReporteGananciasClick() {
        try {
            List<Venta> ventas = service.obtenerVentas();
            if (ventas.isEmpty()) {
                mostrarAlerta("Sin Datos", "No hay ventas registradas para generar el reporte.",
                        Alert.AlertType.INFORMATION);
                return;
            }

            double totalVentas = 0;
            double totalCostos = 0;
            double efectivo = 0;
            double transferencia = 0;

            for (Venta v : ventas) {
                totalVentas += v.getTotal();
                totalCostos += (v.getCostoUnitario() * v.getCantidad());
                if ("Efectivo".equalsIgnoreCase(v.getMetodoPago()))
                    efectivo += v.getTotal();
                else
                    transferencia += v.getTotal();
            }

            double gananciaTotal = totalVentas - totalCostos;
            double margen = totalVentas > 0 ? (gananciaTotal / totalVentas) * 100 : 0;

            // Mostrar en ventana emergente profesional
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Estado de Ventas");
            info.setHeaderText("Resumen de Ganancias (Ventas en curso)");

            String contenido = String.format(
                    "💰 TOTAL VENTAS: $ %,.0f\n" +
                            "📉 TOTAL COSTOS: $ %,.0f\n" +
                            "------------------------------------------\n" +
                            "✅ UTILIDAD BRUTA: $ %,.0f\n" +
                            "📈 RENTABILIDAD: %.2f%%\n" +
                            "------------------------------------------\n" +
                            "MÉTODOS DE PAGO:\n" +
                            "💵 Efectivo: $ %,.0f\n" +
                            "💳 Transferencia: $ %,.0f",
                    totalVentas, totalCostos, gananciaTotal, margen, efectivo, transferencia);

            info.getDialogPane().setPrefWidth(400);
            info.setContentText(contenido);
            info.showAndWait();

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo calcular: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // --- GESTIÓN DE USUARIOS ---
    @FXML
    protected void actualizarListaUsuarios() {
        try {
            listaUsuarios.setAll(new UsuarioDAO().listarTodo());
            tablaUsuarios.setItems(listaUsuarios);
            
            if (comboUsuarioRol.getItems().isEmpty()) {
                comboUsuarioRol.setItems(FXCollections.observableArrayList("Administrador", "Vendedor"));
            }
            
            configurarColumnasUsuarios();
            
            tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    txtUsuarioNombre.setText(newSelection.getNombre());
                    txtUsuarioLogin.setText(newSelection.getUsuario());
                    comboUsuarioRol.setValue(newSelection.getRol());
                    txtUsuarioPass.setText(""); // No mostrar hash
                }
            });
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo cargar la lista de usuarios.", Alert.AlertType.ERROR);
        }
    }

    private void configurarColumnasUsuarios() {
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuarioFull.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsuarioNick.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colUsuarioRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
    }

    @FXML
    protected void onGuardarUsuarioClick() {
        try {
            String nombre = txtUsuarioNombre.getText().trim();
            String login = txtUsuarioLogin.getText().trim();
            String pass = txtUsuarioPass.getText().trim();
            String rol = comboUsuarioRol.getValue();

            if (nombre.isEmpty() || login.isEmpty() || rol == null) {
                throw new Exception("Nombre, Username y Rol son obligatorios.");
            }

            Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
            UsuarioDAO dao = new UsuarioDAO();

            if (seleccionado == null) {
                // Nuevo
                if (pass.isEmpty()) throw new Exception("La contraseña es obligatoria para nuevos usuarios.");
                dao.guardar(new Usuario(0, login, "", nombre, rol), pass);
            } else {
                // Editar
                seleccionado.setNombre(nombre);
                seleccionado.setUsuario(login);
                seleccionado.setRol(rol);
                dao.actualizar(seleccionado, pass);
            }

            mostrarAlerta("Éxito", "Usuario guardado correctamente.", Alert.AlertType.INFORMATION);
            limpiarCamposUsuario();
            actualizarListaUsuarios();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onEliminarUsuarioClick() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (seleccionado.getUsuario().equalsIgnoreCase("admin")) {
            mostrarAlerta("Error", "No se puede eliminar al administrador principal.", Alert.AlertType.ERROR);
            return;
        }

        try {
            new UsuarioDAO().eliminar(seleccionado.getId());
            actualizarListaUsuarios();
            limpiarCamposUsuario();
            mostrarAlerta("Éxito", "Usuario eliminado.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void limpiarCamposUsuario() {
        txtUsuarioNombre.clear();
        txtUsuarioLogin.clear();
        txtUsuarioPass.clear();
        comboUsuarioRol.setValue(null);
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String t, String m, Alert.AlertType tp) {
        Alert a = new Alert(tp);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}
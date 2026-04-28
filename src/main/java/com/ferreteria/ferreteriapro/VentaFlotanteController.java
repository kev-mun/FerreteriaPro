package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ventana flotante NO MODAL para gestionar una venta en curso.
 * No bloquea la ventana principal. Puede moverse y minimizarse.
 */
public class VentaFlotanteController {

    private final Stage stage;
    private final InventarioService service;
    private final Consumer<Void> onVentaCompletada;

    private final ObservableList<CarritoItem> carritoItems = FXCollections.observableArrayList();
    private TableView<CarritoItem> tablaCarrito;
    private Label lblTotal;

    // -------- Modelo del carrito --------
    public static class CarritoItem {
        private final Producto producto;
        private int cantidad;

        public CarritoItem(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }

        public String getNombre()        { return producto.getNombre(); }
        public String getCodigo()        { return producto.getCodigo(); }
        public int    getCantidad()      { return cantidad; }
        public void   setCantidad(int c) { this.cantidad = c; }
        public double getPrecioUnitario(){ return producto.getPrecioVenta(); }
        public double getSubtotal()      { return producto.getPrecioVenta() * cantidad; }
        public Producto getProducto()    { return producto; }
    }

    /**
     * @param productoInicial producto pre-seleccionado (puede ser null).
     */
    public VentaFlotanteController(
            InventarioService service,
            Consumer<Void> onVentaCompletada,
            Producto productoInicial) {

        this.service = service;
        this.onVentaCompletada = onVentaCompletada;

        stage = new Stage();
        stage.setTitle("🛒 Venta en Proceso");
        stage.setMinWidth(560);
        stage.setMinHeight(460);
        stage.setAlwaysOnTop(false);

        Scene scene = new Scene(construirUI(productoInicial), 580, 500);
        stage.setScene(scene);
        stage.show();

        // Agregar producto inicial al carrito si viene de "Realizar Venta"
        if (productoInicial != null) {
            carritoItems.add(new CarritoItem(productoInicial, 1));
            actualizarTotal();
        }
    }

    private VBox construirUI(Producto productoInicial) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #1e1e2e;");

        // ── Título ──────────────────────────────────────────────────────────
        Label lblTitulo = new Label("🛒  Venta en Curso");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTitulo.setStyle("-fx-text-fill: #cba6f7;");

        // ── Buscador + Popup de sugerencias ─────────────────────────────────
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("🔍  Buscar producto por nombre o código...");
        txtBuscar.setStyle(
                "-fx-background-color: #313244; -fx-text-fill: #cdd6f4;" +
                "-fx-prompt-text-fill: #6c7086; -fx-border-color: #89b4fa;" +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8; -fx-font-size: 13px;");

        Button btnAgregar = new Button("➕ Agregar");
        btnAgregar.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e;" +
                "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");

        HBox busquedaBox = new HBox(8, txtBuscar, btnAgregar);
        busquedaBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        // Popup que flota ENCIMA de todos los controles
        Popup popup = new Popup();
        popup.setAutoHide(true);

        ListView<Producto> listaSugerencias = new ListView<>();
        listaSugerencias.setPrefHeight(160);
        listaSugerencias.setPrefWidth(480);
        listaSugerencias.setStyle(
                "-fx-background-color: #313244; -fx-border-color: #89b4fa; -fx-border-width: 1.5;");
        listaSugerencias.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Producto p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                } else {
                    setText(p.getNombre() + "   |   " + p.getCodigo() +
                            "   |   $ " + String.format("%,.0f", p.getPrecioVenta()) +
                            "   |   Stock: " + p.getStock());
                    setStyle("-fx-text-fill: #cdd6f4; -fx-background-color: #313244;");
                }
            }
        });
        popup.getContent().add(listaSugerencias);

        // Cargar todos los productos
        ObservableList<Producto> todos = FXCollections.observableArrayList();
        try { todos.addAll(service.obtenerProductos()); } catch (Exception ignored) {}

        // Filtrar mientras escribe
        txtBuscar.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                popup.hide();
                return;
            }
            String filtro = val.toLowerCase();
            ObservableList<Producto> filtrados = todos.filtered(p ->
                    p.getNombre().toLowerCase().contains(filtro) ||
                    p.getCodigo().toLowerCase().contains(filtro));

            if (filtrados.isEmpty()) {
                popup.hide();
            } else {
                listaSugerencias.setItems(filtrados);
                // Posicionar el popup justo debajo del campo de búsqueda
                if (!popup.isShowing() && stage.isShowing()) {
                    var bounds = txtBuscar.localToScreen(txtBuscar.getBoundsInLocal());
                    if (bounds != null) {
                        popup.show(stage, bounds.getMinX(), bounds.getMaxY() + 2);
                    }
                }
            }
        });

        // Seleccionar por clic
        listaSugerencias.setOnMouseClicked(e -> {
            Producto p = listaSugerencias.getSelectionModel().getSelectedItem();
            if (p != null) {
                agregarAlCarrito(p);
                txtBuscar.clear();
                popup.hide();
            }
        });

        // Agregar con botón (toma el primero de la lista si hay)
        btnAgregar.setOnAction(e -> {
            Producto p = listaSugerencias.getSelectionModel().getSelectedItem();
            if (p == null && !listaSugerencias.getItems().isEmpty())
                p = listaSugerencias.getItems().get(0);
            if (p != null) {
                agregarAlCarrito(p);
                txtBuscar.clear();
                popup.hide();
            }
        });

        // ── Tabla carrito ────────────────────────────────────────────────────
        tablaCarrito = new TableView<>(carritoItems);
        tablaCarrito.setStyle("-fx-background-color: #181825;");
        tablaCarrito.setPlaceholder(new Label("Sin productos. Busca arriba para agregar."));
        VBox.setVgrow(tablaCarrito, Priority.ALWAYS);

        TableColumn<CarritoItem, String> cNom = new TableColumn<>("Producto");
        cNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        cNom.setPrefWidth(180);

        TableColumn<CarritoItem, String> cPrecio = new TableColumn<>("P. Unit.");
        cPrecio.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                "$ " + String.format("%,.0f", c.getValue().getPrecioUnitario())));
        cPrecio.setPrefWidth(90);

        TableColumn<CarritoItem, String> cSub = new TableColumn<>("Subtotal");
        cSub.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                "$ " + String.format("%,.0f", c.getValue().getSubtotal())));
        cSub.setPrefWidth(90);

        // Columna de acciones: [ - ] cantidad [ + ] y botón eliminar
        TableColumn<CarritoItem, Void> cAcciones = new TableColumn<>("Cantidad / Acciones");
        cAcciones.setPrefWidth(180);
        cAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnMenos  = new Button("−");
            private final Button btnMas    = new Button("+");
            private final Label  lblCant   = new Label();
            private final Button btnEditar = new Button("✏");
            private final Button btnDel    = new Button("✖");
            private final HBox   box       = new HBox(4, btnMenos, lblCant, btnMas, btnEditar, btnDel);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                String estBase = "-fx-font-weight:bold; -fx-padding:3 8; -fx-background-radius:5; -fx-cursor:hand;";
                btnMenos.setStyle(estBase + "-fx-background-color:#89b4fa; -fx-text-fill:#1e1e2e;");
                btnMas.setStyle(  estBase + "-fx-background-color:#a6e3a1; -fx-text-fill:#1e1e2e;");
                btnEditar.setStyle(estBase + "-fx-background-color:#f9e2af; -fx-text-fill:#1e1e2e;");
                btnDel.setStyle(  estBase + "-fx-background-color:#f38ba8; -fx-text-fill:white;");
                lblCant.setStyle("-fx-text-fill:#cdd6f4; -fx-font-weight:bold; -fx-min-width:28; -fx-alignment:center;");

                btnMenos.setOnAction(e -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    if (item.getCantidad() <= 1) {
                        carritoItems.remove(item);
                    } else {
                        item.setCantidad(item.getCantidad() - 1);
                        tablaCarrito.refresh();
                    }
                    actualizarTotal();
                });

                btnMas.setOnAction(e -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    int nuevoStock = item.getProducto().getStock(); // stock original aún en BD
                    if (item.getCantidad() + 1 > nuevoStock) {
                        error("Stock insuficiente. Disponible: " + nuevoStock);
                        return;
                    }
                    item.setCantidad(item.getCantidad() + 1);
                    tablaCarrito.refresh();
                    actualizarTotal();
                });

                btnEditar.setOnAction(e -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    TextInputDialog dlg = new TextInputDialog(String.valueOf(item.getCantidad()));
                    dlg.setTitle("Editar cantidad");
                    dlg.setHeaderText(item.getNombre());
                    dlg.setContentText("Nueva cantidad (stock: " + item.getProducto().getStock() + "):");
                    dlg.initOwner(stage);
                    dlg.showAndWait().ifPresent(val -> {
                        try {
                            int nueva = Integer.parseInt(val.trim());
                            if (nueva <= 0) {
                                carritoItems.remove(item);
                            } else if (nueva > item.getProducto().getStock()) {
                                error("Stock insuficiente. Disponible: " + item.getProducto().getStock());
                            } else {
                                item.setCantidad(nueva);
                                tablaCarrito.refresh();
                            }
                            actualizarTotal();
                        } catch (NumberFormatException ex) {
                            error("Ingrese un número válido.");
                        }
                    });
                });

                btnDel.setOnAction(e -> {
                    carritoItems.remove(getTableView().getItems().get(getIndex()));
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    lblCant.setText(String.valueOf(item.getCantidad()));
                    setGraphic(box);
                }
            }
        });

        tablaCarrito.getColumns().add(cNom);
        tablaCarrito.getColumns().add(cPrecio);
        tablaCarrito.getColumns().add(cSub);
        tablaCarrito.getColumns().add(cAcciones);
        tablaCarrito.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // ── Total ────────────────────────────────────────────────────────────
        lblTotal = new Label("TOTAL:  $ 0");
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTotal.setStyle("-fx-text-fill: #a6e3a1;");
        HBox totalBox = new HBox(lblTotal);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        // ── Botones ──────────────────────────────────────────────────────────
        Button btnCancelar = new Button("❌ Cancelar");
        btnCancelar.setStyle("-fx-background-color: #45475a; -fx-text-fill: #cdd6f4;" +
                "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnCobrar = new Button("💰  COBRAR");
        btnCobrar.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e;" +
                "-fx-font-weight: bold; -fx-font-size: 15px;" +
                "-fx-padding: 10 35; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCobrar.setOnAction(e -> procesarCobro());

        HBox botonesBox = new HBox(12, btnCancelar, btnCobrar);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, busquedaBox, tablaCarrito, totalBox, botonesBox);
        return root;
    }

    /** Agrega o incrementa un producto en el carrito. */
    public void agregarAlCarrito(Producto p) {
        if (p.getStock() <= 0) {
            error("Sin stock disponible para: " + p.getNombre());
            return;
        }
        for (CarritoItem item : carritoItems) {
            if (item.getCodigo().equals(p.getCodigo())) {
                if (item.getCantidad() + 1 > p.getStock()) {
                    error("Stock insuficiente. Disponible: " + p.getStock());
                    return;
                }
                item.setCantidad(item.getCantidad() + 1);
                tablaCarrito.refresh();
                actualizarTotal();
                return;
            }
        }
        carritoItems.add(new CarritoItem(p, 1));
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = carritoItems.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        lblTotal.setText("TOTAL:  $ " + String.format("%,.0f", total));
    }

    private void procesarCobro() {
        if (carritoItems.isEmpty()) { error("No hay productos en el carrito."); return; }

        double total = carritoItems.stream().mapToDouble(CarritoItem::getSubtotal).sum();

        // ── 1. Seleccionar método de pago ────────────────────────────────────
        ChoiceDialog<String> dMetodo = new ChoiceDialog<>("Efectivo", "Efectivo", "Transferencia");
        dMetodo.setTitle("Método de Pago");
        dMetodo.setHeaderText("Total a cobrar:  $ " + String.format("%,.0f", total));
        dMetodo.setContentText("Seleccione método:");
        dMetodo.initOwner(stage);
        var mRes = dMetodo.showAndWait();
        if (mRes.isEmpty()) return;
        String metodo = mRes.get();

        // ── 2. Ingresar monto (con bucle de corrección) ──────────────────────
        double pago = 0;
        double cambio = 0;
        if ("Efectivo".equals(metodo)) {
            while (true) {
                TextInputDialog dPago = new TextInputDialog(pago > 0 ? String.format("%.0f", pago) : "");
                dPago.setTitle("Pago en Efectivo");
                dPago.setHeaderText("Total a cobrar: $ " + String.format("%,.0f", total));
                dPago.setContentText("¿Con cuánto paga el cliente?");
                dPago.initOwner(stage);

                ButtonType btnVolver = new ButtonType("⬅ Cambiar método", javafx.scene.control.ButtonBar.ButtonData.BACK_PREVIOUS);
                dPago.getDialogPane().getButtonTypes().add(btnVolver);

                var pRes = dPago.showAndWait();
                if (pRes.isEmpty()) return; // Canceló o presionó Volver → regresa al carrito

                try {
                    pago = Double.parseDouble(pRes.get().trim().replace(",", ""));
                } catch (NumberFormatException ex) {
                    alertaAviso("Valor inválido", "Ingrese solo números. Intente de nuevo.");
                    continue;
                }

                if (pago < total) {
                    alertaAviso("Pago insuficiente",
                            "El cliente paga: $ " + String.format("%,.0f", pago) +
                            "\nFaltan: $ " + String.format("%,.0f", total - pago) +
                            "\n\nPresione OK para corregir el monto.");
                    continue;
                }

                cambio = pago - total;
                break;
            }
        } else {
            pago = total;
        }

        // ── 3. CONFIRMACIÓN ANTES DE GUARDAR (aquí puede volver atrás) ───────
        String resumen = "📦 Productos: " + carritoItems.size() + " ítems\n" +
                "💳 Método: " + metodo + "\n" +
                "💵 Total: $ " + String.format("%,.0f", total);
        if ("Efectivo".equals(metodo)) {
            resumen += "\n💰 Pago recibido: $ " + String.format("%,.0f", pago) +
                       "\n🪙 Cambio: $ " + String.format("%,.0f", cambio);
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Venta");
        confirmacion.setHeaderText("¿Confirmar y registrar la venta?");
        confirmacion.setContentText(resumen);
        confirmacion.initOwner(stage);

        ButtonType btnConfirmar = new ButtonType("✅ Confirmar Venta");
        ButtonType btnCorregirPago = new ButtonType("✏ Corregir Pago", javafx.scene.control.ButtonBar.ButtonData.BACK_PREVIOUS);
        ButtonType btnCancelarVenta = new ButtonType("❌ Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnConfirmar, btnCorregirPago, btnCancelarVenta);

        var confRes = confirmacion.showAndWait();
        if (confRes.isEmpty() || confRes.get() == btnCancelarVenta) return;

        // Si quiere corregir el pago, reiniciar desde el método de pago
        if (confRes.get() == btnCorregirPago) {
            procesarCobro(); // rellamar recursivamente para volver al inicio
            return;
        }

        // ── 4. GUARDAR EN BD (solo si confirmó) ─────────────────────────────
        final double pagoFinal = pago;
        final double cambioFinal = cambio;
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String usuarioActual = Session.getCurrentUser() != null ? Session.getCurrentUser().getNombre() : "Desconocido";
        try {
            for (CarritoItem item : carritoItems) {
                Producto p = item.getProducto();
                p.setStock(p.getStock() - item.getCantidad());
                service.editarProducto(p);
                service.registrarVenta(new Venta(fecha, p.getCodigo(), p.getNombre(),
                        item.getCantidad(), item.getSubtotal(), metodo, p.getPrecioCompra(), usuarioActual));
            }

            // ── 5. Oferta de factura ─────────────────────────────────────────
            String msgExito = "✅ Venta registrada: $ " + String.format("%,.0f", total);
            if ("Efectivo".equals(metodo))
                msgExito += "\n💰 Cambio: $ " + String.format("%,.0f", cambioFinal);

            Alert alertFactura = new Alert(Alert.AlertType.CONFIRMATION);
            alertFactura.setTitle("Venta Exitosa");
            alertFactura.setHeaderText(msgExito);
            alertFactura.setContentText("¿Desea generar la factura profesional?");
            alertFactura.initOwner(stage);

            ButtonType btnSi = new ButtonType("📄 Generar Factura");
            ButtonType btnNo = new ButtonType("Solo Cerrar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            alertFactura.getButtonTypes().setAll(btnSi, btnNo);

            var respuesta = alertFactura.showAndWait();
            if (respuesta.isPresent() && respuesta.get() == btnSi) {
                generarFactura(total, metodo, pagoFinal, cambioFinal);
            }

            if (onVentaCompletada != null)
                javafx.application.Platform.runLater(() -> onVentaCompletada.accept(null));

            stage.close();
        } catch (Exception e) {
            error("Error al registrar: " + e.getMessage());
        }
    }

    private void alertaAviso(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.initOwner(stage); a.showAndWait();
    }

    private void generarFactura(double total, String metodo, double pago, double cambio) {
        String fecha = java.time.LocalDate.now().toString();
        String idFactura = "FAC-" + System.currentTimeMillis();

        java.io.File carpeta = new java.io.File("facturas");
        if (!carpeta.exists()) carpeta.mkdir();
        java.io.File archivo = new java.io.File(carpeta, idFactura + ".txt");

        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(archivo))) {
            out.println("==========================================");
            out.println("           FERRETERÍA PRO                 ");
            out.println("        FACTURA DE VENTA                  ");
            out.println("==========================================");
            out.println("Fecha:      " + fecha);
            out.println("Factura No: " + idFactura);
            out.println("------------------------------------------");
            out.println(String.format("%-22s %6s %12s", "Producto", "Cant.", "Subtotal"));
            out.println("------------------------------------------");
            for (CarritoItem item : carritoItems) {
                out.println(String.format("%-22.22s %6d  $%,.0f",
                        item.getNombre(), item.getCantidad(), item.getSubtotal()));
            }
            out.println("------------------------------------------");
            out.println(String.format("%-22s %6s  $%,.0f", "TOTAL", "", total));
            out.println("Método de Pago: " + metodo);
            if ("Efectivo".equals(metodo)) {
                out.println("Pagó con:       $ " + String.format("%,.0f", pago));
                out.println("Cambio:         $ " + String.format("%,.0f", cambio));
            }
            out.println("==========================================");
            out.println("       ¡Gracias por su compra!            ");
            out.println("==========================================");

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("notepad.exe", archivo.getAbsolutePath()).start();
            }
        } catch (java.io.IOException e) {
            error("No se pudo guardar la factura: " + e.getMessage());
        }
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg);
        a.initOwner(stage); a.showAndWait();
    }

    public Stage getStage() { return stage; }
}

package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.service.InventarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReporteMensualController {

    private final Stage stage;
    private final InventarioService service;
    private final String mesAno; // Formato YYYY-MM
    
    private List<Venta> ventasMes;
    private List<Producto> inventario;

    public ReporteMensualController(InventarioService service, String mesAno) {
        this.service = service;
        this.mesAno = mesAno;
        
        stage = new Stage();
        stage.setTitle("📊 Reporte Avanzado: " + mesAno);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        
        cargarDatosYMostrar();
    }
    
    private void cargarDatosYMostrar() {
        try {
            ventasMes = service.obtenerVentasPorMes(mesAno);
            inventario = service.obtenerProductos();
            
            if (ventasMes.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Sin datos");
                a.setHeaderText(null);
                a.setContentText("No hay ventas registradas para el mes " + mesAno);
                a.showAndWait();
                return;
            }
            
            Scene scene = new Scene(construirUI(), 950, 750);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Error al cargar datos: " + e.getMessage());
            a.showAndWait();
        }
    }
    
    private VBox construirUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e2e; -fx-text-fill: #cdd6f4;");

        Label lblTitulo = new Label("📈 Análisis Estratégico Mensual - " + mesAno);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 26));
        lblTitulo.setStyle("-fx-text-fill: #cba6f7;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #313244;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        tabPane.getTabs().addAll(
            crearTabResumen(),
            crearTabVentasDiarias(),
            crearTabAnalisisProductos()
        );
        
        Button btnCerrar = new Button("Cerrar Reporte");
        btnCerrar.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        btnCerrar.setOnAction(e -> stage.close());
        
        HBox boxBotones = new HBox(btnCerrar);
        boxBotones.setAlignment(Pos.CENTER_RIGHT);
        
        root.getChildren().addAll(lblTitulo, tabPane, boxBotones);
        return root;
    }
    
    private Tab crearTabResumen() {
        Tab tab = new Tab("📊 Resumen General y Finanzas");
        
        double ingresosBrutos = ventasMes.stream().mapToDouble(Venta::getTotal).sum();
        double costosTotales = ventasMes.stream().mapToDouble(v -> v.getCostoUnitario() * v.getCantidad()).sum();
        double utilidadNeta = ingresosBrutos - costosTotales;
        double margenGlobal = ingresosBrutos > 0 ? (utilidadNeta / ingresosBrutos) * 100 : 0;
        
        int totalProductosVendidos = ventasMes.stream().mapToInt(Venta::getCantidad).sum();
        
        // Calcular transacciones únicas basándonos en la fecha/hora (yyyy-MM-dd HH:mm:ss)
        Set<String> transacciones = ventasMes.stream().map(Venta::getFecha).collect(Collectors.toSet());
        int numTransacciones = transacciones.size();
        
        double ticketPromedio = numTransacciones > 0 ? ingresosBrutos / numTransacciones : 0;
        
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);
        grid.setPadding(new Insets(30));
        
        grid.add(crearTarjeta("Ingresos Brutos", "$ " + String.format("%,.0f", ingresosBrutos), "#a6e3a1"), 0, 0);
        grid.add(crearTarjeta("Utilidad Neta", "$ " + String.format("%,.0f", utilidadNeta), "#f9e2af"), 1, 0);
        grid.add(crearTarjeta("Margen Global", String.format("%.2f %%", margenGlobal), "#89b4fa"), 2, 0);
        
        grid.add(crearTarjeta("Total Transacciones", String.valueOf(numTransacciones), "#cba6f7"), 0, 1);
        grid.add(crearTarjeta("Ticket Promedio", "$ " + String.format("%,.0f", ticketPromedio), "#f5c2e7"), 1, 1);
        grid.add(crearTarjeta("Unidades Vendidas", String.valueOf(totalProductosVendidos), "#94e2d5"), 2, 1);
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #181825; -fx-border-color: transparent;");
        tab.setContent(scroll);
        return tab;
    }
    
    private Tab crearTabVentasDiarias() {
        Tab tab = new Tab("📅 Ventas por Día");
        
        // Agrupar por día (yyyy-MM-dd)
        Map<String, List<Venta>> porDia = ventasMes.stream()
            .collect(Collectors.groupingBy(v -> v.getFecha().substring(0, 10)));
            
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #181825;");
        
        double maxVenta = -1;
        String diaMax = "";
        double minVenta = Double.MAX_VALUE;
        String diaMin = "";
        
        GridPane gridDias = new GridPane();
        gridDias.setHgap(20);
        gridDias.setVgap(10);
        gridDias.setStyle("-fx-background-color: #313244; -fx-padding: 15; -fx-background-radius: 8;");
        
        Label h1 = new Label("Día"); h1.setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;");
        Label h2 = new Label("Ingresos"); h2.setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;");
        Label h3 = new Label("Transacciones"); h3.setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;");
        gridDias.addRow(0, h1, h2, h3);
        
        int row = 1;
        List<String> diasOrdenados = new ArrayList<>(porDia.keySet());
        Collections.sort(diasOrdenados);
        
        for (String dia : diasOrdenados) {
            List<Venta> ventasDia = porDia.get(dia);
            double totalDia = ventasDia.stream().mapToDouble(Venta::getTotal).sum();
            int transDia = ventasDia.stream().map(Venta::getFecha).collect(Collectors.toSet()).size();
            
            if (totalDia > maxVenta) { maxVenta = totalDia; diaMax = dia; }
            if (totalDia < minVenta) { minVenta = totalDia; diaMin = dia; }
            
            Label lDia = new Label(dia); lDia.setStyle("-fx-text-fill: #cdd6f4;");
            Label lTot = new Label("$ " + String.format("%,.0f", totalDia)); lTot.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
            Label lTrx = new Label(String.valueOf(transDia)); lTrx.setStyle("-fx-text-fill: #cdd6f4;");
            
            gridDias.addRow(row++, lDia, lTot, lTrx);
        }
        
        HBox highlights = new HBox(30);
        highlights.getChildren().addAll(
            crearTarjeta("🔥 Día con mayor venta", diaMax + " ($ " + String.format("%,.0f", maxVenta) + ")", "#f38ba8"),
            crearTarjeta("❄️ Día con menor venta", diaMin + " ($ " + String.format("%,.0f", minVenta) + ")", "#74c7ec")
        );
        
        ScrollPane scroll = new ScrollPane(gridDias);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        vbox.getChildren().addAll(highlights, scroll);
        tab.setContent(vbox);
        return tab;
    }
    
    private Tab crearTabAnalisisProductos() {
        Tab tab = new Tab("📦 Análisis de Productos");
        
        Map<String, List<Venta>> porProducto = ventasMes.stream()
            .collect(Collectors.groupingBy(Venta::getProductoCodigo));
            
        class ProdInfo {
            String codigo, nombre;
            int unidades;
            double ingresos, costos;
        }
        
        List<ProdInfo> ranking = new ArrayList<>();
        Set<String> codigosVendidos = new HashSet<>();
        
        for (Map.Entry<String, List<Venta>> entry : porProducto.entrySet()) {
            ProdInfo p = new ProdInfo();
            p.codigo = entry.getKey();
            p.nombre = entry.getValue().get(0).getProductoNombre();
            p.unidades = entry.getValue().stream().mapToInt(Venta::getCantidad).sum();
            p.ingresos = entry.getValue().stream().mapToDouble(Venta::getTotal).sum();
            p.costos = entry.getValue().stream().mapToDouble(v -> v.getCostoUnitario() * v.getCantidad()).sum();
            ranking.add(p);
            codigosVendidos.add(p.codigo);
        }
        
        ranking.sort((a, b) -> Integer.compare(b.unidades, a.unidades)); // Descendente
        
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #181825;");
        
        Label lblTop = new Label("🌟 Top 10 Más Vendidos (por unidades)");
        lblTop.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        GridPane gridTop = new GridPane();
        gridTop.setHgap(20); gridTop.setVgap(10);
        gridTop.addRow(0, 
            header("Producto"), header("Unidades"), header("Ingresos"), header("Margen")
        );
        
        for (int i = 0; i < Math.min(10, ranking.size()); i++) {
            ProdInfo p = ranking.get(i);
            double util = p.ingresos - p.costos;
            double mg = p.ingresos > 0 ? (util / p.ingresos) * 100 : 0;
            
            gridTop.addRow(i+1,
                celda(p.nombre + " (" + p.codigo + ")"),
                celda(String.valueOf(p.unidades)),
                celdaVerde("$ " + String.format("%,.0f", p.ingresos)),
                celda(String.format("%.1f%%", mg))
            );
        }
        
        // Menos vendidos (mayor que 0 pero baja rotación, invertimos la lista)
        Label lblFlop = new Label("⚠️ Baja Rotación (Menos vendidos)");
        lblFlop.setStyle("-fx-text-fill: #f9e2af; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        GridPane gridFlop = new GridPane();
        gridFlop.setHgap(20); gridFlop.setVgap(10);
        gridFlop.addRow(0, header("Producto"), header("Unidades"), header("Ingresos"));
        
        List<ProdInfo> inverso = new ArrayList<>(ranking);
        inverso.sort(Comparator.comparingInt(a -> a.unidades)); // Ascendente
        
        for (int i = 0; i < Math.min(5, inverso.size()); i++) {
            ProdInfo p = inverso.get(i);
            gridFlop.addRow(i+1,
                celda(p.nombre + " (" + p.codigo + ")"),
                celda(String.valueOf(p.unidades)),
                celda("$ " + String.format("%,.0f", p.ingresos))
            );
        }
        
        // Productos sin ventas
        Label lblCero = new Label("🚫 Productos Sin Ventas en el Mes");
        lblCero.setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        String sinVentasText = inventario.stream()
            .filter(inv -> !codigosVendidos.contains(inv.getCodigo()))
            .map(inv -> inv.getNombre() + " (" + inv.getStock() + " en stock)")
            .collect(Collectors.joining("\n"));
            
        TextArea txtCero = new TextArea(sinVentasText.isEmpty() ? "Todos los productos tuvieron ventas." : sinVentasText);
        txtCero.setEditable(false);
        txtCero.setPrefRowCount(5);
        txtCero.setStyle("-fx-control-inner-background: #313244; -fx-text-fill: #cdd6f4;");
        
        ScrollPane scroll = new ScrollPane(new VBox(15, lblTop, gridTop, new Separator(), lblFlop, gridFlop, new Separator(), lblCero, txtCero));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #181825; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        vbox.getChildren().add(scroll);
        tab.setContent(vbox);
        return tab;
    }
    
    private VBox crearTarjeta(String titulo, String valor, String colorHex) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #313244; -fx-background-radius: 10; -fx-border-color: " + colorHex + "; -fx-border-width: 0 0 4 0; -fx-border-radius: 10;");
        box.setMinWidth(250);
        
        Label lT = new Label(titulo);
        lT.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 14px;");
        
        Label lV = new Label(valor);
        lV.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        box.getChildren().addAll(lT, lV);
        return box;
    }
    
    private Label header(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;");
        return l;
    }
    private Label celda(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #cdd6f4;");
        return l;
    }
    private Label celdaVerde(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
        return l;
    }
}

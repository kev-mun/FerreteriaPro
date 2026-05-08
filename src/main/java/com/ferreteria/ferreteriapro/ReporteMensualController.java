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
import javafx.scene.chart.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReporteMensualController {

    private final Stage stage;
    private final InventarioService service;
    private final String mesAno;
    
    private List<Venta> ventasMes;
    private List<Producto> inventario;

    public ReporteMensualController(InventarioService service, String mesAno) {
        this.service = service;
        this.mesAno = mesAno;
        
        this.stage = new Stage();
        this.stage.setTitle("Reporte Mensual: " + mesAno);
        this.stage.setMinWidth(950);
        this.stage.setMinHeight(750);
        
        cargarDatosYMostrar();
    }
    
    private void cargarDatosYMostrar() {
        try {
            ventasMes = service.obtenerVentasPorMes(mesAno);
            inventario = service.obtenerProductos();
            
            if (ventasMes == null || ventasMes.isEmpty()) {
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
        root.setStyle("-fx-background-color: #1e1e2e;");

        Label lblTitulo = new Label("Analisis Mensual - " + mesAno);
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 26));
        lblTitulo.setStyle("-fx-text-fill: #cba6f7;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #313244;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        tabPane.getTabs().addAll(
            crearTabResumen(),
            crearTabVentasDiarias(),
            crearTabGraficos(),
            crearTabAnalisisProductos(),
            crearTabListadoDetallado()
        );
        
        Button btnPDF = new Button("Exportar PDF");
        btnPDF.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; -fx-font-weight: bold;");
        btnPDF.setOnAction(e -> exportarAPDF());
        
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: white;");
        btnCerrar.setOnAction(e -> stage.close());
        
        HBox boxBotones = new HBox(15, btnPDF, btnCerrar);
        boxBotones.setAlignment(Pos.CENTER_RIGHT);
        
        root.getChildren().addAll(lblTitulo, tabPane, boxBotones);
        return root;
    }
    
    private Tab crearTabResumen() {
        Tab tab = new Tab("Resumen");
        double ingresos = ventasMes.stream().filter(v -> !"ANULADA".equals(v.getEstado())).mapToDouble(Venta::getTotal).sum();
        double costos = ventasMes.stream().filter(v -> !"ANULADA".equals(v.getEstado())).mapToDouble(v -> v.getCostoUnitario() * v.getCantidad()).sum();
        
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20); grid.setPadding(new Insets(20));
        grid.add(crearTarjeta("Ingresos Brutos", "$ " + String.format("%,.0f", ingresos), "#a6e3a1"), 0, 0);
        grid.add(crearTarjeta("Utilidad", "$ " + String.format("%,.0f", ingresos - costos), "#f9e2af"), 1, 0);
        
        tab.setContent(new ScrollPane(grid));
        return tab;
    }
    
    private Tab crearTabVentasDiarias() {
        Tab tab = new Tab("Ventas por Dia");
        Map<String, List<Venta>> porDia = ventasMes.stream()
            .filter(v -> !"ANULADA".equals(v.getEstado()))
            .collect(Collectors.groupingBy(v -> v.getFecha().substring(0, 10)));
            
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.addRow(0, header("Dia"), header("Venta Total"));
        
        List<String> dias = new ArrayList<>(porDia.keySet());
        Collections.sort(dias);
        int r = 1;
        for (String dia : dias) {
            double total = porDia.get(dia).stream().mapToDouble(Venta::getTotal).sum();
            grid.addRow(r++, celda(dia), celdaVerde("$ " + String.format("%,.0f", total)));
        }
        
        vbox.getChildren().add(grid);
        tab.setContent(new ScrollPane(vbox));
        return tab;
    }

    private Tab crearTabGraficos() {
        Tab tab = new Tab("Graficos");
        VBox root = new VBox(20);
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Tendencia de Ventas");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> porDia = ventasMes.stream()
            .filter(v -> !"ANULADA".equals(v.getEstado()))
            .collect(Collectors.groupingBy(v -> v.getFecha().substring(8, 10), TreeMap::new, Collectors.summingDouble(Venta::getTotal)));
        porDia.forEach((d, t) -> series.getData().add(new XYChart.Data<>(d, t)));
        lineChart.getData().add(series);
        
        root.getChildren().add(lineChart);
        tab.setContent(root);
        return tab;
    }
    
    private Tab crearTabAnalisisProductos() {
        Tab tab = new Tab("Productos");
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        
        Map<String, Integer> porUnidades = ventasMes.stream()
            .filter(v -> !"ANULADA".equals(v.getEstado()))
            .collect(Collectors.groupingBy(Venta::getProductoNombre, Collectors.summingInt(Venta::getCantidad)));
            
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.addRow(0, header("Producto"), header("Unidades Sold"));
        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(porUnidades.entrySet());
        sorted.sort((a,b) -> b.getValue().compareTo(a.getValue()));
        
        int r = 1;
        for (Map.Entry<String, Integer> e : sorted) {
            if (r > 15) break;
            grid.addRow(r++, celda(e.getKey()), celda(String.valueOf(e.getValue())));
        }
        
        vbox.getChildren().add(grid);
        tab.setContent(new ScrollPane(vbox));
        return tab;
    }

    private Tab crearTabListadoDetallado() {
        Tab tab = new Tab("Listado Completo");
        TableView<Venta> tabla = new TableView<>();
        
        TableColumn<Venta, String> colF = new TableColumn<>("Fecha");
        colF.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        TableColumn<Venta, String> colP = new TableColumn<>("Producto");
        colP.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        TableColumn<Venta, Integer> colC = new TableColumn<>("Cant");
        colC.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        TableColumn<Venta, Double> colT = new TableColumn<>("Total");
        colT.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        tabla.getColumns().addAll(colF, colP, colC, colT);
        tabla.getItems().addAll(ventasMes);
        tab.setContent(tabla);
        return tab;
    }
    
    private VBox crearTarjeta(String titulo, String valor, String color) {
        VBox b = new VBox(5);
        b.setPadding(new Insets(15));
        b.setStyle("-fx-background-color: #313244; -fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");
        Label l1 = new Label(titulo); l1.setStyle("-fx-text-fill: #a6adc8;");
        Label l2 = new Label(valor); l2.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20; -fx-font-weight: bold;");
        b.getChildren().addAll(l1, l2);
        return b;
    }
    
    private Label header(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill: #cba6f7; -fx-font-weight: bold;"); return l; }
    private Label celda(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill: #cdd6f4;"); return l; }
    private Label celdaVerde(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill: #a6e3a1;"); return l; }

    private void exportarAPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF");
        fileChooser.setInitialFileName("Reporte_" + mesAno + ".pdf");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (FileOutputStream out = new FileOutputStream(file)) {
            com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();
            
            com.lowagie.text.Font fontT = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 20);
            doc.add(new com.lowagie.text.Paragraph("REPORTE MENSUAL - " + mesAno, fontT));
            doc.add(new com.lowagie.text.Paragraph("Generado: " + LocalDateTime.now().toString()));
            doc.add(new com.lowagie.text.Paragraph(" "));
            
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
            table.addCell("Fecha"); table.addCell("Producto"); table.addCell("Total");
            
            for (Venta v : ventasMes) {
                if (!"ANULADA".equals(v.getEstado())) {
                    table.addCell(v.getFecha());
                    table.addCell(v.getProductoNombre());
                    table.addCell(String.valueOf(v.getTotal()));
                }
            }
            doc.add(table);
            doc.close();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("PDF generado en: " + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

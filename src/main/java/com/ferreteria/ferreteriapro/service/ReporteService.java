package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.CierreCajaDAO;
import com.ferreteria.ferreteriapro.dao.EntradaDAO;
import com.ferreteria.ferreteriapro.model.CierreCaja;
import com.ferreteria.ferreteriapro.model.EntradaInventario;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ReporteService {
    private final CierreCajaDAO cierreCajaDAO = new CierreCajaDAO();
    private final EntradaDAO entradaDAO = new EntradaDAO();

    public String procesarCierreMensual() throws Exception {
        List<EntradaInventario> entradas = entradaDAO.listarTodo();
        if (entradas.isEmpty()) {
            throw new Exception("No hay registros para archivar este mes.");
        }

        // 1. Generar Reporte PDF
        String fechaActual = java.time.LocalDate.now().toString();
        String proyectoRoot = System.getProperty("user.dir");
        File carpeta = new File(proyectoRoot, "reportes/compras");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File archivoReporte = new File(carpeta, "reporte_compras_" + fechaActual + ".pdf");

        Document document = new Document();
        try (FileOutputStream out = new FileOutputStream(archivoReporte)) {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("REPORTE MENSUAL DE COMPRAS", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Fecha de Cierre: " + fechaActual));
            document.add(new Paragraph(" ")); // Espacio

            // Tabla
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Fecha");
            table.addCell("Código");
            table.addCell("Producto");
            table.addCell("Proveedor");
            table.addCell("Cant.");
            table.addCell("Costo U.");

            double totalInvertido = 0;
            for (EntradaInventario e : entradas) {
                String nombreProd = e.getProductoNombre() != null ? e.getProductoNombre() : "Desconocido";

                table.addCell(e.getFecha());
                table.addCell(e.getProductoCodigo());
                table.addCell(nombreProd);
                table.addCell(e.getProveedor() != null ? e.getProveedor() : "N/A");
                table.addCell(String.valueOf(e.getCantidad()));
                table.addCell(String.format("$%,.0f", e.getCostoUnitario()));
                totalInvertido += (e.getCantidad() * e.getCostoUnitario());
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph total = new Paragraph("TOTAL INVERTIDO EN EL MES: $ " + String.format("%,.0f", totalInvertido),
                    fontTotal);
            document.add(total);

            document.close();
        }

        // 2. Mover a Histórico en DB
        entradaDAO.archivarHistorico(entradas);

        // 3. Limpiar tabla activa
        entradaDAO.limpiarEntradas();

        return archivoReporte.getAbsolutePath();
    }

    public void registrarCierreCaja(CierreCaja c) throws Exception {
        cierreCajaDAO.guardar(c);
    }

    public List<CierreCaja> obtenerCierres() throws Exception {
        return cierreCajaDAO.listarTodo();
    }

    public CierreCaja obtenerUltimoCierre() throws Exception {
        return cierreCajaDAO.obtenerUltimoCierre();
    }

    public void abrirTurno(double baseInicial) throws Exception {
        cierreCajaDAO.abrirTurno(baseInicial);
    }
}

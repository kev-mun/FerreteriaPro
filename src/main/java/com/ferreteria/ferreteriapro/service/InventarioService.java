package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.ProductoDAO;
import com.ferreteria.ferreteriapro.dao.VentaDAO;
import com.ferreteria.ferreteriapro.dao.EntradaDAO;
import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.model.EntradaInventario;
import com.ferreteria.ferreteriapro.dao.ProveedorDAO;
import com.ferreteria.ferreteriapro.model.Proveedor;
import com.ferreteria.ferreteriapro.dao.CierreCajaDAO;
import com.ferreteria.ferreteriapro.model.CierreCaja;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class InventarioService {
    private ProductoDAO productoDAO = new ProductoDAO();
    private VentaDAO ventaDAO = new VentaDAO();
    private EntradaDAO entradaDAO = new EntradaDAO();
    private ProveedorDAO proveedorDAO = new ProveedorDAO();
    private CierreCajaDAO cierreCajaDAO = new CierreCajaDAO();

    public void registrarEntradaInventario(EntradaInventario e, double nuevoPrecio, boolean actualizarPrecio)
            throws Exception {
        entradaDAO.guardar(e);

        Producto p = productoDAO.listarTodo().stream()
                .filter(prod -> prod.getCodigo().equals(e.getProductoCodigo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Producto no encontrado"));

        p.setStock(p.getStock() + e.getCantidad());
        if (actualizarPrecio) {
            p.setPrecioVenta(nuevoPrecio);
        }
        productoDAO.actualizar(p);
    }

    public void editarEntradaInventario(EntradaInventario nueva, int cantidadAnterior) throws Exception {
        entradaDAO.actualizar(nueva);

        Producto p = productoDAO.listarTodo().stream()
                .filter(prod -> prod.getCodigo().equals(nueva.getProductoCodigo()))
                .findFirst()
                .orElseThrow(() -> new Exception("Producto no encontrado"));

        // Ajustar stock: Revertir la cantidad anterior y aplicar la nueva
        int diferencia = nueva.getCantidad() - cantidadAnterior;
        p.setStock(p.getStock() + diferencia);
        productoDAO.actualizar(p);
    }

    public List<EntradaInventario> obtenerEntradas() throws Exception {
        return entradaDAO.listarTodo();
    }

    public void registrarVenta(Venta v) throws Exception {
        ventaDAO.guardar(v);
    }

    public List<Venta> obtenerVentas() throws Exception {
        return ventaDAO.listarTodo();
    }

    public List<Venta> obtenerVentasPorMes(String mesAno) throws Exception {
        return ventaDAO.obtenerVentasPorMes(mesAno);
    }

    public List<Producto> obtenerProductos() throws Exception {
        return productoDAO.listarTodo();
    }

    public String generarSiguienteCodigo() throws Exception {
        int ultimoId = productoDAO.obtenerUltimoCodigoNumeric();
        return String.format("ART-%03d", ultimoId + 1);
    }

    public void registrarProducto(Producto p) throws Exception {
        validarProducto(p);
        productoDAO.guardar(p);
    }

    public void editarProducto(Producto p) throws Exception {
        validarProducto(p);
        productoDAO.actualizar(p);
    }

    public void eliminarProducto(String codigo) throws Exception {
        productoDAO.eliminar(codigo);
    }

    public List<Proveedor> obtenerProveedores() throws Exception {
        return proveedorDAO.listarTodo();
    }

    public void registrarProveedor(Proveedor p) throws Exception {
        proveedorDAO.guardar(p);
    }

    public void editarProveedor(Proveedor p) throws Exception {
        proveedorDAO.actualizar(p);
    }

    public void eliminarProveedor(int id) throws Exception {
        proveedorDAO.eliminar(id);
    }

    private void validarProducto(Producto p) throws Exception {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre es obligatorio.");
        }
        if (p.getPrecioVenta() <= 0) {
            throw new Exception("El precio debe ser mayor a 0.");
        }
    }

    public String procesarCierreMensual() throws Exception {
        List<EntradaInventario> entradas = entradaDAO.listarTodo();
        if (entradas.isEmpty()) {
            throw new Exception("No hay registros para archivar este mes.");
        }

        // 1. Generar Reporte PDF
        String fechaActual = java.time.LocalDate.now().toString();
        File carpeta = new File("reportes/compras");
        if (!carpeta.exists())
            carpeta.mkdirs();

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

    public void archivarVentasYReiniciar(List<Venta> ventas) throws Exception {
        ventaDAO.archivarVentas(ventas);
        ventaDAO.limpiarVentas();
    }

    public List<CierreCaja> obtenerCierres() throws Exception {
        return cierreCajaDAO.listarTodo();
    }

    public boolean revertirVenta(Venta v) throws Exception {
        return ventaDAO.revertirVenta(v);
    }
}

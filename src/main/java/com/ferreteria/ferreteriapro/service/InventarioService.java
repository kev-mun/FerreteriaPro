package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.ProductoDAO;
import com.ferreteria.ferreteriapro.dao.EntradaDAO;
import com.ferreteria.ferreteriapro.dao.ProveedorDAO;
import com.ferreteria.ferreteriapro.model.Producto;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.model.EntradaInventario;
import com.ferreteria.ferreteriapro.model.Proveedor;
import com.ferreteria.ferreteriapro.model.CierreCaja;
import com.ferreteria.ferreteriapro.model.Cliente;
import com.ferreteria.ferreteriapro.model.Abono;

import java.util.List;

public class InventarioService {
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final EntradaDAO entradaDAO = new EntradaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    // Delegación a servicios especializados
    private final VentaService ventaService = new VentaService();
    private final ReporteService reporteService = new ReporteService();

    // --- LOGICA DE INVENTARIO Y CATALOGO ---

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

    // --- DELEGADOS A VENTASERVICE ---

    public void registrarVenta(Venta v) throws Exception {
        ventaService.registrarVenta(v);
    }

    public List<Venta> obtenerVentas() throws Exception {
        return ventaService.obtenerVentas();
    }

    public List<Venta> obtenerVentasPorMes(String mesAno) throws Exception {
        return ventaService.obtenerVentasPorMes(mesAno);
    }

    public boolean revertirVenta(Venta v) throws Exception {
        return ventaService.revertirVenta(v);
    }

    public void archivarVentasYReiniciar(List<Venta> ventas) throws Exception {
        ventaService.archivarVentasYReiniciar(ventas);
    }

    public void registrarCliente(Cliente c) throws Exception {
        ventaService.registrarCliente(c);
    }

    public void editarCliente(Cliente c) throws Exception {
        ventaService.editarCliente(c);
    }

    public List<Cliente> obtenerClientes() throws Exception {
        return ventaService.obtenerClientes();
    }

    public List<Cliente> buscarClientes(String termino) throws Exception {
        return ventaService.buscarClientes(termino);
    }

    public void registrarAbono(Abono a) throws Exception {
        ventaService.registrarAbono(a);
    }

    public List<Abono> obtenerAbonosPorCliente(int clienteId) throws Exception {
        return ventaService.obtenerAbonosPorCliente(clienteId);
    }

    public List<Abono> obtenerAbonosPorFecha(String fechaInicio, String fechaFin) throws Exception {
        return ventaService.obtenerAbonosPorFecha(fechaInicio, fechaFin);
    }

    // --- DELEGADOS A REPORTESERVICE ---

    public String procesarCierreMensual() throws Exception {
        return reporteService.procesarCierreMensual();
    }

    public void registrarCierreCaja(CierreCaja c) throws Exception {
        reporteService.registrarCierreCaja(c);
    }

    public List<CierreCaja> obtenerCierres() throws Exception {
        return reporteService.obtenerCierres();
    }

    public CierreCaja obtenerUltimoCierre() throws Exception {
        return reporteService.obtenerUltimoCierre();
    }

    public void abrirTurno(double baseInicial) throws Exception {
        reporteService.abrirTurno(baseInicial);
    }
}

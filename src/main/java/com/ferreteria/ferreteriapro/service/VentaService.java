package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.VentaDAO;
import com.ferreteria.ferreteriapro.dao.ClienteDAO;
import com.ferreteria.ferreteriapro.dao.AbonoDAO;
import com.ferreteria.ferreteriapro.model.Venta;
import com.ferreteria.ferreteriapro.model.Cliente;
import com.ferreteria.ferreteriapro.model.Abono;

import java.util.List;

public class VentaService {
    private final VentaDAO ventaDAO = new VentaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final AbonoDAO abonoDAO = new AbonoDAO();

    public void registrarVenta(Venta v) throws Exception {
        ventaDAO.guardar(v);
        // Sumar al saldo del cliente si es a crédito
        if ("Crédito".equalsIgnoreCase(v.getMetodoPago()) && v.getClienteId() != null) {
            clienteDAO.actualizarSaldo(v.getClienteId(), v.getTotal());
        }
    }

    public List<Venta> obtenerVentas() throws Exception {
        return ventaDAO.listarTodo();
    }

    public List<Venta> obtenerVentasPorMes(String mesAno) throws Exception {
        return ventaDAO.obtenerVentasPorMes(mesAno);
    }

    public boolean revertirVenta(Venta v) throws Exception {
        return ventaDAO.revertirVenta(v);
    }

    public void archivarVentasYReiniciar(List<Venta> ventas) throws Exception {
        ventaDAO.archivarVentas(ventas);
        ventaDAO.limpiarVentas();
    }

    // --- GESTIÓN DE CARTERA (CLIENTES Y ABONOS) ---

    public void registrarCliente(Cliente c) throws Exception {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre del cliente es obligatorio.");
        }
        clienteDAO.insertar(c);
    }

    public void editarCliente(Cliente c) throws Exception {
        if (c.getNombre() == null || c.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre del cliente es obligatorio.");
        }
        clienteDAO.actualizar(c);
    }

    public List<Cliente> obtenerClientes() throws Exception {
        return clienteDAO.listarTodo();
    }

    public List<Cliente> buscarClientes(String termino) throws Exception {
        return clienteDAO.buscar(termino);
    }

    public void registrarAbono(Abono a) throws Exception {
        if (a.getMonto() <= 0) {
            throw new Exception("El abono debe ser mayor a 0.");
        }
        abonoDAO.insertar(a);
        clienteDAO.actualizarSaldo(a.getClienteId(), -a.getMonto());
    }

    public List<Abono> obtenerAbonosPorCliente(int clienteId) throws Exception {
        return abonoDAO.listarPorCliente(clienteId);
    }

    public List<Abono> obtenerAbonosPorFecha(String fechaInicio, String fechaFin) throws Exception {
        return abonoDAO.listarPorFecha(fechaInicio, fechaFin);
    }
}

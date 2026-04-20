package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.ProductoDAO;
import com.ferreteria.ferreteriapro.model.Producto;
import java.util.List;

public class InventarioService {
    private ProductoDAO productoDAO = new ProductoDAO();

    public List<Producto> obtenerProductos() throws Exception {
        return productoDAO.listarTodo();
    }

    public String generarSiguienteCodigo() throws Exception {
        List<Producto> productos = productoDAO.listarTodo();
        if (productos.isEmpty()) {
            return "ART-001";
        }

        // Extraer el número más alto de los códigos existentes (ej: ART-010 -> 10)
        int maxId = productos.stream()
                .map(p -> p.getCodigo().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        return String.format("ART-%03d", maxId + 1);
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

    private void validarProducto(Producto p) throws Exception {
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre es obligatorio.");
        }
        if (p.getPrecioVenta() <= 0) {
            throw new Exception("El precio debe ser mayor a 0.");
        }
    }
}
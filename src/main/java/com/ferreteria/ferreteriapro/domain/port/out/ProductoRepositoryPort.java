package com.ferreteria.ferreteriapro.domain.port.out;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Puerto de salida para operaciones de lectura/actualización de stock de productos.
 * Permite que el servicio de consumos internos acceda al stock
 * sin depender directamente de ProductoDAO ni de la capa de persistencia.
 */
public interface ProductoRepositoryPort {

    /**
     * Obtiene el stock actual de un producto por su código.
     *
     * @param conn   conexión JDBC activa
     * @param codigo código del producto
     * @return stock actual del producto
     * @throws SQLException si el producto no existe o hay error de BD
     */
    int obtenerStock(Connection conn, String codigo) throws SQLException;

    /**
     * Actualiza el stock de un producto por su código.
     *
     * @param conn      conexión JDBC activa
     * @param codigo    código del producto
     * @param nuevoStock nuevo valor de stock
     * @throws SQLException si hay error de actualización
     */
    void actualizarStock(Connection conn, String codigo, int nuevoStock) throws SQLException;
}

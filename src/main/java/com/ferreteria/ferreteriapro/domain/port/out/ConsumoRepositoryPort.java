package com.ferreteria.ferreteriapro.domain.port.out;

import com.ferreteria.ferreteriapro.domain.model.ConsumoInterno;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Puerto de salida del dominio.
 * Define el contrato que debe cumplir el adaptador de persistencia
 * para almacenar y consultar consumos internos.
 */
public interface ConsumoRepositoryPort {

    /**
     * Inserta un registro de consumo interno usando la conexión proporcionada.
     * La conexión se maneja externamente para permitir transacciones coordinadas.
     *
     * @param conn    conexión JDBC activa (gestionada por el llamador)
     * @param consumo datos del consumo a persistir
     * @throws SQLException si hay error de inserción
     */
    void guardar(Connection conn, ConsumoInterno consumo) throws SQLException;

    /**
     * Lista todos los consumos internos con el nombre del producto resuelto.
     *
     * @return lista de consumos ordenada por fecha descendente
     * @throws SQLException si hay error de consulta
     */
    List<ConsumoInterno> listarTodo() throws SQLException;

    /**
     * Lista consumos internos filtrados por rango de fechas.
     *
     * @param fechaInicio fecha inicio (yyyy-MM-dd)
     * @param fechaFin    fecha fin (yyyy-MM-dd)
     * @return lista filtrada de consumos
     * @throws SQLException si hay error de consulta
     */
    List<ConsumoInterno> listarPorFecha(String fechaInicio, String fechaFin) throws SQLException;
}

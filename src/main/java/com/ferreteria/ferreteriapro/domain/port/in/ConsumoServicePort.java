package com.ferreteria.ferreteriapro.domain.port.in;

import com.ferreteria.ferreteriapro.domain.model.ConsumoInterno;
import java.util.List;

/**
 * Puerto de entrada del dominio.
 * Define las operaciones que la capa de aplicación (controladores, UI)
 * puede solicitar al servicio de consumos internos.
 */
public interface ConsumoServicePort {

    /**
     * Registra un consumo interno descontando stock de forma transaccional.
     *
     * @param consumo el consumo a registrar (sin id, se asigna automáticamente)
     * @throws Exception si el producto no existe, stock insuficiente o error de BD
     */
    void registrarConsumo(ConsumoInterno consumo) throws Exception;

    /**
     * Lista todos los consumos internos registrados, ordenados por fecha descendente.
     *
     * @return lista de consumos con nombre de producto resuelto
     * @throws Exception si hay error de BD
     */
    List<ConsumoInterno> listarConsumos() throws Exception;

    /**
     * Lista consumos filtrados por un rango de fechas (formato yyyy-MM-dd).
     *
     * @param fechaInicio fecha de inicio inclusive
     * @param fechaFin    fecha de fin inclusive
     * @return lista filtrada de consumos
     * @throws Exception si hay error de BD
     */
    List<ConsumoInterno> listarConsumosPorFecha(String fechaInicio, String fechaFin) throws Exception;
}

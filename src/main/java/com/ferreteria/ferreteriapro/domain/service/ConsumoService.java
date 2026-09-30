package com.ferreteria.ferreteriapro.domain.service;

import com.ferreteria.ferreteriapro.DatabaseConnection;
import com.ferreteria.ferreteriapro.domain.model.ConsumoInterno;
import com.ferreteria.ferreteriapro.domain.port.in.ConsumoServicePort;
import com.ferreteria.ferreteriapro.domain.port.out.ConsumoRepositoryPort;
import com.ferreteria.ferreteriapro.domain.port.out.ProductoRepositoryPort;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de dominio que implementa la lógica de negocio para consumos internos.
 * Coordina la transacción: descuenta stock + registra el consumo de forma atómica.
 *
 * Solo depende de interfaces (puertos), nunca de implementaciones concretas.
 */
public class ConsumoService implements ConsumoServicePort {

    private final ConsumoRepositoryPort consumoRepository;
    private final ProductoRepositoryPort productoRepository;

    public ConsumoService(ConsumoRepositoryPort consumoRepository,
                          ProductoRepositoryPort productoRepository) {
        this.consumoRepository = consumoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void registrarConsumo(ConsumoInterno consumo) throws Exception {
        // Validaciones de dominio
        if (consumo.getProductoCodigo() == null || consumo.getProductoCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (consumo.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");
        }
        if (consumo.getMotivo() == null || consumo.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo del consumo es obligatorio.");
        }

        // Transacción: descontar stock + insertar registro
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Verificar stock disponible
            int stockActual = productoRepository.obtenerStock(conn, consumo.getProductoCodigo());
            if (stockActual < consumo.getCantidad()) {
                throw new Exception("Stock insuficiente. Disponible: " + stockActual
                        + ", Solicitado: " + consumo.getCantidad());
            }

            // 2. Descontar stock
            int nuevoStock = stockActual - consumo.getCantidad();
            productoRepository.actualizarStock(conn, consumo.getProductoCodigo(), nuevoStock);

            // 3. Registrar el consumo interno
            consumoRepository.guardar(conn, consumo);

            conn.commit();
            System.out.println("✅ Consumo interno registrado correctamente. Nuevo stock: " + nuevoStock);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Error en rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("❌ Error al cerrar conexión: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public List<ConsumoInterno> listarConsumos() throws Exception {
        return consumoRepository.listarTodo();
    }

    @Override
    public List<ConsumoInterno> listarConsumosPorFecha(String fechaInicio, String fechaFin) throws Exception {
        return consumoRepository.listarPorFecha(fechaInicio, fechaFin);
    }
}

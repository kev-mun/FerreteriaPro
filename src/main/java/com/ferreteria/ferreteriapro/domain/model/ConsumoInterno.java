package com.ferreteria.ferreteriapro.domain.model;

/**
 * Modelo de dominio que representa una salida de inventario
 * por consumo interno (uso propio, muestras, daños, etc.).
 * Es independiente de cualquier framework o persistencia.
 */
public class ConsumoInterno {

    private int id;
    private String productoCodigo;
    private String productoNombre; // Campo de lectura (no persistido en consumos_internos)
    private int cantidad;
    private String motivo;
    private String usuarioId;
    private String fecha;

    /** Constructor completo (para lecturas desde BD) */
    public ConsumoInterno(int id, String productoCodigo, String productoNombre,
                          int cantidad, String motivo, String usuarioId, String fecha) {
        this.id = id;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
    }

    /** Constructor para registro nuevo (sin id ni nombre de producto) */
    public ConsumoInterno(String productoCodigo, int cantidad, String motivo, String usuarioId) {
        this.productoCodigo = productoCodigo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.usuarioId = usuarioId;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getProductoCodigo() { return productoCodigo; }
    public String getProductoNombre() { return productoNombre; }
    public int getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public String getUsuarioId() { return usuarioId; }
    public String getFecha() { return fecha; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    @Override
    public String toString() {
        return "ConsumoInterno{" +
                "id=" + id +
                ", productoCodigo='" + productoCodigo + '\'' +
                ", cantidad=" + cantidad +
                ", motivo='" + motivo + '\'' +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}

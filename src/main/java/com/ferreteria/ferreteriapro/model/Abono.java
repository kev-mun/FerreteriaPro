package com.ferreteria.ferreteriapro.model;

public class Abono {
    private int id;
    private int clienteId;
    private double monto;
    private String fecha;
    private String metodoPago;
    private String usuarioNombre;

    public Abono(int id, int clienteId, double monto, String fecha, String metodoPago, String usuarioNombre) {
        this.id = id;
        this.clienteId = clienteId;
        this.monto = monto;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
        this.usuarioNombre = usuarioNombre;
    }

    public Abono(int clienteId, double monto, String fecha, String metodoPago, String usuarioNombre) {
        this(0, clienteId, monto, fecha, metodoPago, usuarioNombre);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
}

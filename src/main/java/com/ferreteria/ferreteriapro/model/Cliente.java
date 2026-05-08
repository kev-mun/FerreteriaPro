package com.ferreteria.ferreteriapro.model;

public class Cliente {
    private int id;
    private String documento;
    private String nombre;
    private String telefono;
    private double saldoPendiente;

    public Cliente(int id, String documento, String nombre, String telefono, double saldoPendiente) {
        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
        this.saldoPendiente = saldoPendiente;
    }

    public Cliente(String documento, String nombre, String telefono) {
        this(0, documento, nombre, telefono, 0.0);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public double getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(double saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    @Override
    public String toString() {
        return nombre + (documento != null && !documento.isEmpty() ? " - " + documento : "");
    }
}

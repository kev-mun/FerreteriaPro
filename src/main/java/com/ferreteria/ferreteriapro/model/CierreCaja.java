package com.ferreteria.ferreteriapro.model;

public class CierreCaja {
    private int id;
    private String fecha;
    private double totalVentas;
    private double totalCostos;
    private double ganancia;
    private double efectivo;
    private double transferencia;
    private String estado;
    private double baseInicial;
    private double baseSiguiente;

    // Empty constructor
    public CierreCaja() {
    }

    // Constructor without id (for insert)
    public CierreCaja(String fecha, double totalVentas, double totalCostos, double ganancia,
            double efectivo, double transferencia, String estado,
            double baseInicial, double baseSiguiente) {
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.totalCostos = totalCostos;
        this.ganancia = ganancia;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
        this.estado = estado;
        this.baseInicial = baseInicial;
        this.baseSiguiente = baseSiguiente;
    }

    // Constructor with id (for queries)
    public CierreCaja(int id, String fecha, double totalVentas, double totalCostos, double ganancia,
            double efectivo, double transferencia, String estado,
            double baseInicial, double baseSiguiente) {
        this.id = id;
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.totalCostos = totalCostos;
        this.ganancia = ganancia;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
        this.estado = estado;
        this.baseInicial = baseInicial;
        this.baseSiguiente = baseSiguiente;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public double getTotalCostos() {
        return totalCostos;
    }

    public void setTotalCostos(double totalCostos) {
        this.totalCostos = totalCostos;
    }

    public double getGanancia() {
        return ganancia;
    }

    public void setGanancia(double ganancia) {
        this.ganancia = ganancia;
    }

    public double getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(double efectivo) {
        this.efectivo = efectivo;
    }

    public double getTransferencia() {
        return transferencia;
    }

    public void setTransferencia(double transferencia) {
        this.transferencia = transferencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getBaseInicial() {
        return baseInicial;
    }

    public void setBaseInicial(double baseInicial) {
        this.baseInicial = baseInicial;
    }

    public double getBaseSiguiente() {
        return baseSiguiente;
    }

    public void setBaseSiguiente(double baseSiguiente) {
        this.baseSiguiente = baseSiguiente;
    }
}

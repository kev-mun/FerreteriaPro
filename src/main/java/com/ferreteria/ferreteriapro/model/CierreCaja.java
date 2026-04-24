package com.ferreteria.ferreteriapro.model;

public class CierreCaja {
    private int id;
    private String fecha;
    private double totalVentas;
    private double totalCostos;
    private double ganancia;
    private double efectivo;
    private double transferencia;

    public CierreCaja(int id, String fecha, double totalVentas, double totalCostos, double ganancia, double efectivo, double transferencia) {
        this.id = id;
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.totalCostos = totalCostos;
        this.ganancia = ganancia;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
    }

    public CierreCaja(String fecha, double totalVentas, double totalCostos, double ganancia, double efectivo, double transferencia) {
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.totalCostos = totalCostos;
        this.ganancia = ganancia;
        this.efectivo = efectivo;
        this.transferencia = transferencia;
    }

    // Getters
    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public double getTotalVentas() { return totalVentas; }
    public double getTotalCostos() { return totalCostos; }
    public double getGanancia() { return ganancia; }
    public double getEfectivo() { return efectivo; }
    public double getTransferencia() { return transferencia; }
}

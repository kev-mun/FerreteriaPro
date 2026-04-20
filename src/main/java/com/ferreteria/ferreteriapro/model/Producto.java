package com.ferreteria.ferreteriapro.model;

public class Producto {
    private String codigo;
    private String nombre;
    private double precioVenta;
    private int stock;

    public Producto(String codigo, String nombre, double precioVenta, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.stock = stock;
    }

    // Getters: Indispensables para que la tabla sea visible
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecioVenta() { return precioVenta; }
    public int getStock() { return stock; }

    // Setters: Esto corrige los errores "cannot find symbol"
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
package com.ferreteria.ferreteriapro.model;

public class Producto {
    private String codigo;
    private String nombre;
    private double precioCompra;
    private double precioVenta;
    private int stock;
    private String proveedorNombre;

    public Producto(String codigo, String nombre, double precioCompra, double precioVenta, int stock, String proveedorNombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.proveedorNombre = proveedorNombre;
    }

    public Producto(String codigo, String nombre, double precioCompra, double precioVenta, int stock) {
        this(codigo, nombre, precioCompra, precioVenta, stock, "Genérico");
    }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public Producto(String codigo, String nombre, double precioVenta, int stock) {
        this(codigo, nombre, 0, precioVenta, stock);
    }

    // Getters: Indispensables para que la tabla sea visible
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecioCompra() { return precioCompra; }
    public double getPrecioVenta() { return precioVenta; }
    public int getStock() { return stock; }

    // Setters: Esto corrige los errores "cannot find symbol"
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }
}
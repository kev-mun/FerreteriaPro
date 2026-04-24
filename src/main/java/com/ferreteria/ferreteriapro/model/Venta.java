package com.ferreteria.ferreteriapro.model;

public class Venta {
    private int id;
    private String fecha;
    private String productoCodigo;
    private String productoNombre;
    private int cantidad;
    private double total;
    private String metodoPago;
    private double costoUnitario;
    private String usuarioNombre;

    public Venta(int id, String fecha, String productoCodigo, String productoNombre, int cantidad, double total, String metodoPago, double costoUnitario, String usuarioNombre) {
        this.id = id;
        this.fecha = fecha;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.total = total;
        this.metodoPago = metodoPago;
        this.costoUnitario = costoUnitario;
        this.usuarioNombre = usuarioNombre;
    }

    public Venta(String fecha, String productoCodigo, String productoNombre, int cantidad, double total, String metodoPago, double costoUnitario, String usuarioNombre) {
        this.fecha = fecha;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.total = total;
        this.metodoPago = metodoPago;
        this.costoUnitario = costoUnitario;
        this.usuarioNombre = usuarioNombre;
    }

    public String getProductoNombre() { return productoNombre; }
    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getProductoCodigo() { return productoCodigo; }
    public int getCantidad() { return cantidad; }
    public double getTotal() { return total; }
    public String getMetodoPago() { return metodoPago; }
    public double getCostoUnitario() { return costoUnitario; }
    public String getUsuarioNombre() { return usuarioNombre; }
}

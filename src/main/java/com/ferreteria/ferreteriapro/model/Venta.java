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

    private String estado;

    public Venta(int id, String fecha, String productoCodigo, String productoNombre, int cantidad, double total, String metodoPago, double costoUnitario, String usuarioNombre, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.total = total;
        this.metodoPago = metodoPago;
        this.costoUnitario = costoUnitario;
        this.usuarioNombre = usuarioNombre;
        this.estado = estado;
    }

    public Venta(int id, String fecha, String productoCodigo, String productoNombre, int cantidad, double total, String metodoPago, double costoUnitario, String usuarioNombre) {
        this(id, fecha, productoCodigo, productoNombre, cantidad, total, metodoPago, costoUnitario, usuarioNombre, "ACTIVA");
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
        this.estado = "ACTIVA";
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
    public String getEstado() { return estado; }
}

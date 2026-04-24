package com.ferreteria.ferreteriapro.model;

public class EntradaInventario {
    private int id;
    private String productoCodigo;
    private String productoNombre;
    private int cantidad;
    private double costoUnitario;
    private String fecha;
    private String proveedor;
    private String usuarioNombre;

    public EntradaInventario(int id, String productoCodigo, String productoNombre, int cantidad, double costoUnitario, String fecha, String proveedor, String usuarioNombre) {
        this.id = id;
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.usuarioNombre = usuarioNombre;
    }

    public EntradaInventario(String productoCodigo, int cantidad, double costoUnitario, String fecha, String proveedor, String usuarioNombre) {
        this.productoCodigo = productoCodigo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.usuarioNombre = usuarioNombre;
    }

    public EntradaInventario(int id, String productoCodigo, int cantidad, double costoUnitario, String fecha, String proveedor, String usuarioNombre) {
        this.id = id;
        this.productoCodigo = productoCodigo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.usuarioNombre = usuarioNombre;
    }

    public int getId() { return id; }
    public String getProductoCodigo() { return productoCodigo; }
    public String getProductoNombre() { return productoNombre; }
    public int getCantidad() { return cantidad; }
    public double getCostoUnitario() { return costoUnitario; }
    public String getFecha() { return fecha; }
    public String getProveedor() { return proveedor; }
    public String getUsuarioNombre() { return usuarioNombre; }
    
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
}

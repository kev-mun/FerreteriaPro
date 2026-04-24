package com.ferreteria.ferreteriapro.model;

public class Proveedor {
    private int id;
    private String nombre;

    public Proveedor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Proveedor(String nombre) {
        this(0, nombre);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}

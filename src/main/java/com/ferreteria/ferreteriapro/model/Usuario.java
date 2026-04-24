package com.ferreteria.ferreteriapro.model;

public class Usuario {
    private int id;
    private String usuario;
    private String passwordHash;
    private String nombre;
    private String rol;

    public Usuario(int id, String usuario, String passwordHash, String nombre, String rol) {
        this.id = id;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}

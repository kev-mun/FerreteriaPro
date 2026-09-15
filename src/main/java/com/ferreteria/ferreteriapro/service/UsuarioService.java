package com.ferreteria.ferreteriapro.service;

import com.ferreteria.ferreteriapro.dao.UsuarioDAO;
import com.ferreteria.ferreteriapro.model.Usuario;
import java.util.List;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario login(String username, String password) throws Exception {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.login(username, password);
    }

    public List<Usuario> obtenerUsuarios() throws Exception {
        return usuarioDAO.listarTodo();
    }

    public void registrarUsuario(Usuario u, String passwordPlana) throws Exception {
        if (u.getUsuario() == null || u.getUsuario().trim().isEmpty()) {
            throw new Exception("El login de usuario es obligatorio.");
        }
        if (passwordPlana == null || passwordPlana.trim().isEmpty()) {
            throw new Exception("La contraseña es obligatoria.");
        }
        usuarioDAO.guardar(u, passwordPlana);
    }

    public void editarUsuario(Usuario u, String nuevaPasswordPlana) throws Exception {
        if (u.getUsuario() == null || u.getUsuario().trim().isEmpty()) {
            throw new Exception("El login de usuario es obligatorio.");
        }
        usuarioDAO.actualizar(u, nuevaPasswordPlana);
    }

    public void eliminarUsuario(int id) throws Exception {
        usuarioDAO.eliminar(id);
    }
}

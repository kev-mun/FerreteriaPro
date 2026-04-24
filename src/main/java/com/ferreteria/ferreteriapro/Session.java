package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.Usuario;

public class Session {
    private static Usuario currentUser;

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Usuario user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }
}

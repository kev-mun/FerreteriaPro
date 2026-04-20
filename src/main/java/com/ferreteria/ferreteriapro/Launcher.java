package com.ferreteria.ferreteriapro;

import javafx.application.Application;

class Launcher {
    // En Java 26, el 'public' y 'static' pueden ser opcionales,
    // pero mantenerlo así asegura compatibilidad total.
    static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
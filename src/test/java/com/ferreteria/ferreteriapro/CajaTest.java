package com.ferreteria.ferreteriapro;

import com.ferreteria.ferreteriapro.model.CierreCaja;
import com.ferreteria.ferreteriapro.model.Usuario;
import com.ferreteria.ferreteriapro.service.InventarioService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CajaTest {

    @Test
    public void testPersistenciaCajaTrasLogout() throws Exception {
        DatabaseConnection.inicializarBaseDeDatos();
        InventarioService service = new InventarioService();

        // 1. Simular apertura de turno con base inicial
        double basePrueba = 35000.0;
        service.abrirTurno(basePrueba);

        // 2. Verificar que se abrió correctamente
        CierreCaja turno = service.obtenerTurnoAbierto();
        assertNotNull(turno, "El turno debe encontrarse activo");
        assertEquals("ABIERTO", turno.getEstado());
        assertEquals(basePrueba, turno.getBaseInicial(), 0.001);

        // 3. Simular que el usuario hace logout
        Session.logout();
        assertNull(Session.getCurrentUser());

        // 4. Simular que el usuario vuelve a iniciar sesión (nuevo ciclo de vida)
        Usuario usuarioReingreso = new Usuario(1, "admin", "admin", "Administrador", "Administrador");
        Session.setCurrentUser(usuarioReingreso);

        // 5. La verificación en BD al login debe encontrar la caja abierta
        CierreCaja turnoAlLogin = service.obtenerTurnoAbierto();
        assertNotNull(turnoAlLogin, "La caja debe seguir abierta en la base de datos");
        assertEquals("ABIERTO", turnoAlLogin.getEstado());
        assertEquals(basePrueba, turnoAlLogin.getBaseInicial(), 0.001);

        System.out.println("✅ Verificación completada: la caja persiste abierta entre sesiones de usuario.");
    }
}

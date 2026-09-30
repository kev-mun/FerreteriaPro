# 🛠️ Ferretería Pro - Enterprise POS & Sistema de Gestión de Inventario

**Ferretería Pro Enterprise** es una solución integral de software de escritorio desarrollada en **Java 21/26** y **JavaFX 21**, diseñada para optimizar los procesos comerciales, financieros y logísticos de ferreterías, depósitos y almacenes de distribución.

---

## ✨ Módulos y Características Principales

### 📦 1. Gestión de Inventario & Compras
- **Cálculo Automático de Precios:** Determina precios de venta finales basados en costo unitario, margen de ganancia porcentual e impuestos (IVA).
- **Semáforo y Alerta de Stock Crítico:** Pestaña inteligente de "Stock Bajo" que filtra automáticamente productos por debajo del umbral mínimo y genera órdenes de compra sugeridas.
- **Entradas de Mercancía:** Registro histórico de adquisiciones enlazadas a proveedores específicos con trazabilidad de costos.
- **Auditoría de Consumos Internos:** Módulo desacoplado bajo **Arquitectura Hexagonal** (`domain/port/service/adapter`) para dar salida a productos por uso propio, muestras comerciales o averías sin alterar el flujo de caja.

### 💰 2. Punto de Venta (POS) & Ventana Flotante
- **Buscador Predictivo:** Filtrado en tiempo real con selector de productos de alta velocidad.
- **Control de Cartera y Ventas a Crédito:** Administración de deudas de clientes y emisión de recibos de abono en PDF vectorial (**OpenPDF**).
- **Comprobantes y Notas de Crédito:** Generación instantánea de tickets de venta y comprobantes de devolución con reversión automática de existencias.
- **Ventana Flotante de Venta Rápida (`VentaFlotanteController`):** Modal desacoplado para despachos ágiles sin salir de la vista general.

### 💵 3. Arqueo y Cierre de Caja
- **Apertura de Turno:** Definición de la base inicial de caja.
- **Balance Dual:** Comparativa entre ventas registradas por método de pago (Efectivo, Transferencias, Tarjetas) vs. efectivo físico reportado.
- **Trazabilidad y Reporte de Descuadres:** Detección de sobrantes o faltantes con generación de balance impreso y resguardo del histórico.

### 🔄 4. Sistema Híbrido de Copias de Seguridad (`BackupService`)
- **Ejecución Asíncrona:** El respaldo se ejecuta en un hilo en segundo plano (`Thread`) para evitar congelamiento de la interfaz gráfica.
- **Doble Destino (Nube + USB):**
  - **Copia Local/Drive:** Respaldada automáticamente en `%APPDATA%\FerreteriaPro\backups\drive\`.
  - **Copia Externa USB:** Detecta la unidad conectada (`E:\BackupsFerreteria`) y deposita una copia idéntica.
  - **Contingencia Automática:** Si la memoria USB no está conectada, el sistema deposita la copia en `%APPDATA%\FerreteriaPro\backups\pendientes_usb\` y emite una alerta preventiva.

### 🔐 5. Seguridad y Roles
- **Control de Acceso:** Login autenticado con hashes **SHA-256**.
- **Roles:** Separación de privilegios entre **Administrador** (gestión total, inventario, reportes, usuarios) y **Vendedor** (ventas POS, abonos y consultas).
- **Auditoría:** Cada transacción almacena fecha, hora y usuario responsable.

---

## 🗄️ Arquitectura de Almacenamiento Seguro (`AppPaths`)

Para cumplir con las políticas de seguridad de Windows y evitar errores de permisos (`AccessDeniedException`) al instalar el software en `C:\Program Files\FerreteriaPro\`, todos los datos y archivos generados por la aplicación se gestionan dinámicamente en el espacio de usuario:

| Tipo de Recurso | Ruta de Almacenamiento en Windows |
|---|---|
| **Base de Datos SQLite** | `%APPDATA%\FerreteriaPro\database\ferreteria_nueva.db` |
| **Comprobantes y Facturas** | `%APPDATA%\FerreteriaPro\facturas\` (subcarpetas `notas_credito/` y `abonos/`) |
| **Copias de Respaldo** | `%APPDATA%\FerreteriaPro\backups\` (subcarpetas `drive/` y `pendientes_usb/`) |
| **Reportes y Balances** | `%APPDATA%\FerreteriaPro\reportes\` (subcarpetas `cierres_diarios/`, `ordenes/`, `compras/`) |

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 LTS / Java 26
- **Interfaz Gráfica:** JavaFX 21 + FXML + CSS Moderno
- **Componentes Avanzados:** ControlsFX (SearchableComboBox)
- **Base de Datos:** SQLite 3 con modo WAL (`journal_mode = WAL`, `synchronous = NORMAL`, `busy_timeout = 5000`)
- **Documentos & Reportes:** OpenPDF 1.3.30
- **Empaquetador y Build:** Apache Maven + Maven Shade Plugin 3.5.1
- **Instalador Nativo Windows:** JDK `jpackage` + WiX Toolset 3.x

---

## 📂 Estructura del Código Fuente

```text
FerreteriaPro/
├── build_installer.bat      # Script automatizado para compilar y generar instalador .exe
├── pom.xml                  # Configuración de dependencias y plugins Maven
├── icon.png                 # Icono principal de la aplicación (fuente)
├── README.md                # Documentación del proyecto
├── src/
│   ├── main/
│   │   ├── java/com/ferreteria/ferreteriapro/
│   │   │   ├── Launcher.java                 # Punto de entrada principal (Fat JAR / EXE)
│   │   │   ├── HelloApplication.java         # Inicialización del Stage de JavaFX
│   │   │   ├── DatabaseConnection.java       # Pool SQLite, conexión y migraciones DDL
│   │   │   ├── AppPaths.java                 # Gestor centralizado de rutas en %APPDATA%
│   │   │   ├── Session.java                  # Contexto global de sesión y usuario activo
│   │   │   ├── GenerarDocumentacionWord.java # Generador del manual técnico en Word (.docx)
│   │   │   ├── adapter/                      # Adaptadores de persistencia (Arquitectura Hexagonal)
│   │   │   ├── dao/                          # Objetos de Acceso a Datos (JDBC DAO)
│   │   │   ├── domain/                       # Modelos de dominio y puertos de entrada/salida
│   │   │   ├── model/                        # Entidades del negocio (POJOs)
│   │   │   └── service/                      # Servicios de negocio (BackupService, ReporteService, etc.)
│   │   └── resources/com/ferreteria/ferreteriapro/
│   │       ├── hello-view.fxml               # Vista principal modular del ERP/POS
│   │       ├── login-view.fxml               # Formulario de autenticación
│   │       ├── styles.css                    # Hoja de estilos moderna (Dark/Light tokens)
│   │       └── icon.png                      # Asset embebido del icono
```

---

## 🚀 Puesta en Marcha en Desarrollo

1. **Requisitos:**
   - JDK 21 o superior instalado y configurado en `JAVA_HOME`.
   - IDE recomendado: **Antigravity IDE** o **VS Code**.
2. **Ejecutar en modo desarrollo:**
   ```bash
   .\mvnw.cmd javafx:run
   ```
   *Nota: La base de datos SQLite se crea y migra automáticamente en la primera ejecución.*
3. **Credenciales por Defecto:**
   - **Administrador:** `admin` / `admin123`
   - **Vendedor:** `vendedor` / `vendedor123`

---

## 📦 Generación del Instalador Nativo de Windows (`.exe`)

El proyecto incluye un pipeline automatizado en [`build_installer.bat`](file:///c:/Users/tecnico_ti2/Desktop/FerreteriaPro/build_installer.bat) que:
1. Compila y empaqueta el **Fat JAR** con todas las dependencias (`mvnw clean package -DskipTests`).
2. Convierte el icono PNG en un icono nativo de Windows `.ico` (256x256) en tiempo real.
3. Invoca `jpackage` para generar un instalador ejecutable independiente con soporte para:
   - Acceso directo en el **Escritorio** (`--win-shortcut`).
   - Acceso en el **Menú de Inicio** (`--win-menu`).
   - Selector personalizado de carpeta de instalación (`--win-dir-chooser`).

### Ejecución:
```cmd
.\build_installer.bat
```

### Resultado:
El instalador queda listo para distribución en:
```text
target/installer/FerreteriaPro-1.0.0.exe
```

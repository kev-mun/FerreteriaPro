# 🛠️ Ferretería Pro - Sistema de Gestión e Inventario

**Ferretería Pro** es una aplicación de escritorio robusta desarrollada en **JavaFX**, diseñada para automatizar el control de inventario, ventas y administración de usuarios en negocios minoristas.

---

## ✨ Características Principales

### 📦 Gestión de Inventario & Compras

- **Cálculo Automático:** Determina precios de venta finales basados en costo, % de ganancia e IVA.
- **Stock Inteligente:** Pestaña dinámica de "Stock Bajo" que se activa automáticamente cuando los productos alcanzan niveles críticos.
- **Entradas de Mercancía:** Registro histórico de compras vinculadas a proveedores específicos.

### 💰 Punto de Venta (POS)

- **Interfaz Intuitiva:** Registro de ventas rápido con buscador de productos.
- **Facturación:** Generación automática de comprobantes de venta en formato `.txt`.
- **Anulación de Ventas:** Capacidad para revertir transacciones con actualización automática de stock.

### 🔐 Seguridad y Administración

- **Control de Acceso:** Sistema de Login con perfiles de **Administrador** y **Vendedor**.
- **Trazabilidad:** Cada movimiento queda registrado con el nombre del usuario que realizó la acción.
- **Reportes Mensuales:** Cierres de caja y resúmenes de ganancias generados automáticamente.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21
- **Interfaz Gráfica:** JavaFX (MVC)
- **Base de Datos:** SQLite
- **Gestor de Dependencias:** Maven
- **Diseño:** CSS personalizado para una apariencia moderna.

---

## 📂 Estructura del Proyecto

- `src/main/java/com/ferreteria/ferreteriapro/dao`: Clases para la persistencia de datos (CRUD).
- `src/main/java/com/ferreteria/ferreteriapro/model`: Modelos de datos (POJOs).
- `src/main/resources`: Archivos FXML y estilos CSS.
- `facturas/`: Almacén de comprobantes generados.
- `reportes/`: Resúmenes de cierres mensuales y diarios.

---

## ⚙️ Instalación y Uso

1. Clona el repositorio: `git clone https://github.com/kev-mun/FerreteriaPro.git`
2. Abre el proyecto en tu IDE favorito (Recomendado: Antigravity / VS Code).
3. Ejecuta el comando `mvn javafx:run`.

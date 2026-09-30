package com.ferreteria.ferreteriapro;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerarDocumentacionWord {

    public static void main(String[] args) {
        String filename = "FerreteriaPro_Documentacion_Completa.docx";
        try {
            generarDocx(Paths.get(filename));
            System.out.println("✅ Documento Word generado exitosamente: " + Paths.get(filename).toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarDocx(Path targetPath) throws Exception {
        StringBuilder body = new StringBuilder();

        // Header / Cover
        addTitle(body, "MANUAL DE FUNCIONALIDAD Y ARQUITECTURA");
        addSubtitle(body, "Documentación Técnica y Operativa Completa de FerreteríaPro");
        
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        addMetaBox(body, 
            "FerreteríaPro - Sistema Integral de Gestión Ferretera", 
            "1.0-RELEASE (Producción 2026)", 
            fecha, 
            "Java 21/26 + JavaFX 21 + SQLite 3 (WAL Mode) + OpenPDF 1.3 + ControlsFX"
        );

        // 1. Resumen Ejecutivo
        addH1(body, "1. Resumen Ejecutivo y Alcance del Software");
        addParagraph(body, "FerreteríaPro es una solución integral de software de escritorio concebida y desarrollada para optimizar la totalidad de los procesos comerciales, financieros y logísticos de un negocio de ferretería o distribución de materiales. El sistema integra bajo una interfaz unificada la facturación ágil de punto de venta (POS), el control estricto de existencias e inventarios multiescala, la gestión de cartera con ventas a crédito y registro de abonos, el control de turnos y arqueos de caja, la auditoría de consumos internos bajo Arquitectura Hexagonal y una política automatizada de copias de seguridad híbridas (en Google Drive y memorias USB).");
        addParagraph(body, "El motor de datos utiliza SQLite 3 configurado con Write-Ahead Logging (WAL) y temporizadores de concurrencia (busy_timeout = 5000ms), lo cual asegura lecturas y escrituras no bloqueantes, resistencia ante cortes eléctricos inesperados y un rendimiento de respuesta inmediato.");

        // 2. Arquitectura General
        addH1(body, "2. Arquitectura de Software y Stack Tecnológico");
        addParagraph(body, "La solución está diseñada siguiendo principios de modularidad, alta cohesión y bajo acoplamiento:");
        addBullet(body, "Capa de Presentación (UI)", "Construida sobre JavaFX 21 y FXML reactivo, enriquecida con ControlsFX (SearchableComboBox) para búsquedas predictivas instantáneas, diálogos modales nativos y ventanas flotantes desacopladas.");
        addBullet(body, "Capa de Lógica y Servicios (Domain & Application)", "Servicios de negocio especializados (InventarioService, VentaService, ReporteService, UsuarioService, BackupService) que encapsulan las reglas y orquestan los flujos.");
        addBullet(body, "Módulo de Consumos Internos (Arquitectura Hexagonal)", "Implementación estricta con aislamiento total del dominio: ConsumoInterno (Domain Model), ConsumoServicePort (Input Port), ConsumoRepositoryPort y ProductoRepositoryPort (Output Ports), ConsumoService (Domain Service) y ConsumoDaoAdapter / ProductoDaoAdapter (Persistence Adapters).");
        addBullet(body, "Capa de Datos y Persistencia", "Patrón DAO y Adaptadores JDBC directos con transacciones ACID gestionadas explícitamente mediante setAutoCommit(false), commit() y rollback().");
        addBullet(body, "Motor Documental", "Integración con OpenPDF 1.3.30 para maquetación vectorial de facturas, tickets y balances mensuales de compras en formato PDF estándar.");

        // 3. Módulos Funcionales
        addH1(body, "3. Descripción Exhaustiva de Módulos Funcionales");

        addH2(body, "3.1. Seguridad, Autenticación y Control de Roles");
        addParagraph(body, "El sistema protege las operaciones críticas y la confidencialidad financiera del negocio a través de políticas de autenticación y privilegios:");
        addBullet(body, "Inicio de Sesión Blindado", "Validación contra la tabla de usuarios con contraseñas protegidas por función hash SHA-256 unidireccional.");
        addBullet(body, "Control de Sesión Global (Session.java)", "Gestión centralizada del usuario autenticado en el hilo de ejecución para estampar auditoría con nombre y rol en cada transacción.");
        addBullet(body, "Diferenciación de Roles", "El sistema soporta perfiles de 'Administrador' (acceso irrestricto a inventario, compras, edición de precios, creación de usuarios, cierres globales y reportes) y 'Vendedor' (enfocado en POS, registro de ventas, consultas de stock y créditos).");
        addBullet(body, "Restricción Dinámica de Vistas", "Ocultamiento automático de pestañas de configuración sensible según los privilegios del operador conectado.");

        addH2(body, "3.2. Catálogo Maestro e Inventario de Productos");
        addParagraph(body, "Centraliza y automatiza la administración de los artículos comerciales de la ferretería:");
        addBullet(body, "Generador Secuencial de Códigos", "Calcula y sugiere automáticamente el siguiente código numérico disponible con formato estándar (ej: ART-001, ART-002, etc.).");
        addBullet(body, "Calculadora Comercial Integrada", "Asistente en tiempo real que calcula el precio de venta sugerido a partir del costo de compra, el margen de utilidad deseado (%) y el porcentaje de IVA aplicable.");
        addBullet(body, "Buscador Predictivo Multifiltro", "Búsqueda interactiva en tiempo real por coincidencia de texto en código, nombre del producto o proveedor vinculado.");
        addBullet(body, "Baja Lógica (Soft-Delete)", "Mantiene la integridad histórica de ventas pasadas desactivando productos (activo = 0) en lugar de eliminarlos físicamente de la base de datos.");
        addBullet(body, "Módulo de Alerta de Stock Bajo", "Vista especializada que lista los productos cuyas existencias han alcanzado niveles críticos para tramitar compras de reposición prioritarias.");

        addH2(body, "3.3. Punto de Venta (POS) y Ventanas Flotantes Desacopladas");
        addParagraph(body, "El módulo POS está diseñado para minimizar los tiempos de espera y permitir la multitarea del cajero:");
        addBullet(body, "Ventana Flotante No Modal (VentaFlotanteController)", "Permite tener una venta abierta mientras se consulta libremente el inventario, se crea un producto o se atiende otra consulta en la ventana principal.");
        addBullet(body, "Carrito de Compras Interactivo", "Adición instantánea de artículos, ajuste de cantidades en tabla, cálculo automático de subtotales, impuestos y monto total.");
        addBullet(body, "Múltiples Métodos de Pago", "Soporte de cobro en Efectivo (con cálculo dinámico de cambio/vueltos), Transferencias Digitales (Nequi, Daviplata, Bancolombia), Tarjetas y Venta a Crédito.");
        addBullet(body, "Descuento Transaccional de Existencias", "Al confirmar el cobro, el stock se descuenta atómicamente de la base de datos.");

        addH2(body, "3.4. Cartera de Clientes, Cuentas por Cobrar y Abonos");
        addParagraph(body, "Manejo completo de crédito comercial y fidelización de clientes habituales:");
        addBullet(body, "Directorio de Clientes", "Ficha con Documento de Identidad / Cédula, Nombre Completo, Teléfono y Saldo deudor acumulado.");
        addBullet(body, "Asignación de Ventas a Crédito", "Al facturar con método 'Crédito', la venta se imputa al cliente seleccionado y su saldo deudor se incrementa inmediatamente.");
        addBullet(body, "Registro de Abonos y Liquidación", "Módulo de recepción de abonos en efectivo o transferencias, reduciendo el saldo adeudado y emitiendo el comprobante correspondiente.");
        addBullet(body, "Historial de Pagos y Consulta de Saldos", "Trazabilidad completa de abonos por cliente y por periodo para conciliación contable.");

        addH2(body, "3.5. Entradas de Mercancía, Compras y Proveedores");
        addParagraph(body, "Control del reabastecimiento y relación con distribuidores:");
        addBullet(body, "Catálogo de Proveedores", "Registro de empresas proveedoras vinculables a productos y compras.");
        addBullet(body, "Recepción de Mercancía (EntradaDAO)", "Ingreso de lotes de productos registrando cantidad, costo unitario, fecha, proveedor y usuario receptor.");
        addBullet(body, "Actualización Automática de Stock y Costo", "Suma inmediata al inventario existente con opción de recalcular el precio de venta.");
        addBullet(body, "Cierre Mensual de Compras y Archivo Histórico", "Generación de informe consolidado en PDF (ReporteService) y migración de entradas activas hacia la tabla historico_compras.");

        addH2(body, "3.6. Consumos Internos (Arquitectura Hexagonal)");
        addParagraph(body, "Módulo de máxima precisión contable para registrar mermas, uso propio y garantías sin distorsionar los balances de ventas:");
        addBullet(body, "Motivos de Salida", "Registro categorizado de consumo (ej: herramientas de uso en taller, muestras comerciales, averías o garantías a fábrica).");
        addBullet(body, "Desacoplamiento Total", "El núcleo del dominio (ConsumoService) no contiene ninguna sentencia SQL ni importaciones de librerías externas; interactúa exclusivamente con los puertos ConsumoRepositoryPort y ProductoRepositoryPort.");
        addBullet(body, "Garantía ACID Transaccional", "Verifica la disponibilidad de existencias, descuenta el stock de la tabla de productos y registra la auditoría en consumos_internos en una única transacción atómica.");

        addH2(body, "3.7. Control de Caja, Turnos y Arqueo Diario");
        addParagraph(body, "Supervisión del flujo de caja físico y digital:");
        addBullet(body, "Apertura de Turno", "Establecimiento de la base inicial de efectivo en caja.");
        addBullet(body, "Cierre y Arqueo de Caja (CierreCajaDAO)", "Cálculo comparativo entre las ventas teóricas del sistema (desglosadas por Efectivo, Tarjetas y Transferencias) y el efectivo real recontado por el cajero.");
        addBullet(body, "Reporte de Descuadres", "Identificación exacta de faltantes o sobrantes de caja para control de pérdidas.");
        addBullet(body, "Histórico de Turnos", "Registro permanente de aperturas, cierres, fechas y responsables.");

        addH2(body, "3.8. Reportes Ejecutivos, Estadísticas e Impresión");
        addParagraph(body, "Capacidades analíticas para la toma de decisiones gerenciales:");
        addBullet(body, "Panel de Gráficos Mensuales (ReporteMensualController)", "Visualización con gráficos de barras y sectores de ventas diarias, ingresos por método de pago y ranking de productos más demandados.");
        addBullet(body, "Generación de Documentos PDF", "Creación de reportes limpios y maquetados mediante OpenPDF listos para imprimir o archivar.");
        addBullet(body, "Exportación a Formatos Planos", "Exportación de listados a formato CSV para integración con hojas de cálculo (Excel) y programas contables.");

        addH2(body, "3.9. Sistema Híbrido de Copias de Seguridad (BackupService)");
        addParagraph(body, "Esquema de contingencia robusto para salvaguardar la base de datos sin interrumpir la operación:");
        addBullet(body, "Ejecución Asíncrona (Hilo Secundario)", "El proceso de clonación de la base de datos corre en segundo plano para no congelar la UI de JavaFX.");
        addBullet(body, "Origen Dinámico y Seguro", "Localiza la base de datos activa directamente mediante AppPaths.getDatabaseFile(), evitando bloqueos por permisos en Program Files.");
        addBullet(body, "Doble Destino (Nube + USB)", "Genera una copia en la carpeta de Google Drive sincronizada (%APPDATA%/FerreteriaPro/backups/drive/) y simultáneamente en una memoria USB externa (E:/BackupsFerreteria/).");
        addBullet(body, "Carpeta de Contingencia", "En caso de que la USB no se encuentre conectada, el sistema deposita el respaldo en %APPDATA%/FerreteriaPro/backups/pendientes_usb/ y emite una alerta preventiva al usuario.");

        addH2(body, "3.10. Arquitectura de Almacenamiento Seguro (AppPaths)");
        addParagraph(body, "Aislamiento de datos en espacio de usuario para compatibilidad total con instalaciones de Windows en C:\\Program Files sin requerir privilegios de Administrador (UAC):");
        addBullet(body, "Base de Datos", "%APPDATA%\\FerreteriaPro\\database\\ferreteria_nueva.db (con modo WAL y timeout concurrente).");
        addBullet(body, "Facturas y Comprobantes", "%APPDATA%\\FerreteriaPro\\facturas\\ (tickets de venta, notas de crédito y recibos de abono).");
        addBullet(body, "Copias de Respaldo", "%APPDATA%\\FerreteriaPro\\backups\\ (carpetas locales de Drive y contingencias USB).");
        addBullet(body, "Reportes Físicos", "%APPDATA%\\FerreteriaPro\\reportes\\ (cierres diarios, órdenes de compra sugeridas y compras mensuales).");

        // 4. Esquema de Base de Datos
        addH1(body, "4. Diccionario de Datos y Estructura de Tablas");
        String[] dbHeaders = {"Tabla SQLite", "Función Principal", "Columnas Principales"};
        String[][] dbRows = {
            {"productos", "Catálogo maestro y existencias", "codigo, nombre, precio_compra, precio_venta, stock, activo, proveedor_nombre"},
            {"ventas", "Registro de ventas activas del mes", "id, fecha, producto_codigo, cantidad, total, metodo_pago, usuario_nombre, cliente_id"},
            {"historico_ventas", "Archivo histórico tras cierre mensual", "id, fecha, producto_codigo, cantidad, total, metodo_pago, costo_unitario, cliente_id"},
            {"entradas_inventario", "Recepciones de mercancía activas", "id, producto_codigo, cantidad, costo_unitario, fecha, proveedor, usuario_nombre"},
            {"historico_compras", "Archivo histórico de compras", "id, producto_codigo, cantidad, costo_unitario, fecha, proveedor, fecha_archivo"},
            {"consumos_internos", "Salidas por uso propio o averías", "id, producto_codigo, cantidad, motivo, usuario_id, fecha"},
            {"proveedores", "Catálogo de proveedores", "id, nombre"},
            {"usuarios", "Seguridad y control de acceso", "id, usuario, password_hash, nombre, rol"},
            {"clientes", "Directorio de clientes y créditos", "id, documento, nombre, telefono, saldo_pendiente"},
            {"abonos_credito", "Pagos a cuentas de crédito", "id, cliente_id, monto, fecha, metodo_pago, usuario_nombre"},
            {"cierres_caja", "Arqueos y cierres de turno", "id, fecha_apertura, fecha_cierre, base_inicial, total_efectivo, total_digital, faltante_sobrante"}
        };
        addTable(body, dbHeaders, dbRows);

        // 5. Guía de Ejecución y Requisitos
        addH1(body, "5. Requisitos del Sistema y Puesta en Marcha");
        String[] reqHeaders = {"Requerimiento", "Especificación Mínima", "Especificación Recomendada"};
        String[][] reqRows = {
            {"Sistema Operativo", "Windows 10 (64-bit)", "Windows 11 (64-bit)"},
            {"Java Runtime (JRE/JDK)", "Java 21 LTS", "Java 21 o superior (Oracle / Eclipse Temurin)"},
            {"Memoria RAM", "2 GB", "4 GB o más"},
            {"Espacio en Disco", "200 MB libres", "500 MB (incluyendo respaldos periódicos)"},
            {"Almacenamiento Externo", "Memoria USB estándar (FAT32/NTFS)", "Unidad USB dedicada de 16 GB o superior"}
        };
        addTable(body, reqHeaders, reqRows);

        addNote(body, "Para iniciar la aplicación en desarrollo, ejecute Launcher.java. La base de datos 'ferreteria_nueva.db' se inicializa de forma completamente automática con sus tablas, migraciones y usuarios de prueba por defecto (admin/admin123 y vendedor/vendedor123).");

        // 6. Empaquetado Nativo de Windows (.exe)
        addH1(body, "6. Pipeline de Empaquetado Nativo (.exe) con jpackage");
        addParagraph(body, "Para garantizar la portabilidad y facilitar la distribución comercial a usuarios finales en entornos Windows sin necesidad de configurar previamente Java o Maven:");
        addBullet(body, "Estrategia Fat JAR (Maven Shade)", "Debido a librerías híbridas no modulares (como SQLite JDBC y OpenPDF), se utiliza maven-shade-plugin 3.5.1 para consolidar el bytecode, recursos y ServiceLoader providers en un artefacto ejecutable unificado.");
        addBullet(body, "Generación Vectorial de Icono", "Conversión automatizada de src/main/resources/com/ferreteria/ferreteriapro/icon.png hacia formato multipágina Windows .ico (256x256) mediante PowerShell en tiempo de construcción.");
        addBullet(body, "Empaquetado jpackage + WiX Toolset", "Generación de instalador estándar .exe (FerreteriaPro-1.0.0.exe) con opciones --win-shortcut, --win-menu y --win-dir-chooser para permitir al usuario definir la ruta de instalación.");
        addBullet(body, "Script Automatizado build_installer.bat", "Script de construcción en un solo paso que ejecuta clean package, genera el icono nativo y compila el instalador en target/installer/.");

        // Assemble document.xml
        String docXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"\n"
            + "            xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n"
            + "  <w:body>\n"
            + body.toString()
            + "    <w:sectPr>\n"
            + "      <w:pgSz w:w=\"12240\" w:h=\"15840\"/>\n"
            + "      <w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>\n"
            + "    </w:sectPr>\n"
            + "  </w:body>\n"
            + "</w:document>";

        // Packaging into docx (ZIP)
        String contentTypesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n"
            + "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n"
            + "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n"
            + "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n"
            + "  <Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>\n"
            + "</Types>";

        String dotRelsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
            + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n"
            + "</Relationships>";

        String docRelsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
            + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n"
            + "</Relationships>";

        String stylesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n"
            + "  <w:docDefaults>\n"
            + "    <w:rPrDefault>\n"
            + "      <w:rPr>\n"
            + "        <w:rFonts w:ascii=\"Segoe UI\" w:hAnsi=\"Segoe UI\" w:cs=\"Segoe UI\"/>\n"
            + "        <w:sz w:val=\"22\"/>\n"
            + "        <w:color w:val=\"2D3748\"/>\n"
            + "      </w:rPr>\n"
            + "    </w:rPrDefault>\n"
            + "    <w:pPrDefault>\n"
            + "      <w:pPr>\n"
            + "        <w:spacing w:line=\"276\" w:lineRule=\"auto\" w:after=\"160\"/>\n"
            + "      </w:pPr>\n"
            + "    </w:pPrDefault>\n"
            + "  </w:docDefaults>\n"
            + "</w:styles>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetPath.toFile()))) {
            addZipEntry(zos, "[Content_Types].xml", contentTypesXml);
            addZipEntry(zos, "_rels/.rels", dotRelsXml);
            addZipEntry(zos, "word/_rels/document.xml.rels", docRelsXml);
            addZipEntry(zos, "word/styles.xml", stylesXml);
            addZipEntry(zos, "word/document.xml", docXml);
        }
    }

    private static void addZipEntry(ZipOutputStream zos, String path, String content) throws Exception {
        ZipEntry entry = new ZipEntry(path);
        zos.putNextEntry(entry);
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        zos.write(data, 0, data.length);
        zos.closeEntry();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static void addTitle(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"360\" w:after=\"120\"/></w:pPr>")
          .append("<w:r><w:rPr><w:b/><w:color w:val=\"1E3A8A\"/><w:sz w:val=\"52\"/><w:rFonts w:ascii=\"Segoe UI Semibold\" w:hAnsi=\"Segoe UI Semibold\"/></w:rPr>")
          .append("<w:t>").append(escape(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void addSubtitle(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"0\" w:after=\"300\"/></w:pPr>")
          .append("<w:r><w:rPr><w:i/><w:color w:val=\"4B5563\"/><w:sz w:val=\"26\"/></w:rPr>")
          .append("<w:t>").append(escape(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void addMetaBox(StringBuilder sb, String title, String version, String date, String stack) {
        sb.append("<w:tbl><w:tblPr><w:tblW w:w=\"9600\" w:type=\"dxa\"/><w:jc w:val=\"center\"/>")
          .append("<w:tblBorders>")
          .append("<w:top w:val=\"single\" w:sz=\"6\" w:space=\"0\" w:color=\"93C5FD\"/>")
          .append("<w:left w:val=\"single\" w:sz=\"24\" w:space=\"0\" w:color=\"2563EB\"/>")
          .append("<w:bottom w:val=\"single\" w:sz=\"6\" w:space=\"0\" w:color=\"93C5FD\"/>")
          .append("<w:right w:val=\"single\" w:sz=\"6\" w:space=\"0\" w:color=\"93C5FD\"/>")
          .append("</w:tblBorders>")
          .append("<w:tblCellMar><w:top w:w=\"140\" w:type=\"dxa\"/><w:left w:w=\"200\" w:type=\"dxa\"/><w:bottom w:w=\"140\" w:type=\"dxa\"/><w:right w:w=\"200\" w:type=\"dxa\"/></w:tblCellMar>")
          .append("</w:tblPr><w:tr><w:tc><w:tcPr><w:tcW w:w=\"9600\" w:type=\"dxa\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"EFF6FF\"/></w:tcPr>")
          .append("<w:p><w:pPr><w:spacing w:after=\"40\"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val=\"1E40AF\"/></w:rPr><w:t>Sistema: </w:t></w:r><w:r><w:t>").append(escape(title)).append("</w:t></w:r></w:p>")
          .append("<w:p><w:pPr><w:spacing w:after=\"40\"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val=\"1E40AF\"/></w:rPr><w:t>Versión / Edición: </w:t></w:r><w:r><w:t>").append(escape(version)).append("</w:t></w:r></w:p>")
          .append("<w:p><w:pPr><w:spacing w:after=\"40\"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val=\"1E40AF\"/></w:rPr><w:t>Fecha de Emisión: </w:t></w:r><w:r><w:t>").append(escape(date)).append("</w:t></w:r></w:p>")
          .append("<w:p><w:pPr><w:spacing w:after=\"40\"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val=\"1E40AF\"/></w:rPr><w:t>Stack Tecnológico: </w:t></w:r><w:r><w:t>").append(escape(stack)).append("</w:t></w:r></w:p>")
          .append("</w:tc></w:tr></w:tbl><w:p><w:pPr><w:spacing w:after=\"240\"/></w:pPr></w:p>\n");
    }

    private static void addH1(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:spacing w:before=\"360\" w:after=\"140\"/>")
          .append("<w:pBdr><w:bottom w:val=\"single\" w:sz=\"12\" w:space=\"4\" w:color=\"2563EB\"/></w:pBdr></w:pPr>")
          .append("<w:r><w:rPr><w:b/><w:color w:val=\"1E3A8A\"/><w:sz w:val=\"34\"/><w:rFonts w:ascii=\"Segoe UI Semibold\" w:hAnsi=\"Segoe UI Semibold\"/></w:rPr>")
          .append("<w:t>").append(escape(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void addH2(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:spacing w:before=\"260\" w:after=\"100\"/></w:pPr>")
          .append("<w:r><w:rPr><w:b/><w:color w:val=\"0D9488\"/><w:sz w:val=\"28\"/><w:rFonts w:ascii=\"Segoe UI Semibold\" w:hAnsi=\"Segoe UI Semibold\"/></w:rPr>")
          .append("<w:t>").append(escape(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void addParagraph(StringBuilder sb, String text) {
        sb.append("<w:p><w:pPr><w:spacing w:after=\"140\" w:line=\"276\" w:lineRule=\"auto\"/><w:jc w:val=\"both\"/></w:pPr>")
          .append("<w:r><w:rPr><w:sz w:val=\"22\"/><w:color w:val=\"334155\"/></w:rPr>")
          .append("<w:t xml:space=\"preserve\">").append(escape(text)).append("</w:t></w:r></w:p>\n");
    }

    private static void addBullet(StringBuilder sb, String title, String desc) {
        sb.append("<w:p><w:pPr><w:ind w:left=\"400\" w:hanging=\"200\"/><w:spacing w:after=\"80\"/></w:pPr>")
          .append("<w:r><w:rPr><w:color w:val=\"2563EB\"/><w:b/></w:rPr><w:t xml:space=\"preserve\">▪  </w:t></w:r>")
          .append("<w:r><w:rPr><w:b/><w:color w:val=\"1E293B\"/></w:rPr><w:t xml:space=\"preserve\">").append(escape(title)).append(": </w:t></w:r>")
          .append("<w:r><w:rPr><w:color w:val=\"475569\"/></w:rPr><w:t>").append(escape(desc)).append("</w:t></w:r></w:p>\n");
    }

    private static void addNote(StringBuilder sb, String text) {
        sb.append("<w:tbl><w:tblPr><w:tblW w:w=\"9600\" w:type=\"dxa\"/><w:jc w:val=\"center\"/>")
          .append("<w:tblBorders><w:top w:val=\"none\"/><w:left w:val=\"single\" w:sz=\"24\" w:space=\"0\" w:color=\"059669\"/><w:bottom w:val=\"none\"/><w:right w:val=\"none\"/></w:tblBorders>")
          .append("<w:tblCellMar><w:top w:w=\"120\" w:type=\"dxa\"/><w:left w:w=\"180\" w:type=\"dxa\"/><w:bottom w:w=\"120\" w:type=\"dxa\"/><w:right w:w=\"180\" w:type=\"dxa\"/></w:tblCellMar>")
          .append("</w:tblPr><w:tr><w:tc><w:tcPr><w:tcW w:w=\"9600\" w:type=\"dxa\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"ECFDF5\"/></w:tcPr>")
          .append("<w:p><w:pPr><w:spacing w:after=\"0\"/></w:pPr>")
          .append("<w:r><w:rPr><w:b/><w:color w:val=\"065F46\"/></w:rPr><w:t xml:space=\"preserve\">📌 Nota Clave: </w:t></w:r>")
          .append("<w:r><w:rPr><w:color w:val=\"047857\"/></w:rPr><w:t>").append(escape(text)).append("</w:t></w:r>")
          .append("</w:p></w:tc></w:tr></w:tbl><w:p><w:pPr><w:spacing w:after=\"160\"/></w:pPr></w:p>\n");
    }

    private static void addTable(StringBuilder sb, String[] headers, String[][] rows) {
        int colCount = headers.length;
        int totalWidth = 9600;
        int colWidth = totalWidth / colCount;

        sb.append("<w:tbl><w:tblPr><w:tblW w:w=\"").append(totalWidth).append("\" w:type=\"dxa\"/><w:jc w:val=\"center\"/>")
          .append("<w:tblBorders>")
          .append("<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>")
          .append("<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>")
          .append("<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>")
          .append("<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CBD5E1\"/>")
          .append("<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>")
          .append("<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E2E8F0\"/>")
          .append("</w:tblBorders>")
          .append("<w:tblCellMar><w:top w:w=\"100\" w:type=\"dxa\"/><w:left w:w=\"120\" w:type=\"dxa\"/><w:bottom w:w=\"100\" w:type=\"dxa\"/><w:right w:w=\"120\" w:type=\"dxa\"/></w:tblCellMar>")
          .append("</w:tblPr><w:tr>");

        for (String h : headers) {
            sb.append("<w:tc><w:tcPr><w:tcW w:w=\"").append(colWidth).append("\" w:type=\"dxa\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"1E3A8A\"/></w:tcPr>")
              .append("<w:p><w:pPr><w:jc w:val=\"center\"/><w:spacing w:after=\"0\"/></w:pPr>")
              .append("<w:r><w:rPr><w:b/><w:color w:val=\"FFFFFF\"/><w:sz w:val=\"20\"/></w:rPr><w:t>")
              .append(escape(h)).append("</w:t></w:r></w:p></w:tc>");
        }
        sb.append("</w:tr>");

        for (int r = 0; r < rows.length; r++) {
            String bg = (r % 2 == 0) ? "F8FAFC" : "FFFFFF";
            sb.append("<w:tr>");
            for (int c = 0; c < colCount; c++) {
                String val = (c < rows[r].length) ? rows[r][c] : "";
                sb.append("<w:tc><w:tcPr><w:tcW w:w=\"").append(colWidth).append("\" w:type=\"dxa\"/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"").append(bg).append("\"/></w:tcPr>")
                  .append("<w:p><w:pPr><w:spacing w:after=\"0\"/></w:pPr>")
                  .append("<w:r><w:rPr><w:color w:val=\"334155\"/><w:sz w:val=\"19\"/></w:rPr><w:t>")
                  .append(escape(val)).append("</w:t></w:r></w:p></w:tc>");
            }
            sb.append("</w:tr>");
        }
        sb.append("</w:tbl><w:p><w:pPr><w:spacing w:after=\"180\"/></w:pPr></w:p>\n");
    }
}

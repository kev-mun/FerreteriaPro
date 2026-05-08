module com.ferreteria.ferreteriapro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.controlsfx.controls;
    requires com.github.librepdf.openpdf;
    requires java.desktop;

    opens com.ferreteria.ferreteriapro to javafx.fxml;
    opens com.ferreteria.ferreteriapro.model to javafx.base;

    exports com.ferreteria.ferreteriapro;
    exports com.ferreteria.ferreteriapro.model;
    exports com.ferreteria.ferreteriapro.service;
}
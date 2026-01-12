module at.newsfx.fhtechnikum.newsfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires java.xml;
    requires java.desktop;
    requires javafx.web;
    requires java.sql;

    requires com.h2database;


    opens at.newsfx.fhtechnikum.newsfx.controller to javafx.fxml;

    exports at.newsfx.fhtechnikum.newsfx.app;
}
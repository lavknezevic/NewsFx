module at.newsfx.fhtechnikum.newsfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;
    requires java.xml;
    requires javafx.web;


    opens at.newsfx.fhtechnikum.newsfx.controller to javafx.fxml;

    exports at.newsfx.fhtechnikum.newsfx.app;
}
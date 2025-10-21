module com.unmadgamer.lostandfoundfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens com.unmadgamer.lostandfoundfinal to javafx.fxml;
    opens com.unmadgamer.lostandfoundfinal.controller to javafx.fxml;
    opens com.unmadgamer.lostandfoundfinal.model to javafx.base, com.fasterxml.jackson.databind;

    exports com.unmadgamer.lostandfoundfinal;
    exports com.unmadgamer.lostandfoundfinal.controller;
    exports com.unmadgamer.lostandfoundfinal.model;
}
package com.unmadgamer.lostandfoundfinal.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private void handleAddItem() {
        // This will be handled by the dashboard now
        showAlert("Info", "Please use the dashboard buttons to report lost or found items.");
    }

    @FXML
    private void handleSearchItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/search.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Search Lost & Found");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (IOException e) {
            showError("Cannot open Search form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Dashboard - Lost and Found System");
            stage.setScene(new Scene(root, 600, 450));
            stage.show();
        } catch (IOException e) {
            showError("Cannot open Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
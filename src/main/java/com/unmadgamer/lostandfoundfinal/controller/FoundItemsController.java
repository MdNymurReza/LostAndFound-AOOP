package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class FoundItemsController {

    @FXML
    private Label userNameLabel;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        if (userService.getCurrentUser() != null) {
            loadFoundItems();
        }
    }

    private void loadFoundItems() {
        String currentUser = userService.getCurrentUser().getUsername();
        List<LostFoundItem> foundItems = itemService.getItemsByStatus("found");

        // For now, we'll just set the user name
        // In a real implementation, you'd dynamically create UI elements for each item
        userNameLabel.setText(userService.getCurrentUser().getFirstName() + " " + userService.getCurrentUser().getLastName());

        System.out.println("Loaded " + foundItems.size() + " found items for user: " + currentUser);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) userNameLabel.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();
        } catch (IOException e) {
            showError("Cannot return to dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
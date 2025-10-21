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

public class ReturnedItemsController {

    @FXML
    private Label userNameLabel;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        if (userService.getCurrentUser() != null) {
            loadReturnedItems();
        }
    }

    private void loadReturnedItems() {
        String currentUser = userService.getCurrentUser().getUsername();
        List<LostFoundItem> returnedItems = itemService.getAllItems().stream()
                .filter(LostFoundItem::isReturned)
                .toList();

        userNameLabel.setText(userService.getCurrentUser().getFirstName() + " " + userService.getCurrentUser().getLastName());

        System.out.println("Loaded " + returnedItems.size() + " returned items for user: " + currentUser);
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
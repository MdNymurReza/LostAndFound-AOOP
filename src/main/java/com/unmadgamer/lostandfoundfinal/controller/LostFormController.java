package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class LostFormController {

    @FXML
    private TextField numberField;

    @FXML
    private TextField itemNameField;

    @FXML
    private TextField categoryField;

    @FXML
    private DatePicker lostDatePicker;

    @FXML
    private TextField lostPlaceField;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        lostDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSubmit() {
        if (validateInput()) {
            String currentUser = userService.getCurrentUser().getUsername();
            String userEmail = userService.getCurrentUser().getEmail();

            // In the handleSubmit method, change the item creation:
            LostFoundItem lostItem = new LostFoundItem(
                    itemNameField.getText(),
                    categoryField.getText(),
                    "Lost item - " + itemNameField.getText(),
                    lostDatePicker.getValue(), // This will automatically convert to String
                    lostPlaceField.getText(),
                    "lost",
                    currentUser,
                    userEmail
            );

            itemService.addItem(lostItem);

            showSuccess("Lost item reported successfully!");
            clearForm();
            returnToDashboard();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        returnToDashboard();
    }

    private void returnToDashboard() {
        try {
            Stage currentStage = (Stage) itemNameField.getScene().getWindow();
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

    private boolean validateInput() {
        if (itemNameField.getText().trim().isEmpty()) {
            showError("Item name is required");
            return false;
        }
        if (categoryField.getText().trim().isEmpty()) {
            showError("Category is required");
            return false;
        }
        if (lostDatePicker.getValue() == null) {
            showError("Lost date is required");
            return false;
        }
        if (lostPlaceField.getText().trim().isEmpty()) {
            showError("Lost place is required");
            return false;
        }
        return true;
    }

    private void clearForm() {
        numberField.clear();
        itemNameField.clear();
        categoryField.clear();
        lostDatePicker.setValue(LocalDate.now());
        lostPlaceField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
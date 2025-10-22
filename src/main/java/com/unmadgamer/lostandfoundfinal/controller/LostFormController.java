package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LostFormController {

    @FXML private TextField itemNameField;
    @FXML private TextField categoryField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private DatePicker lostDatePicker;
    @FXML private TextField contactInfoField;
    @FXML private TextField rewardField;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        lostDatePicker.setValue(LocalDate.now());

        if (userService.getCurrentUser() != null) {
            contactInfoField.setText(userService.getCurrentUser().getEmail());
        }
    }

    @FXML
    private void handleSubmit() {
        if (validateInput()) {
            String currentUser = userService.getCurrentUser().getUsername();
            String formattedDate = lostDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Create lost item using constructor
            LostItem lostItem = new LostItem(
                    itemNameField.getText().trim(),
                    categoryField.getText().trim(),
                    descriptionField.getText().trim(),
                    locationField.getText().trim(),
                    formattedDate,
                    currentUser,
                    formattedDate,
                    rewardField.getText().trim(),
                    contactInfoField.getText().trim()
            );

            // Set initial status
            lostItem.setStatus("active");
            lostItem.setVerificationStatus("pending");

            if (itemService.addLostItem(lostItem)) {
                showSuccess("Lost item reported successfully!\n\nThe item has been added to the lost items list.");
                clearForm();
                closeWindow();
            } else {
                showError("Failed to save lost item. Please try again.");
            }
        }
    }

    @FXML
    private void handleBackToDashboard() {
        closeWindow();
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        showAlert("Form Cleared", "All form fields have been cleared.", Alert.AlertType.INFORMATION);
    }

    private void closeWindow() {
        Stage currentStage = (Stage) itemNameField.getScene().getWindow();
        currentStage.close();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (itemNameField.getText().trim().isEmpty()) {
            errors.append("• Item name is required\n");
        }
        if (categoryField.getText().trim().isEmpty()) {
            errors.append("• Category is required\n");
        }
        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("• Description is required\n");
        }
        if (locationField.getText().trim().isEmpty()) {
            errors.append("• Location is required\n");
        }
        if (lostDatePicker.getValue() == null) {
            errors.append("• Lost date is required\n");
        }
        if (contactInfoField.getText().trim().isEmpty()) {
            errors.append("• Contact information is required\n");
        }

        if (errors.length() > 0) {
            showError("Please fix the following errors:\n\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void clearForm() {
        itemNameField.clear();
        categoryField.clear();
        descriptionField.clear();
        locationField.clear();
        rewardField.clear();
        lostDatePicker.setValue(LocalDate.now());

        if (userService.getCurrentUser() != null) {
            contactInfoField.setText(userService.getCurrentUser().getEmail());
        } else {
            contactInfoField.clear();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Lost Item Reported Successfully");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
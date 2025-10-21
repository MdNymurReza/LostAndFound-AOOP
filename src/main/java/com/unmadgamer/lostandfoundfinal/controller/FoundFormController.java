package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.FoundItem;
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

public class FoundFormController {

    @FXML
    private TextField itemNameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField locationField;

    @FXML
    private DatePicker foundDatePicker;

    @FXML
    private TextField contactInfoField;

    @FXML
    private TextField storageLocationField;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        foundDatePicker.setValue(LocalDate.now());

        // Set default contact info to current user's email
        if (userService.getCurrentUser() != null) {
            contactInfoField.setText(userService.getCurrentUser().getEmail());
        }

        System.out.println("📝 FoundFormController initialized for user: " +
                (userService.getCurrentUser() != null ? userService.getCurrentUser().getUsername() : "Unknown"));

        // Debug: Check if services are properly initialized
        System.out.println("🔧 ItemService instance: " + (itemService != null ? "OK" : "NULL"));
        System.out.println("🔧 UserService instance: " + (userService != null ? "OK" : "NULL"));
    }

    @FXML
    private void handleSubmit() {
        System.out.println("🔄 Submit button clicked in Found Form");

        if (validateInput()) {
            String currentUser = userService.getCurrentUser().getUsername();

            // Format the date properly
            String formattedDate = foundDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Create the found item using the FoundItem subclass
            FoundItem foundItem = new FoundItem();
            foundItem.setItemName(itemNameField.getText().trim());
            foundItem.setCategory(categoryField.getText().trim());
            foundItem.setDescription(descriptionField.getText().trim());
            foundItem.setLocation(locationField.getText().trim());
            foundItem.setReportedBy(currentUser);
            foundItem.setDate(formattedDate);
            foundItem.setContactInfo(contactInfoField.getText().trim());
            foundItem.setFoundDate(formattedDate);
            foundItem.setStorageLocation(storageLocationField.getText().trim());

            // Set initial status
            foundItem.setStatus("pending");
            foundItem.setVerificationStatus("pending");

            System.out.println("➕ Attempting to add found item: " + foundItem.getItemName());
            System.out.println("📋 Item details - Category: " + foundItem.getCategory() +
                    ", Location: " + foundItem.getLocation() +
                    ", Storage: " + foundItem.getStorageLocation() +
                    ", Reported by: " + foundItem.getReportedBy());

            if (itemService.addFoundItem(foundItem)) {
                System.out.println("✅ Found item added successfully!");
                showSuccess("Found item reported successfully!\n\n" +
                        "Item: " + foundItem.getItemName() + "\n" +
                        "Category: " + foundItem.getCategory() + "\n" +
                        "Location Found: " + foundItem.getLocation() + "\n" +
                        "Storage: " + foundItem.getStorageLocation() + "\n\n" +
                        "✅ The item has been submitted for admin verification.\n" +
                        "You can track the verification status in your items list.");

                clearForm();
                closeWindow();
            } else {
                System.err.println("❌ Failed to add found item to service");
                showError("Failed to save found item. Please try again.");
            }
        } else {
            System.out.println("❌ Form validation failed");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        System.out.println("🔙 Back to dashboard clicked");
        closeWindow();
    }

    @FXML
    private void handleClearForm() {
        System.out.println("🗑️ Clear form clicked");
        clearForm();
        showAlert("Form Cleared", "All form fields have been cleared.", Alert.AlertType.INFORMATION);
    }

    private void closeWindow() {
        try {
            Stage currentStage = (Stage) itemNameField.getScene().getWindow();
            currentStage.close();
            System.out.println("✅ Found form window closed");
        } catch (Exception e) {
            System.err.println("❌ Error closing window: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        // Item Name validation
        if (itemNameField.getText().trim().isEmpty()) {
            errors.append("• Item name is required\n");
        } else if (itemNameField.getText().trim().length() < 2) {
            errors.append("• Item name must be at least 2 characters\n");
        }

        // Category validation
        if (categoryField.getText().trim().isEmpty()) {
            errors.append("• Category is required\n");
        } else if (categoryField.getText().trim().length() < 2) {
            errors.append("• Category must be at least 2 characters\n");
        }

        // Description validation
        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("• Description is required\n");
        } else if (descriptionField.getText().trim().length() < 10) {
            errors.append("• Description must be at least 10 characters\n");
        }

        // Location validation
        if (locationField.getText().trim().isEmpty()) {
            errors.append("• Location where item was found is required\n");
        } else if (locationField.getText().trim().length() < 3) {
            errors.append("• Location must be at least 3 characters\n");
        }

        // Storage location validation
        if (storageLocationField.getText().trim().isEmpty()) {
            errors.append("• Current storage location is required\n");
        } else if (storageLocationField.getText().trim().length() < 3) {
            errors.append("• Storage location must be at least 3 characters\n");
        }

        // Date validation
        if (foundDatePicker.getValue() == null) {
            errors.append("• Found date is required\n");
        } else if (foundDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("• Found date cannot be in the future\n");
        }

        // Contact info validation
        if (contactInfoField.getText().trim().isEmpty()) {
            errors.append("• Contact information is required\n");
        } else if (!isValidContactInfo(contactInfoField.getText().trim())) {
            errors.append("• Please provide valid contact information (email or phone)\n");
        }

        if (errors.length() > 0) {
            showError("Please fix the following errors:\n\n" + errors.toString());
            return false;
        }

        System.out.println("✅ Form validation passed");
        return true;
    }

    private boolean isValidContactInfo(String contactInfo) {
        // Basic validation for email or phone
        if (contactInfo.contains("@")) {
            // Email validation
            return contactInfo.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        } else {
            // Phone validation - at least 10 digits
            String digitsOnly = contactInfo.replaceAll("\\D", "");
            return digitsOnly.length() >= 10;
        }
    }

    private void clearForm() {
        itemNameField.clear();
        categoryField.clear();
        descriptionField.clear();
        locationField.clear();
        storageLocationField.clear();
        foundDatePicker.setValue(LocalDate.now());

        // Reset contact info to user's email
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
        alert.setHeaderText("Found Item Reported Successfully");
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
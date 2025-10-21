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

    @FXML
    private TextField itemNameField;

    @FXML
    private TextField categoryField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField locationField;

    @FXML
    private DatePicker lostDatePicker;

    @FXML
    private TextField contactInfoField;

    @FXML
    private TextField rewardField;

    private ItemService itemService;
    private UserService userService;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        lostDatePicker.setValue(LocalDate.now());

        // Set default contact info to current user's email
        if (userService.getCurrentUser() != null) {
            contactInfoField.setText(userService.getCurrentUser().getEmail());
        }

        System.out.println("📝 LostFormController initialized for user: " +
                (userService.getCurrentUser() != null ? userService.getCurrentUser().getUsername() : "Unknown"));

        // Debug: Check if services are properly initialized
        System.out.println("🔧 ItemService instance: " + (itemService != null ? "OK" : "NULL"));
        System.out.println("🔧 UserService instance: " + (userService != null ? "OK" : "NULL"));
    }

    @FXML
    private void handleSubmit() {
        System.out.println("🔄 Submit button clicked in Lost Form");

        if (validateInput()) {
            String currentUser = userService.getCurrentUser().getUsername();

            // Format the date properly
            String formattedDate = lostDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Create the lost item using the LostItem subclass
            LostItem lostItem = new LostItem();
            lostItem.setItemName(itemNameField.getText().trim());
            lostItem.setCategory(categoryField.getText().trim());
            lostItem.setDescription(descriptionField.getText().trim());
            lostItem.setLocation(locationField.getText().trim());
            lostItem.setReportedBy(currentUser);
            lostItem.setDate(formattedDate);
            lostItem.setContactInfo(contactInfoField.getText().trim());
            lostItem.setLostDate(formattedDate);
            lostItem.setReward(rewardField.getText().trim());

            // Set initial status
            lostItem.setStatus("pending");
            lostItem.setVerificationStatus("pending");

            System.out.println("➕ Attempting to add lost item: " + lostItem.getItemName());
            System.out.println("📋 Item details - Category: " + lostItem.getCategory() +
                    ", Location: " + lostItem.getLocation() +
                    ", Reward: " + lostItem.getReward() +
                    ", Reported by: " + lostItem.getReportedBy());

            if (itemService.addLostItem(lostItem)) {
                System.out.println("✅ Lost item added successfully!");
                showSuccess("Lost item reported successfully!\n\n" +
                        "Item: " + lostItem.getItemName() + "\n" +
                        "Category: " + lostItem.getCategory() + "\n" +
                        "Location Lost: " + lostItem.getLocation() + "\n" +
                        "Reward: " + (lostItem.getReward() != null && !lostItem.getReward().isEmpty() ? lostItem.getReward() : "Not specified") + "\n\n" +
                        "🔍 Your item has been added to the lost items list.\n" +
                        "You will be notified if someone finds it!\n\n" +
                        "📋 The item is now pending admin verification.");

                clearForm();
                closeWindow();
            } else {
                System.err.println("❌ Failed to add lost item to service");
                showError("Failed to save lost item. Please try again.");
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
            System.out.println("✅ Lost form window closed");
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
            errors.append("• Location where item was lost is required\n");
        } else if (locationField.getText().trim().length() < 3) {
            errors.append("• Location must be at least 3 characters\n");
        }

        // Date validation
        if (lostDatePicker.getValue() == null) {
            errors.append("• Lost date is required\n");
        } else if (lostDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("• Lost date cannot be in the future\n");
        }

        // Contact info validation
        if (contactInfoField.getText().trim().isEmpty()) {
            errors.append("• Contact information is required\n");
        } else if (!isValidContactInfo(contactInfoField.getText().trim())) {
            errors.append("• Please provide valid contact information (email or phone)\n");
        }

        // Reward validation (optional)
        String reward = rewardField.getText().trim();
        if (!reward.isEmpty() && reward.length() < 2) {
            errors.append("• Reward description must be at least 2 characters if provided\n");
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
        rewardField.clear();
        lostDatePicker.setValue(LocalDate.now());

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
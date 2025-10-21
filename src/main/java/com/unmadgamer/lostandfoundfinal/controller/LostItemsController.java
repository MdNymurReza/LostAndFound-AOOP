package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class LostItemsController {

    @FXML private VBox itemsContainer;
    @FXML private Label totalItemsLabel;
    @FXML private Label verifiedItemsLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;

    private ItemService itemService;
    private UserService userService;
    private List<LostFoundItem> allLostItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        setupEventHandlers();
        loadLostItems();

        if (userService.getCurrentUser() != null) {
            subtitleLabel.setText("Welcome, " + userService.getCurrentUser().getFirstName());
        }
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());

        // Initialize filter combo
        filterCombo.getItems().addAll("All Items", "Verified Only", "Pending Verification");
        filterCombo.setValue("All Items");
    }

    private void loadLostItems() {
        // Get items for current user only
        String currentUser = userService.getCurrentUser().getUsername();

        // If user is admin, show all lost items. Otherwise, show only user's items
        if (userService.getCurrentUser().isAdmin()) {
            allLostItems = itemService.getItemsByStatus("lost");
        } else {
            allLostItems = itemService.getUserItemsByStatus(currentUser, "lost");
        }

        updateStatistics();
        displayItems(allLostItems);
    }

    private void updateStatistics() {
        totalItemsLabel.setText(String.valueOf(allLostItems.size()));
        long verifiedCount = allLostItems.stream().filter(LostFoundItem::isVerified).count();
        verifiedItemsLabel.setText(String.valueOf(verifiedCount));
    }

    private void displayItems(List<LostFoundItem> items) {
        itemsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label noItemsLabel = new Label("No lost items found");
            noItemsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px; -fx-padding: 40px;");
            itemsContainer.getChildren().add(noItemsLabel);
            return;
        }

        for (LostFoundItem item : items) {
            itemsContainer.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(LostFoundItem item) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setSpacing(10);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);

        // Header with item name and status
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getItemName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Status badge
        Label statusBadge = new Label(item.isLost() ? "LOST" : item.isFound() ? "FOUND" : "RETURNED");
        statusBadge.setStyle("-fx-background-color: " +
                (item.isLost() ? "#e74c3c" : item.isFound() ? "#3498db" : "#27ae60") +
                "; -fx-text-fill: white; -fx-padding: 4 8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Verification badge
        Label verificationBadge = new Label(item.isVerified() ? "✓ VERIFIED" : "⏳ PENDING");
        verificationBadge.setStyle("-fx-text-fill: " +
                (item.isVerified() ? "#27ae60" : "#e67e22") +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");

        header.getChildren().addAll(nameLabel, statusBadge, spacer, verificationBadge);

        // Item details
        Label categoryLabel = new Label("Category: " + item.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        // Footer with location, date and actions
        HBox footer = new HBox();
        footer.setSpacing(15);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label locationLabel = new Label("📍 " + item.getLocation());
        locationLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        Label dateLabel = new Label("📅 " + item.getDate());
        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        viewDetailsBtn.setOnAction(e -> showItemDetails(item));

        // Add admin actions if user is admin
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        if (userService.getCurrentUser().isAdmin() && !item.isVerified()) {
            Button verifyBtn = new Button("Verify");
            verifyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            verifyBtn.setOnAction(e -> verifyItem(item));
            actionButtons.getChildren().add(verifyBtn);
        }

        if (userService.getCurrentUser().isAdmin() && item.isLost()) {
            Button markFoundBtn = new Button("Mark Found");
            markFoundBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            markFoundBtn.setOnAction(e -> markAsFound(item));
            actionButtons.getChildren().add(markFoundBtn);
        }

        actionButtons.getChildren().add(viewDetailsBtn);
        footer.getChildren().addAll(locationLabel, dateLabel, footerSpacer, actionButtons);

        card.getChildren().addAll(header, categoryLabel, descLabel, footer);
        return card;
    }

    private void verifyItem(LostFoundItem item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Verification");
        confirmAlert.setHeaderText("Verify Lost Item: " + item.getItemName());
        confirmAlert.setContentText("Are you sure you want to verify this lost item?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = itemService.verifyItem(item.getItemName(), userService.getCurrentUser().getUsername());
                if (success) {
                    showAlert("Success", "Item has been verified successfully!");
                    loadLostItems(); // Refresh the list
                } else {
                    showError("Failed to verify item");
                }
            }
        });
    }

    private void markAsFound(LostFoundItem item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark as Found");
        confirmAlert.setHeaderText("Mark Item as Found: " + item.getItemName());
        confirmAlert.setContentText("Are you sure you want to mark this lost item as found?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = itemService.updateItemStatus(item.getItemName(), "found", userService.getCurrentUser().getUsername());
                if (success) {
                    showAlert("Success", "Item has been marked as found!");
                    loadLostItems(); // Refresh the list
                } else {
                    showError("Failed to update item status");
                }
            }
        });
    }

    private void filterItems() {
        String searchText = searchField.getText().toLowerCase();
        String filterValue = filterCombo.getValue();

        List<LostFoundItem> filteredItems = allLostItems.stream()
                .filter(item ->
                        item.getItemName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText) ||
                                item.getCategory().toLowerCase().contains(searchText) ||
                                item.getLocation().toLowerCase().contains(searchText)
                )
                .filter(item -> {
                    if (filterValue == null || "All Items".equals(filterValue)) {
                        return true;
                    } else if ("Verified Only".equals(filterValue)) {
                        return item.isVerified();
                    } else if ("Pending Verification".equals(filterValue)) {
                        return !item.isVerified();
                    }
                    return true;
                })
                .collect(Collectors.toList());

        displayItems(filteredItems);
    }

    private void showItemDetails(LostFoundItem item) {
        StringBuilder details = new StringBuilder();
        details.append("=== LOST ITEM DETAILS ===\n\n");
        details.append("Item Name: ").append(item.getItemName()).append("\n");
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Description: ").append(item.getDescription()).append("\n");
        details.append("Location: ").append(item.getLocation()).append("\n");
        details.append("Date Lost: ").append(item.getDate()).append("\n");
        details.append("Reported By: ").append(item.getReportedBy()).append("\n");
        details.append("Contact Info: ").append(item.getContactInfo() != null ? item.getContactInfo() : "Not provided").append("\n");
        details.append("Unique ID: ").append(item.getUniqueId()).append("\n\n");

        details.append("=== VERIFICATION STATUS ===\n");
        if (item.isVerified()) {
            details.append("✅ VERIFIED\n");
            details.append("Verified By: ").append(item.getVerifiedBy()).append("\n");
            details.append("Verification Date: ").append(item.getVerificationDate()).append("\n");
        } else {
            details.append("⏳ PENDING VERIFICATION\n");
            details.append("This item is waiting for admin verification.\n");
        }

        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 300);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText("Lost Item: " + item.getItemName());
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadLostItems();
        showAlert("Refreshed", "Lost items list has been updated.");
    }

    @FXML
    private void handleBackToDashboard() {
        closeWindow();
    }

    private void closeWindow() {
        try {
            Stage currentStage = (Stage) itemsContainer.getScene().getWindow();
            currentStage.close();
            System.out.println("✅ Lost items window closed");
        } catch (Exception e) {
            System.err.println("❌ Error closing window: " + e.getMessage());
        }
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
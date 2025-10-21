package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AdminVerificationController {

    @FXML private TableView<LostFoundItem> itemsTable;

    @FXML private TableColumn<LostFoundItem, String> itemNameCol;
    @FXML private TableColumn<LostFoundItem, String> categoryCol;
    @FXML private TableColumn<LostFoundItem, String> descriptionCol;
    @FXML private TableColumn<LostFoundItem, String> locationCol;
    @FXML private TableColumn<LostFoundItem, String> reportedByCol;
    @FXML private TableColumn<LostFoundItem, String> statusCol;
    @FXML private TableColumn<LostFoundItem, String> dateCol;
    @FXML private TableColumn<LostFoundItem, String> verificationCol;

    @FXML private TextArea verificationNotes;
    @FXML private Label adminWelcomeLabel;
    @FXML private Label statsLabel;

    private ItemService itemService;
    private UserService userService;
    private User currentAdmin;
    private ObservableList<LostFoundItem> pendingItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        currentAdmin = userService.getCurrentUser();

        if (currentAdmin != null && currentAdmin.isAdmin()) {
            adminWelcomeLabel.setText("Admin Verification Panel - Welcome, " + currentAdmin.getFirstName() + " " + currentAdmin.getLastName());
            setupTable();
            loadPendingVerificationItems();
            updateStats();
        } else {
            showError("Access Denied", "You don't have administrator privileges to access this panel.");
            closeWindow();
        }
    }

    private void setupTable() {
        // Set up cell value factories
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        reportedByCol.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Custom cell factory for verification status
        verificationCol.setCellValueFactory(new PropertyValueFactory<>("verified"));
        verificationCol.setCellFactory(col -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String verified, boolean empty) {
                super.updateItem(verified, empty);
                if (empty || verified == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    if (item.isVerified()) {
                        setText("✓ Verified");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("⏳ Pending");
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Add custom styling for different statuses
        statusCol.setCellFactory(col -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                    switch (status.toLowerCase()) {
                        case "lost":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "found":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "returned":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadPendingVerificationItems() {
        pendingItems = FXCollections.observableArrayList(
                itemService.getItemsPendingVerification()
        );
        itemsTable.setItems(pendingItems);

        System.out.println("📋 Loaded " + pendingItems.size() + " items pending verification");

        // Update selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> onItemSelected(newSelection)
        );
    }

    private void onItemSelected(LostFoundItem item) {
        if (item != null) {
            verificationNotes.setPromptText("Add verification notes for: " + item.getItemName());
            verificationNotes.setDisable(false);
        } else {
            verificationNotes.setPromptText("Select an item to add verification notes...");
            verificationNotes.setDisable(true);
        }
    }

    private void updateStats() {
        int pendingCount = itemService.getPendingVerificationCount();
        int verifiedCount = itemService.getVerifiedItemsCount();
        int totalItems = itemService.getAllItems().size();

        statsLabel.setText(String.format(
                "Statistics: %d pending verification | %d verified | %d total items",
                pendingCount, verifiedCount, totalItems
        ));
    }

    @FXML
    private void handleVerifyItem() {
        LostFoundItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Confirm verification
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Verification");
            confirmAlert.setHeaderText("Verify Item: " + selectedItem.getItemName());
            confirmAlert.setContentText("Are you sure you want to verify this item? This action cannot be undone.");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = itemService.verifyItem(
                            selectedItem.getItemName(),
                            currentAdmin.getUsername()
                    );

                    if (success) {
                        String notes = verificationNotes.getText().trim();
                        if (!notes.isEmpty()) {
                            System.out.println("📝 Verification notes: " + notes);
                            // You could save these notes to the item if you add a notes field
                        }

                        showAlert("Verification Successful",
                                "Item '" + selectedItem.getItemName() + "' has been verified successfully!\n" +
                                        "Verified by: " + currentAdmin.getFirstName() + " " + currentAdmin.getLastName());

                        // Refresh the list and stats
                        loadPendingVerificationItems();
                        updateStats();
                        verificationNotes.clear();

                        // Refresh dashboard if it's open
                        refreshDashboardIfOpen();
                    } else {
                        showError("Verification Failed", "Could not verify the selected item. Please try again.");
                    }
                }
            });
        } else {
            showAlert("No Selection", "Please select an item to verify.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleViewDetails() {
        LostFoundItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            StringBuilder details = new StringBuilder();
            details.append("=== ITEM DETAILS ===\n\n");
            details.append("Name: ").append(selectedItem.getItemName()).append("\n");
            details.append("Category: ").append(selectedItem.getCategory()).append("\n");
            details.append("Description: ").append(selectedItem.getDescription()).append("\n");
            details.append("Location: ").append(selectedItem.getLocation()).append("\n");
            details.append("Date Reported: ").append(selectedItem.getDate()).append("\n");
            details.append("Status: ").append(selectedItem.getStatusDisplay()).append("\n");
            details.append("Reported By: ").append(selectedItem.getReportedBy()).append("\n");
            details.append("Unique ID: ").append(selectedItem.getUniqueId()).append("\n\n");

            details.append("=== VERIFICATION STATUS ===\n");
            if (selectedItem.isVerified()) {
                details.append("✅ VERIFIED\n");
                details.append("Verified By: ").append(selectedItem.getVerifiedBy()).append("\n");
                details.append("Verification Date: ").append(selectedItem.getVerificationDate()).append("\n");
            } else {
                details.append("⏳ PENDING VERIFICATION\n");
                details.append("This item is waiting for admin verification.\n");
            }

            TextArea textArea = new TextArea(details.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(600, 400);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Item Details");
            alert.setHeaderText("Detailed Information: " + selectedItem.getItemName());
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        } else {
            showAlert("No Selection", "Please select an item to view details.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRefresh() {
        loadPendingVerificationItems();
        updateStats();
        showAlert("List Refreshed", "Verification list has been updated with the latest data.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewVerifiedItems() {
        ObservableList<LostFoundItem> verifiedItems = FXCollections.observableArrayList(
                itemService.getVerifiedItems()
        );

        if (verifiedItems.isEmpty()) {
            showAlert("No Verified Items", "There are no verified items in the system yet.", Alert.AlertType.INFORMATION);
            return;
        }

        // Create a temporary table to show verified items
        TableView<LostFoundItem> verifiedTable = new TableView<>();
        verifiedTable.setItems(verifiedItems);

        // Create columns (similar to main table)
        TableColumn<LostFoundItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<LostFoundItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<LostFoundItem, String> verifiedByCol = new TableColumn<>("Verified By");
        verifiedByCol.setCellValueFactory(new PropertyValueFactory<>("verifiedBy"));

        TableColumn<LostFoundItem, String> dateCol = new TableColumn<>("Verification Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("verificationDate"));

        verifiedTable.getColumns().addAll(nameCol, statusCol, verifiedByCol, dateCol);

        ScrollPane scrollPane = new ScrollPane(verifiedTable);
        scrollPane.setPrefSize(600, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Verified Items");
        alert.setHeaderText("All Verified Items (" + verifiedItems.size() + " items)");
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void refreshDashboardIfOpen() {
        // This method could be enhanced to actually find and refresh the dashboard
        System.out.println("🔄 Admin verification completed - dashboard should refresh");
    }

    private void closeWindow() {
        Stage stage = (Stage) itemsTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
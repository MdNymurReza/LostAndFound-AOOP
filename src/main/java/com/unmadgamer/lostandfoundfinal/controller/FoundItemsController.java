package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class FoundItemsController {

    @FXML
    private Label userNameLabel;

    @FXML
    private TableView<LostFoundItem> itemsTable;

    @FXML
    private TableColumn<LostFoundItem, String> colItemName;

    @FXML
    private TableColumn<LostFoundItem, String> colCategory;

    @FXML
    private TableColumn<LostFoundItem, String> colDate;

    @FXML
    private TableColumn<LostFoundItem, String> colLocation;

    @FXML
    private TableColumn<LostFoundItem, String> colReportedBy;

    @FXML
    private TableColumn<LostFoundItem, String> colVerification;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCombo;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> itemsData;
    private List<LostFoundItem> allFoundItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        itemsData = FXCollections.observableArrayList();

        // Set up table columns
        setupTableColumns();
        setupEventHandlers();

        itemsTable.setItems(itemsData);

        if (userService.getCurrentUser() != null) {
            loadFoundItems();
            userNameLabel.setText(userService.getCurrentUser().getFirstName() + " " + userService.getCurrentUser().getLastName());
        }
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colReportedBy.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));

        // FIXED: Use proper cell factory for verification status
        colVerification.setCellFactory(col -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    LostFoundItem lostFoundItem = getTableView().getItems().get(getIndex());
                    if (lostFoundItem.isVerified()) {
                        setText("✓ Verified");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("⏳ Pending");
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Add context menu for admin actions
        if (userService.getCurrentUser() != null && userService.getCurrentUser().isAdmin()) {
            setupContextMenu();
        }
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem verifyItem = new MenuItem("Verify Item");
        verifyItem.setOnAction(e -> verifySelectedItem());

        MenuItem viewDetails = new MenuItem("View Details");
        viewDetails.setOnAction(e -> showSelectedItemDetails());

        MenuItem markReturned = new MenuItem("Mark as Returned");
        markReturned.setOnAction(e -> handleMarkAsReturned());

        contextMenu.getItems().addAll(verifyItem, viewDetails, markReturned);
        itemsTable.setContextMenu(contextMenu);
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());

        // Initialize filter combo
        filterCombo.getItems().addAll("All Items", "Verified Only", "Pending Verification");
        filterCombo.setValue("All Items");
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void loadFoundItems() {
        String currentUser = userService.getCurrentUser().getUsername();

        // If user is admin, show all found items. Otherwise, show only user's items
        if (userService.getCurrentUser().isAdmin()) {
            allFoundItems = itemService.getItemsByStatus("found");
        } else {
            allFoundItems = itemService.getUserItemsByStatus(currentUser, "found");
        }

        itemsData.setAll(allFoundItems);
        System.out.println("📋 Loaded " + allFoundItems.size() + " found items for user: " + currentUser);

        // Update table with admin actions if applicable
        updateTableForAdmin();
    }

    private void updateTableForAdmin() {
        if (userService.getCurrentUser().isAdmin()) {
            System.out.println("👑 Admin mode: Showing all found items in the system");
        }
    }

    private void filterItems() {
        String searchText = searchField.getText().toLowerCase();
        String filterValue = filterCombo.getValue();

        List<LostFoundItem> filteredItems = allFoundItems.stream()
                .filter(item ->
                        item.getItemName().toLowerCase().contains(searchText) ||
                                item.getCategory().toLowerCase().contains(searchText) ||
                                item.getLocation().toLowerCase().contains(searchText) ||
                                item.getReportedBy().toLowerCase().contains(searchText)
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
                .toList();

        itemsData.setAll(filteredItems);
    }

    private void verifySelectedItem() {
        LostFoundItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem.isVerified()) {
                showAlert("Already Verified", "This item is already verified.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Verification");
            confirmAlert.setHeaderText("Verify Item: " + selectedItem.getItemName());
            confirmAlert.setContentText("Are you sure you want to verify this found item?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = itemService.verifyItem(
                            selectedItem.getItemName(),
                            userService.getCurrentUser().getUsername()
                    );

                    if (success) {
                        showAlert("Verification Successful",
                                "Item '" + selectedItem.getItemName() + "' has been verified successfully!");
                        loadFoundItems(); // Refresh the list
                    } else {
                        showError("Verification Failed", "Could not verify the selected item.");
                    }
                }
            });
        } else {
            showAlert("No Selection", "Please select an item to verify.", Alert.AlertType.WARNING);
        }
    }

    private void showSelectedItemDetails() {
        LostFoundItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            showItemDetails(selectedItem);
        } else {
            showAlert("No Selection", "Please select an item to view details.", Alert.AlertType.WARNING);
        }
    }

    private void showItemDetails(LostFoundItem item) {
        StringBuilder details = new StringBuilder();
        details.append("=== FOUND ITEM DETAILS ===\n\n");
        details.append("Item Name: ").append(item.getItemName()).append("\n");
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Description: ").append(item.getDescription()).append("\n");
        details.append("Location: ").append(item.getLocation()).append("\n");
        details.append("Date Found: ").append(item.getDate()).append("\n");
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
        alert.setHeaderText("Found Item: " + item.getItemName());
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refresh button clicked - Found Items");
        loadFoundItems();
        showAlert("Refreshed", "Found items list has been refreshed.");
    }

    @FXML
    private void handleBackToDashboard() {
        closeWindow();
    }

    @FXML
    private void handleMarkAsReturned() {
        LostFoundItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (!selectedItem.isVerified()) {
                showError("Cannot Mark Returned", "This item must be verified by an admin before it can be marked as returned.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Mark as Returned");
            confirmAlert.setHeaderText("Mark Item as Returned: " + selectedItem.getItemName());
            confirmAlert.setContentText("Are you sure you want to mark this item as returned to its owner?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = itemService.updateItemStatus(
                            selectedItem.getItemName(),
                            "returned",
                            userService.getCurrentUser().getUsername()
                    );

                    if (success) {
                        showAlert("Success", "Item '" + selectedItem.getItemName() + "' has been marked as returned!");
                        loadFoundItems(); // Refresh the list
                    } else {
                        showError("Failed", "Could not mark the item as returned.");
                    }
                }
            });
        } else {
            showAlert("No Selection", "Please select an item to mark as returned.", Alert.AlertType.WARNING);
        }
    }

    private void closeWindow() {
        try {
            Stage currentStage = (Stage) userNameLabel.getScene().getWindow();
            currentStage.close();
            System.out.println("✅ Found items window closed");
        } catch (Exception e) {
            System.err.println("❌ Error closing window: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
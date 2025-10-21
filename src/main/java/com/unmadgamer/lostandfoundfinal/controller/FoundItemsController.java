package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FoundItemsController {

    @FXML private TableView<LostFoundItem> itemsTable;
    @FXML private TableColumn<LostFoundItem, String> colItemName;
    @FXML private TableColumn<LostFoundItem, String> colCategory;
    @FXML private TableColumn<LostFoundItem, String> colDescription;
    @FXML private TableColumn<LostFoundItem, String> colLocation;
    @FXML private TableColumn<LostFoundItem, String> colDate;
    @FXML private TableColumn<LostFoundItem, String> colStatus;
    @FXML private TableColumn<LostFoundItem, String> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label welcomeLabel;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> allItems;
    private ObservableList<LostFoundItem> filteredItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        initializeTable();
        setupFilters();
        setupClaimFunctionality();
        loadItems();
        updateWelcomeMessage();
    }

    private void initializeTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Actions column with Claim button
        colActions.setCellFactory(param -> new TableCell<LostFoundItem, String>() {
            private final Button claimBtn = new Button("Claim");
            {
                claimBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                claimBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    handleClaimItem(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LostFoundItem currentItem = getTableView().getItems().get(getIndex());
                    // Only show claim button for verified items that aren't already returned
                    if (currentItem.isVerified() && !"returned".equals(currentItem.getStatus())) {
                        claimBtn.setText("Claim");
                        claimBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                        setGraphic(claimBtn);
                    } else if ("returned".equals(currentItem.getStatus())) {
                        claimBtn.setText("Returned");
                        claimBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                        claimBtn.setDisable(true);
                        setGraphic(claimBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        itemsTable.setItems(filteredItems);
    }

    private void setupFilters() {
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Electronics", "Documents", "Clothing", "Accessories", "Bags", "Books", "Other"
        ));
        categoryFilter.setValue("All");

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void setupClaimFunctionality() {
        // Double-click to view details and claim
        itemsTable.setRowFactory(tv -> {
            TableRow<LostFoundItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    LostFoundItem item = row.getItem();
                    showItemDetails(item);
                }
            });
            return row;
        });
    }

    private void loadItems() {
        List<LostFoundItem> items = itemService.getAvailableFoundItems();
        allItems.setAll(items);
        filteredItems.setAll(items);

        System.out.println("✅ Loaded " + items.size() + " found items for user");
    }

    private void filterItems() {
        String searchText = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();

        List<LostFoundItem> filtered = allItems.stream()
                .filter(item ->
                        item.getItemName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText) ||
                                item.getLocation().toLowerCase().contains(searchText))
                .filter(item -> category.equals("All") || item.getCategory().equals(category))
                .collect(Collectors.toList());

        filteredItems.setAll(filtered);
    }

    private void updateWelcomeMessage() {
        String username = userService.getCurrentUser().getUsername();
        welcomeLabel.setText("Welcome, " + username + "! Browse found items below.");
    }

    @FXML
    private void handleClaimItem(LostFoundItem item) {
        String claimant = userService.getCurrentUser().getUsername();

        // Check if item is already returned
        if ("returned".equals(item.getStatus())) {
            showAlert("Already Returned",
                    "This item has already been returned to its owner!",
                    Alert.AlertType.WARNING);
            return;
        }

        // Check if user is trying to claim their own found item
        if (item.getReportedBy().equals(claimant)) {
            showAlert("Cannot Claim",
                    "You cannot claim an item you reported as found!",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Claim Found Item");
        confirmAlert.setHeaderText("Claim '" + item.getItemName() + "'?");
        confirmAlert.setContentText(
                "Are you sure this is your lost item?\n\n" +
                        "Item Details:\n" +
                        "• Name: " + item.getItemName() + "\n" +
                        "• Category: " + item.getCategory() + "\n" +
                        "• Location Found: " + item.getLocation() + "\n" +
                        "• Date Found: " + item.getDate() + "\n\n" +
                        "You will need to provide proof of ownership. Admin verification will be required."
        );

        confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (confirmAlert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (itemService.claimItem(item.getId(), claimant)) {
                showAlert("Claim Submitted",
                        "Your claim request has been submitted successfully!\n\n" +
                                "Please wait for admin approval. You will need to provide proof of ownership to receive the item.",
                        Alert.AlertType.INFORMATION);
                loadItems(); // Refresh the table
            } else {
                showAlert("Claim Failed",
                        "Failed to submit claim request. Please try again.",
                        Alert.AlertType.ERROR);
            }
        }
    }

    private void showItemDetails(LostFoundItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getItemName());

        String details =
                "Category: " + item.getCategory() + "\n" +
                        "Description: " + item.getDescription() + "\n" +
                        "Location Found: " + item.getLocation() + "\n" +
                        "Date Found: " + item.getDate() + "\n" +
                        "Reported by: " + item.getReportedBy() + "\n" +
                        "Status: " + getStatusDisplay(item.getStatus()) + "\n" +
                        "Verification: " + getVerificationStatus(item) + "\n\n";

        // Add FoundItem specific details
        if (item instanceof FoundItem) {
            FoundItem foundItem = (FoundItem) item;
            details += "Storage Location: " + foundItem.getStorageLocation() + "\n";
            if (foundItem.getClaimedBy() != null) {
                details += "Claimed by: " + foundItem.getClaimedBy() + "\n";
                details += "Claim Status: " + foundItem.getClaimStatus() + "\n";
            }
        }

        alert.setContentText(details);
        alert.showAndWait();
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case "pending": return "⏳ Pending Verification";
            case "verified": return "✅ Verified - Available for Claim";
            case "returned": return "📦 Returned to Owner";
            case "rejected": return "❌ Rejected";
            default: return status;
        }
    }

    private String getVerificationStatus(LostFoundItem item) {
        if (item.isVerified()) {
            return "✅ Verified by " + item.getVerifiedBy();
        } else if (item.isRejected()) {
            return "❌ Rejected by " + item.getVerifiedBy();
        } else {
            return "⏳ Pending Admin Verification";
        }
    }

    @FXML
    private void handleRefresh() {
        loadItems();
        showAlert("Refreshed", "Found items list has been updated.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSearch() {
        filterItems();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All");
        filteredItems.setAll(allItems);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) itemsTable.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Cannot open dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReportFoundItem() {
        try {
            Stage currentStage = (Stage) itemsTable.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/found-form.fxml"));
            Parent root = loader.load();

            Stage formStage = new Stage();
            formStage.setTitle("Report Found Item - Lost and Found System");
            formStage.setScene(new Scene(root, 600, 700));
            formStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Cannot open found item form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
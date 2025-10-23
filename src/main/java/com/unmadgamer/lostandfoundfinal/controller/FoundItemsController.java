package com.unmadgamer.lostandfoundfinal.controller;

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
import javafx.util.Callback;

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
    @FXML private TableColumn<LostFoundItem, Void> colAction;

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

        // Add action column with claim button
        colAction.setCellFactory(createActionCellFactory());

        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        itemsTable.setItems(filteredItems);
    }

    private Callback<TableColumn<LostFoundItem, Void>, TableCell<LostFoundItem, Void>> createActionCellFactory() {
        return param -> new TableCell<LostFoundItem, Void>() {
            private final Button claimButton = new Button("Claim");

            {
                claimButton.setOnAction((event) -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    handleClaimItem(item);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LostFoundItem currentItem = getTableView().getItems().get(getIndex());
                    // Only show claim button if item can be claimed
                    if (currentItem != null && currentItem.isVerified() &&
                            "active".equalsIgnoreCase(currentItem.getStatus()) &&
                            !currentItem.getReportedBy().equals(userService.getCurrentUser().getUsername())) {
                        claimButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        setGraphic(claimButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };
    }

    private void setupFilters() {
        // Initialize category filter with all options
        ObservableList<String> categories = FXCollections.observableArrayList(
                "All", "Electronics", "Documents", "Clothing", "Accessories", "Other"
        );
        categoryFilter.setItems(categories);
        categoryFilter.setValue("All");

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void loadItems() {
        itemService.refreshItems(); // Force refresh from JSON
        List<LostFoundItem> items = itemService.getAvailableFoundItems();
        if (items != null) {
            allItems.setAll(items);
            filteredItems.setAll(items);
            System.out.println("✅ Loaded " + items.size() + " available found items");

            // Debug: Print each item
            for (LostFoundItem item : items) {
                System.out.println("   - " + item.getItemName() + " | " + item.getStatus() + " | " + item.getReportedBy());
            }
        } else {
            allItems.clear();
            filteredItems.clear();
            System.out.println("⚠️ No found items available");
        }
    }

    private void filterItems() {
        if (allItems == null || allItems.isEmpty()) {
            return;
        }

        String searchText = searchField.getText().toLowerCase();
        String category = categoryFilter.getValue();

        List<LostFoundItem> filtered = allItems.stream()
                .filter(item -> item != null &&
                        (item.getItemName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText) ||
                                item.getLocation().toLowerCase().contains(searchText)))
                .filter(item -> category == null || "All".equals(category) || item.getCategory().equals(category))
                .collect(Collectors.toList());

        filteredItems.setAll(filtered);
    }

    private void updateWelcomeMessage() {
        if (userService.getCurrentUser() != null) {
            String username = userService.getCurrentUser().getUsername();
            welcomeLabel.setText("Welcome, " + username + "! Browse found items below.");
        }
    }

    @FXML
    private void handleClaimItem(LostFoundItem item) {
        if (item == null || userService.getCurrentUser() == null) {
            showAlert("Error", "Invalid item or user.", Alert.AlertType.ERROR);
            return;
        }

        String currentUser = userService.getCurrentUser().getUsername();

        // Check if user is trying to claim their own item
        if (item.getReportedBy().equals(currentUser)) {
            showAlert("Cannot Claim Item", "You cannot claim your own found item.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Claim Item");
        confirmation.setHeaderText("Claim Found Item");
        confirmation.setContentText("Are you sure this is your lost item: " + item.getItemName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (itemService.claimItem(item.getId(), currentUser)) {
                    showAlert("Claim Submitted", "Your claim has been submitted for admin approval.", Alert.AlertType.INFORMATION);
                    loadItems(); // Refresh the list
                } else {
                    showAlert("Claim Failed", "Unable to claim this item. It may have been already claimed.", Alert.AlertType.ERROR);
                }
            }
        });
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
        if (allItems != null) {
            filteredItems.setAll(allItems);
        }
    }

    @FXML
    private void handleBackToDashboard() {
        System.out.println("Clicked: Back to Dashboard from Found Items");

        try {
            // Close current found items window
            Stage currentStage = (Stage) itemsTable.getScene().getWindow();
            currentStage.close();

            // Check if dashboard is already open
            boolean dashboardExists = false;
            for (Stage stage : Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage)
                    .map(window -> (Stage) window)
                    .collect(Collectors.toList())) {
                if (stage.getTitle() != null && stage.getTitle().contains("Dashboard")) {
                    dashboardExists = true;
                    stage.toFront(); // Bring to front if exists
                    break;
                }
            }

            // Only open new dashboard if none exists
            if (!dashboardExists) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
                Parent root = loader.load();

                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("Dashboard - Lost and Found System");
                dashboardStage.setScene(new Scene(root, 750, 600));
                dashboardStage.show();
            }

        } catch (IOException e) {
            System.err.println("❌ Error navigating to dashboard: " + e.getMessage());
            showAlert("Navigation Error", "Cannot open dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
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
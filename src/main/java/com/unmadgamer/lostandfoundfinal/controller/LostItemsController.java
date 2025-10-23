package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
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

public class LostItemsController {

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

        // Add action column with view details button (NO CLAIM BUTTON)
        colAction.setCellFactory(createActionCellFactory());

        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        itemsTable.setItems(filteredItems);
    }

    private Callback<TableColumn<LostFoundItem, Void>, TableCell<LostFoundItem, Void>> createActionCellFactory() {
        return param -> new TableCell<LostFoundItem, Void>() {
            private final Button viewDetailsButton = new Button("View Details");

            {
                viewDetailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                viewDetailsButton.setOnAction((event) -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    showItemDetails(item);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LostFoundItem currentItem = getTableView().getItems().get(getIndex());
                    if (currentItem != null) {
                        setGraphic(viewDetailsButton);
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
        List<LostFoundItem> items = itemService.getAvailableLostItems();
        if (items != null) {
            allItems.setAll(items);
            filteredItems.setAll(items);
            System.out.println("✅ Loaded " + items.size() + " available lost items");

            // Debug: Print each item
            for (LostFoundItem item : items) {
                System.out.println("   - " + item.getItemName() + " | " + item.getStatus() + " | " + item.getReportedBy());
            }
        } else {
            allItems.clear();
            filteredItems.clear();
            System.out.println("⚠️ No lost items available");
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
            welcomeLabel.setText("Welcome, " + username + "! Browse lost items below.");
        }
    }

    private void showItemDetails(LostFoundItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getItemName());

        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(item.getType()).append("\n");
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Description: ").append(item.getDescription()).append("\n");
        details.append("Location: ").append(item.getLocation()).append("\n");
        details.append("Date: ").append(item.getDate()).append("\n");
        details.append("Status: ").append(item.getStatus()).append("\n");
        details.append("Verification: ").append(item.getVerificationStatus()).append("\n");

        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            details.append("\n=== LOST ITEM DETAILS ===\n");
            details.append("Lost Date: ").append(lostItem.getLostDate()).append("\n");
            details.append("Reward: ").append(lostItem.getReward() != null ? lostItem.getReward() : "Not specified").append("\n");
            details.append("Contact Info: ").append(lostItem.getContactInfo()).append("\n");
            details.append("Reported By: ").append(lostItem.getReportedBy()).append("\n");
        }

        alert.setContentText(details.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(400, 400);
        alert.showAndWait();
    }

    @FXML
    private void handleReportLostItem() {
        try {
            Stage currentStage = (Stage) itemsTable.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/lost-form.fxml"));
            Parent root = loader.load();

            Stage formStage = new Stage();
            formStage.setTitle("Report Lost Item - Lost and Found System");
            formStage.setScene(new Scene(root, 600, 700));
            formStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Cannot open lost item form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadItems();
        showAlert("Refreshed", "Lost items list has been updated.", Alert.AlertType.INFORMATION);
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
        System.out.println("Clicked: Back to Dashboard from Lost Items");

        try {
            // Close current lost items window
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
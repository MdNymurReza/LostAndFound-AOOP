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

        // Add action column with claim button
        colAction.setCellFactory(createActionCellFactory());

        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        itemsTable.setItems(filteredItems);
    }

    private Callback<TableColumn<LostFoundItem, Void>, TableCell<LostFoundItem, Void>> createActionCellFactory() {
        return new Callback<TableColumn<LostFoundItem, Void>, TableCell<LostFoundItem, Void>>() {
            @Override
            public TableCell<LostFoundItem, Void> call(final TableColumn<LostFoundItem, Void> param) {
                return new TableCell<LostFoundItem, Void>() {
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
                            if (currentItem.isVerified() && currentItem.isActive()) {
                                claimButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                                setGraphic(claimButton);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }

    private void setupFilters() {
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Electronics", "Documents", "Clothing", "Accessories", "Other"
        ));
        categoryFilter.setValue("All");

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void loadItems() {
        itemService.refreshItems(); // Force refresh from JSON
        List<LostFoundItem> items = itemService.getAvailableLostItems();
        allItems.setAll(items);
        filteredItems.setAll(items);
        System.out.println("✅ Loaded " + items.size() + " available lost items");

        // Debug: Print each item
        for (LostFoundItem item : items) {
            System.out.println("   - " + item.getItemName() + " | " + item.getStatus() + " | " + item.getReportedBy());
        }
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
        welcomeLabel.setText("Welcome, " + username + "! Browse lost items below.");
    }

    @FXML
    private void handleClaimItem(LostFoundItem item) {
        String currentUser = userService.getCurrentUser().getUsername();

        // Check if user is trying to claim their own item
        if (item.getReportedBy().equals(currentUser)) {
            showAlert("Cannot Claim Item", "You cannot claim your own lost item.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Claim Item");
        confirmation.setHeaderText("Claim Lost Item");
        confirmation.setContentText("Are you sure you want to claim this item: " + item.getItemName() + "?");

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
            dashboardStage.setScene(new Scene(root, 800, 600));
            dashboardStage.show();
        } catch (IOException e) {
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
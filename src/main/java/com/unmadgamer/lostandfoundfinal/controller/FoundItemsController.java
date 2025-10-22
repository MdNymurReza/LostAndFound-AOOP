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

        allItems = FXCollections.observableArrayList();
        filteredItems = FXCollections.observableArrayList();
        itemsTable.setItems(filteredItems);
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
        List<LostFoundItem> items = itemService.getAvailableFoundItems();
        allItems.setAll(items);
        filteredItems.setAll(items);
        System.out.println("✅ Loaded " + items.size() + " available found items");
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
            dashboardStage.setScene(new Scene(root, 600, 400));
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
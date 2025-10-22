package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.model.FoundItem;
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

public class ReturnedItemsController {

    @FXML private TableView<LostFoundItem> returnedTable;
    @FXML private TableColumn<LostFoundItem, String> colItemName;
    @FXML private TableColumn<LostFoundItem, String> colCategory;
    @FXML private TableColumn<LostFoundItem, String> colType;
    @FXML private TableColumn<LostFoundItem, String> colReturnedTo;
    @FXML private TableColumn<LostFoundItem, String> colReturnedBy;
    @FXML private TableColumn<LostFoundItem, String> colDate;
    @FXML private TableColumn<LostFoundItem, String> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label statsLabel;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> returnedItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        initializeTable();
        setupFilters();
        loadReturnedItems();
        updateStatistics();
    }

    private void initializeTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Custom cell factories for returned to and returned by
        colReturnedTo.setCellFactory(param -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    LostFoundItem lostFoundItem = getTableView().getItems().get(getIndex());
                    if (lostFoundItem instanceof LostItem) {
                        LostItem lostItem = (LostItem) lostFoundItem;
                        setText(lostItem.getClaimedBy() != null ? lostItem.getClaimedBy() : "N/A");
                    } else if (lostFoundItem instanceof FoundItem) {
                        FoundItem foundItem = (FoundItem) lostFoundItem;
                        setText(foundItem.getReportedBy() != null ? foundItem.getReportedBy() : "N/A");
                    } else {
                        setText("N/A");
                    }
                }
            }
        });

        colReturnedBy.setCellFactory(param -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    LostFoundItem lostFoundItem = getTableView().getItems().get(getIndex());
                    if (lostFoundItem instanceof LostItem) {
                        setText(lostFoundItem.getReportedBy());
                    } else if (lostFoundItem instanceof FoundItem) {
                        FoundItem foundItem = (FoundItem) lostFoundItem;
                        setText(foundItem.getClaimedBy() != null ? foundItem.getClaimedBy() : "N/A");
                    } else {
                        setText("N/A");
                    }
                }
            }
        });

        // Actions column
        colActions.setCellFactory(param -> new TableCell<LostFoundItem, String>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                viewBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    showItemDetails(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        returnedItems = FXCollections.observableArrayList();
        returnedTable.setItems(returnedItems);
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "All", "Lost", "Found"
        ));
        typeFilter.setValue("All");

        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Electronics", "Documents", "Clothing", "Accessories", "Other"
        ));
        categoryFilter.setValue("All");

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
        categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());
    }

    private void loadReturnedItems() {
        // Get all returned items (both lost and found)
        List<LostFoundItem> returnedLostItems = itemService.getLostItems().stream()
                .filter(item -> "returned".equals(item.getStatus()) || "claimed".equals(item.getStatus()))
                .collect(Collectors.toList());

        List<LostFoundItem> returnedFoundItems = itemService.getFoundItems().stream()
                .filter(item -> "returned".equals(item.getStatus()) || "claimed".equals(item.getStatus()))
                .collect(Collectors.toList());

        returnedItems.setAll(returnedLostItems);
        returnedItems.addAll(returnedFoundItems);

        System.out.println("✅ Loaded " + returnedItems.size() + " returned items");
    }

    private void filterItems() {
        String searchText = searchField.getText().toLowerCase();
        String type = typeFilter.getValue();
        String category = categoryFilter.getValue();

        List<LostFoundItem> filtered = returnedItems.stream()
                .filter(item ->
                        item.getItemName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText))
                .filter(item -> {
                    if ("All".equals(type)) return true;
                    if ("Lost".equals(type)) return item instanceof LostItem;
                    if ("Found".equals(type)) return item instanceof FoundItem;
                    return true;
                })
                .filter(item -> category.equals("All") || item.getCategory().equals(category))
                .collect(Collectors.toList());

        returnedTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateStatistics() {
        int totalReturned = returnedItems.size();
        long lostReturned = returnedItems.stream().filter(item -> item instanceof LostItem).count();
        long foundReturned = returnedItems.stream().filter(item -> item instanceof FoundItem).count();

        statsLabel.setText(String.format("📊 Statistics: %d Total Returned (%d Lost, %d Found)",
                totalReturned, lostReturned, foundReturned));
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

        // Add type-specific details
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            details.append("\n=== LOST ITEM DETAILS ===\n");
            details.append("Lost Date: ").append(lostItem.getLostDate()).append("\n");
            details.append("Reward: ").append(lostItem.getReward() != null ? lostItem.getReward() : "Not specified").append("\n");
            details.append("Contact Info: ").append(lostItem.getContactInfo() != null ? lostItem.getContactInfo() : "N/A").append("\n");
            details.append("Reported By: ").append(lostItem.getReportedBy()).append("\n");

            if (lostItem.getClaimedBy() != null) {
                details.append("Returned To: ").append(lostItem.getClaimedBy()).append("\n");
                details.append("Claim Status: ").append(lostItem.getClaimStatus()).append("\n");
            }
        } else if (item instanceof FoundItem) {
            FoundItem foundItem = (FoundItem) item;
            details.append("\n=== FOUND ITEM DETAILS ===\n");
            details.append("Found Date: ").append(foundItem.getFoundDate()).append("\n");
            details.append("Storage Location: ").append(foundItem.getStorageLocation()).append("\n");
            details.append("Contact Info: ").append(foundItem.getContactInfo() != null ? foundItem.getContactInfo() : "N/A").append("\n");
            details.append("Found By: ").append(foundItem.getReportedBy()).append("\n");

            if (foundItem.getClaimedBy() != null) {
                details.append("Claimed By: ").append(foundItem.getClaimedBy()).append("\n");
                details.append("Claim Status: ").append(foundItem.getClaimStatus()).append("\n");
            }
        }

        if (item.isVerified()) {
            details.append("\nVerified By: ").append(item.getVerifiedBy()).append("\n");
            details.append("Verification Date: ").append(item.getVerificationDate()).append("\n");
        }

        alert.setContentText(details.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(400, 500);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadReturnedItems();
        updateStatistics();
        showAlert("Refreshed", "Returned items list has been updated.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleSearch() {
        filterItems();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        typeFilter.setValue("All");
        categoryFilter.setValue("All");
        returnedTable.setItems(returnedItems);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) returnedTable.getScene().getWindow();
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

    @FXML
    private void handleExportReport() {
        showAlert("Export Feature",
                "Export feature will be implemented in the next version.\n\n" +
                        "This will allow you to export returned items data to CSV or PDF format.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewMyRewards() {
        showAlert("Rewards Feature",
                "Rewards system will be implemented in the next version.\n\n" +
                        "You will be able to view your reward points and redemption options here.",
                Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
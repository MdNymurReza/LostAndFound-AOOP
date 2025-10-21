package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AdminVerificationDashboardController {

    @FXML private Label adminWelcomeLabel;
    @FXML private Label pendingVerificationLabel;
    @FXML private Label verifiedTodayLabel;
    @FXML private Label totalVerifiedLabel;
    @FXML private Label verificationRateLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private DatePicker dateFilter;

    @FXML private TableView<LostFoundItem> pendingTable;
    @FXML private TableColumn<LostFoundItem, String> colPendingItemName;
    @FXML private TableColumn<LostFoundItem, String> colPendingCategory;
    @FXML private TableColumn<LostFoundItem, String> colPendingDate;
    @FXML private TableColumn<LostFoundItem, String> colPendingReportedBy;
    @FXML private TableColumn<LostFoundItem, String> colPendingActions;

    @FXML private TableView<LostFoundItem> recentVerifiedTable;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedItemName;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedCategory;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedDate;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedBy;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedActions;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> pendingItems;
    private ObservableList<LostFoundItem> verifiedItems;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        initializeTables();
        setupFilters();
        updateStatistics();
        loadData();

        adminWelcomeLabel.setText("Admin Verification Dashboard - Welcome " +
                userService.getCurrentUser().getUsername() + "!");
    }

    private void initializeTables() {
        // Pending table setup
        colPendingItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colPendingCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPendingDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colPendingReportedBy.setCellValueFactory(new PropertyValueFactory<>("reportedBy"));

        // Actions column for pending items
        colPendingActions.setCellFactory(param -> new TableCell<>() {
            private final Button verifyBtn = new Button("Verify");
            private final Button rejectBtn = new Button("Reject");
            private final HBox buttons = new HBox(5, verifyBtn, rejectBtn);

            {
                verifyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                verifyBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    verifyItem(item);
                });

                rejectBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    rejectItem(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        // Verified table setup
        colVerifiedItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colVerifiedCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colVerifiedDate.setCellValueFactory(new PropertyValueFactory<>("verificationDate"));
        colVerifiedBy.setCellValueFactory(new PropertyValueFactory<>("verifiedBy"));

        colVerifiedActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                viewBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    viewItemDetails(item);
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

        pendingItems = FXCollections.observableArrayList();
        verifiedItems = FXCollections.observableArrayList();

        pendingTable.setItems(pendingItems);
        recentVerifiedTable.setItems(verifiedItems);
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Verified", "Rejected"
        ));
        statusFilter.setValue("All");

        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Electronics", "Documents", "Clothing", "Accessories", "Other"
        ));
        categoryFilter.setValue("All");
    }

    private void loadData() {
        // Load pending verification items
        List<LostFoundItem> pendingList = itemService.getPendingVerificationItems();
        pendingItems.setAll(pendingList);

        // Load recently verified items (last 7 days)
        List<LostFoundItem> verifiedList = itemService.getVerifiedItems();
        verifiedItems.setAll(verifiedList.stream()
                .limit(10) // Show only recent 10 items
                .collect(java.util.stream.Collectors.toList()));
    }

    private void verifyItem(LostFoundItem item) {
        String adminUsername = userService.getCurrentUser().getUsername();
        if (itemService.verifyItem(item.getId(), adminUsername)) {
            showAlert("Success", "Item verified successfully!", Alert.AlertType.INFORMATION);
            loadData();
            updateStatistics();
        } else {
            showAlert("Error", "Failed to verify item!", Alert.AlertType.ERROR);
        }
    }

    private void rejectItem(LostFoundItem item) {
        String adminUsername = userService.getCurrentUser().getUsername();
        if (itemService.rejectItem(item.getId(), adminUsername)) {
            showAlert("Success", "Item rejected!", Alert.AlertType.INFORMATION);
            loadData();
            updateStatistics();
        } else {
            showAlert("Error", "Failed to reject item!", Alert.AlertType.ERROR);
        }
    }

    private void viewItemDetails(LostFoundItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getItemName());
        alert.setContentText(
                "Category: " + item.getCategory() + "\n" +
                        "Description: " + item.getDescription() + "\n" +
                        "Location: " + item.getLocation() + "\n" +
                        "Reported by: " + item.getReportedBy() + "\n" +
                        "Status: " + item.getStatus() + "\n" +
                        "Verified by: " + item.getVerifiedBy() + "\n" +
                        "Verification Date: " + item.getVerificationDate()
        );
        alert.showAndWait();
    }

    private void updateStatistics() {
        pendingVerificationLabel.setText(String.valueOf(itemService.getPendingVerificationCount()));
        verifiedTodayLabel.setText(String.valueOf(itemService.getVerifiedTodayCount()));
        totalVerifiedLabel.setText(String.valueOf(itemService.getTotalVerifiedCount()));
        verificationRateLabel.setText(String.format("%.1f%%", itemService.getVerificationRate()));
    }

    @FXML
    private void handleBulkVerify() {
        List<LostFoundItem> selectedItems = pendingTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showAlert("Warning", "Please select items to verify!", Alert.AlertType.WARNING);
            return;
        }

        String adminUsername = userService.getCurrentUser().getUsername();
        int successCount = 0;

        for (LostFoundItem item : selectedItems) {
            if (itemService.verifyItem(item.getId(), adminUsername)) {
                successCount++;
            }
        }

        showAlert("Bulk Verify",
                "Successfully verified " + successCount + " out of " + selectedItems.size() + " items!",
                Alert.AlertType.INFORMATION);

        loadData();
        updateStatistics();
    }

    @FXML
    private void handleExportReport() {
        showAlert("Export", "Export feature will be implemented soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleRefresh() {
        loadData();
        updateStatistics();
        showAlert("Refresh", "Data refreshed successfully!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) pendingTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
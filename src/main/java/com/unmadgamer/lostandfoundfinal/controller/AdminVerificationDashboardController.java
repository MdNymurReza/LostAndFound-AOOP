package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class AdminVerificationDashboardController {

    @FXML private Label adminWelcomeLabel;
    @FXML private Label pendingVerificationLabel;
    @FXML private Label verifiedTodayLabel;
    @FXML private Label totalVerifiedLabel;
    @FXML private Label verificationRateLabel;
    @FXML private Label pendingClaimsLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> categoryFilter;

    @FXML private TableView<LostFoundItem> pendingTable;
    @FXML private TableColumn<LostFoundItem, String> colPendingItemName;
    @FXML private TableColumn<LostFoundItem, String> colPendingCategory;
    @FXML private TableColumn<LostFoundItem, String> colPendingDate;
    @FXML private TableColumn<LostFoundItem, String> colPendingReportedBy;
    @FXML private TableColumn<LostFoundItem, Void> colPendingActions;

    @FXML private TableView<LostFoundItem> recentVerifiedTable;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedItemName;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedCategory;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedDate;
    @FXML private TableColumn<LostFoundItem, String> colVerifiedBy;
    @FXML private TableColumn<LostFoundItem, Void> colVerifiedActions;

    // Claims Management Table
    @FXML private TableView<LostFoundItem> pendingClaimsTable;
    @FXML private TableColumn<LostFoundItem, String> colClaimItemName;
    @FXML private TableColumn<LostFoundItem, String> colClaimItemType;
    @FXML private TableColumn<LostFoundItem, String> colClaimClaimant;
    @FXML private TableColumn<LostFoundItem, String> colClaimDate;
    @FXML private TableColumn<LostFoundItem, Void> colClaimActions;

    // NEW: Add debug button
    @FXML private Button debugRewardBtn;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> pendingItems;
    private ObservableList<LostFoundItem> verifiedItems;
    private ObservableList<LostFoundItem> pendingClaimItems;

    @FXML
    public void initialize() {
        System.out.println("🔄 Initializing Admin Verification Dashboard...");

        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        initializeTables();
        setupFilters();
        updateStatistics();
        loadData();

        adminWelcomeLabel.setText("Admin Verification Dashboard - Welcome " +
                userService.getCurrentUser().getUsername() + "!");

        System.out.println("✅ Admin Verification Dashboard initialized successfully");
    }

    private void initializeTables() {
        System.out.println("🔄 Setting up tables...");

        // Pending verification table setup
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
                verifyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");

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
            protected void updateItem(Void item, boolean empty) {
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
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                viewBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    viewItemDetails(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });

        // Pending Claims table setup
        colClaimItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colClaimItemType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colClaimDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        colClaimClaimant.setCellFactory(param -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    LostFoundItem lostFoundItem = getTableView().getItems().get(getIndex());
                    if (lostFoundItem instanceof LostItem) {
                        setText(((LostItem) lostFoundItem).getClaimedBy());
                    } else if (lostFoundItem instanceof FoundItem) {
                        setText(((FoundItem) lostFoundItem).getClaimedBy());
                    } else {
                        setText("Unknown");
                    }
                }
            }
        });

        colClaimActions.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve Return");
            private final Button rejectBtn = new Button("Reject");
            private final Button debugBtn = new Button("Debug");
            private final HBox buttons = new HBox(5, approveBtn, rejectBtn, debugBtn);

            {
                approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                debugBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");

                approveBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    approveClaim(item);
                });

                rejectBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    rejectClaim(item);
                });

                debugBtn.setOnAction(event -> {
                    LostFoundItem item = getTableView().getItems().get(getIndex());
                    debugRewardFlow(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        pendingItems = FXCollections.observableArrayList();
        verifiedItems = FXCollections.observableArrayList();
        pendingClaimItems = FXCollections.observableArrayList();

        pendingTable.setItems(pendingItems);
        recentVerifiedTable.setItems(verifiedItems);
        pendingClaimsTable.setItems(pendingClaimItems);

        System.out.println("✅ Tables setup completed");
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
        System.out.println("🔄 Loading dashboard data...");

        try {
            // Load pending verification items
            List<LostFoundItem> pendingList = itemService.getPendingVerificationItems();
            pendingItems.setAll(pendingList);

            // Load recently verified items (last 10 items)
            List<LostFoundItem> verifiedList = itemService.getVerifiedItems();
            verifiedItems.setAll(verifiedList.stream()
                    .limit(10)
                    .collect(Collectors.toList()));

            // Load pending claim items
            List<LostFoundItem> claimList = itemService.getPendingClaimItems();
            pendingClaimItems.setAll(claimList);

            System.out.println("📊 Admin Dashboard Data Loaded:");
            System.out.println("   ⏳ Pending Verification: " + pendingList.size());
            System.out.println("   ✅ Verified Items: " + verifiedList.size());
            System.out.println("   📝 Pending Claims: " + claimList.size());

        } catch (Exception e) {
            System.err.println("❌ Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Data Load Error", "Failed to load dashboard data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    // Claim approval methods with FIXED REWARD SYSTEM
    private void approveClaim(LostFoundItem item) {
        String adminUsername = userService.getCurrentUser().getUsername();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Successful Return");
        confirmation.setHeaderText("Confirm Item Return");
        confirmation.setContentText("Are you sure this item has been successfully returned to its owner?\n\n" +
                "Item: " + item.getItemName() + "\n" +
                "Type: " + item.getType() + "\n" +
                "Reward: 50 points will be awarded\n" +
                "This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("🔄 Attempting to approve claim and award rewards...");
                if (itemService.approveClaim(item.getId(), adminUsername)) {
                    showAlert("Return Confirmed",
                            "✅ Item return has been confirmed!\n\n" +
                                    "🎁 50 reward points have been awarded\n" +
                                    "📦 Item marked as successfully returned\n" +
                                    "💎 Check user dashboard for updated rewards",
                            Alert.AlertType.INFORMATION);
                    loadData();
                    updateStatistics();

                    // Debug: Show current reward state
                    itemService.debugRewardSystem();
                } else {
                    showAlert("Error",
                            "Failed to confirm return. Please try again.\n\n" +
                                    "Check console for detailed error information.",
                            Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void rejectClaim(LostFoundItem item) {
        String adminUsername = userService.getCurrentUser().getUsername();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reject Claim");
        confirmation.setHeaderText("Reject Item Claim");
        confirmation.setContentText("Are you sure you want to reject this claim?\n\n" +
                "Item: " + item.getItemName() + "\n" +
                "The item will become available for claiming again.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (itemService.rejectClaim(item.getId(), adminUsername)) {
                    showAlert("Claim Rejected",
                            "Claim has been rejected successfully!\n\n" +
                                    "The item is now available for claiming again.",
                            Alert.AlertType.INFORMATION);
                    loadData();
                    updateStatistics();
                } else {
                    showAlert("Error", "Failed to reject claim!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // NEW: Debug reward flow for selected item
    private void debugRewardFlow(LostFoundItem item) {
        System.out.println("🔍 Debugging reward flow for: " + item.getItemName());
        itemService.debugRewardFlow(item.getId());
        showAlert("Debug Info",
                "Check console for detailed reward flow information.\n\n" +
                        "Item: " + item.getItemName() + "\n" +
                        "Type: " + item.getType(),
                Alert.AlertType.INFORMATION);
    }

    private void viewItemDetails(LostFoundItem item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Item Details");
        alert.setHeaderText(item.getItemName());

        StringBuilder details = new StringBuilder();
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Description: ").append(item.getDescription()).append("\n");
        details.append("Location: ").append(item.getLocation()).append("\n");
        details.append("Date: ").append(item.getDate()).append("\n");
        details.append("Reported by: ").append(item.getReportedBy()).append("\n");
        details.append("Status: ").append(item.getStatus()).append("\n");
        details.append("Verification: ").append(item.getVerificationStatus()).append("\n");

        if (item.isVerified()) {
            details.append("Verified by: ").append(item.getVerifiedBy()).append("\n");
            details.append("Verification Date: ").append(item.getVerificationDate()).append("\n");
        }

        // Add claim information if applicable
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            if (lostItem.getClaimedBy() != null) {
                details.append("\n=== CLAIM INFORMATION ===\n");
                details.append("Claimed by: ").append(lostItem.getClaimedBy()).append("\n");
                details.append("Claim Status: ").append(lostItem.getClaimStatus()).append("\n");
                if (lostItem.getClaimStatus().equals("approved")) {
                    details.append("✅ This item has been returned to its owner\n");
                    details.append("🎁 Reward points have been awarded\n");
                }
            }
        } else if (item instanceof FoundItem) {
            FoundItem foundItem = (FoundItem) item;
            if (foundItem.getClaimedBy() != null) {
                details.append("\n=== CLAIM INFORMATION ===\n");
                details.append("Claimed by: ").append(foundItem.getClaimedBy()).append("\n");
                details.append("Claim Status: ").append(foundItem.getClaimStatus()).append("\n");
                if (foundItem.getClaimStatus().equals("approved")) {
                    details.append("✅ This item has been returned to its owner\n");
                    details.append("🎁 Reward points have been awarded\n");
                }
            }
        }

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void updateStatistics() {
        pendingVerificationLabel.setText(String.valueOf(itemService.getPendingVerificationCount()));
        verifiedTodayLabel.setText(String.valueOf(itemService.getVerifiedTodayCount()));
        totalVerifiedLabel.setText(String.valueOf(itemService.getTotalVerifiedCount()));
        verificationRateLabel.setText(String.format("%.1f%%", itemService.getVerificationRate()));

        // Update pending claims count
        long pendingClaimsCount = itemService.getPendingClaimItems().size();
        pendingClaimsLabel.setText(String.valueOf(pendingClaimsCount));
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

    // NEW: Debug the entire reward system
    @FXML
    private void handleDebugRewardSystem() {
        itemService.debugRewardSystem();
        showAlert("Reward System Debug",
                "Check console for complete reward system status.\n\n" +
                        "This shows all users and their current reward points.",
                Alert.AlertType.INFORMATION);
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
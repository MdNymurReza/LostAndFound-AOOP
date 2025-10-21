package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class ReturnedItemsController {

    @FXML private VBox itemsContainer;
    @FXML private Label totalItemsLabel;
    @FXML private Label subtitleLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;

    private ItemService itemService;
    private UserService userService;
    private List<LostFoundItem> allReturnedItems;

    @FXML
    public void initialize() {
        System.out.println("🔄 Initializing ReturnedItemsController...");

        itemService = ItemService.getInstance();
        userService = UserService.getInstance();

        setupEventHandlers();
        loadReturnedItems();

        if (userService.getCurrentUser() != null) {
            User currentUser = userService.getCurrentUser();
            subtitleLabel.setText("Welcome, " + currentUser.getFirstName() + " 🏆 " + currentUser.getRewardTier());
        }

        System.out.println("✅ ReturnedItemsController initialized");
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems());
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> filterItems());

        // Initialize filter combo
        filterCombo.getItems().addAll("All Items", "Verified Only", "Pending Verification", "With Rewards");
        filterCombo.setValue("All Items");
    }

    private void loadReturnedItems() {
        try {
            // Get all items with "returned" or "claimed" status
            allReturnedItems = itemService.getAllItems().stream()
                    .filter(item -> item != null &&
                            ("returned".equals(item.getStatus()) || "claimed".equals(item.getStatus())))
                    .collect(Collectors.toList());

            System.out.println("📋 Loaded " + allReturnedItems.size() + " returned items");

            updateStatistics();
            displayItems(allReturnedItems);

        } catch (Exception e) {
            System.err.println("❌ Error loading returned items: " + e.getMessage());
            e.printStackTrace();
            allReturnedItems = List.of(); // Ensure it's never null
            showError("Error loading returned items: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        totalItemsLabel.setText(String.valueOf(allReturnedItems.size()));
    }

    private void displayItems(List<LostFoundItem> items) {
        itemsContainer.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label noItemsLabel = new Label("No returned items found");
            noItemsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px; -fx-padding: 40px;");
            itemsContainer.getChildren().add(noItemsLabel);
            return;
        }

        for (LostFoundItem item : items) {
            itemsContainer.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(LostFoundItem item) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setSpacing(10);
        card.setPadding(new Insets(20));
        card.setMaxWidth(Double.MAX_VALUE);

        // Header with item name and status
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getItemName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Status badge
        String status = item.getStatus();
        Label statusBadge = new Label(status != null ? status.toUpperCase() : "UNKNOWN");
        statusBadge.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Reward indicator - Check if this item had a reward
        boolean hasReward = false;
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            hasReward = lostItem.getReward() != null && !lostItem.getReward().isEmpty();
        }

        if (hasReward) {
            Label rewardLabel = new Label("🎁 REWARD");
            rewardLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12px; -fx-font-weight: bold;");
            header.getChildren().add(rewardLabel);
        }

        // Verification badge
        Label verificationBadge = new Label(item.isVerified() ? "✓ VERIFIED" : "⏳ PENDING");
        verificationBadge.setStyle("-fx-text-fill: " +
                (item.isVerified() ? "#27ae60" : "#e67e22") +
                "; -fx-font-size: 12px; -fx-font-weight: bold;");

        header.getChildren().addAll(nameLabel, statusBadge, spacer, verificationBadge);

        // Item details
        Label categoryLabel = new Label("Category: " + item.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        // Reward information if available
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            if (lostItem.getReward() != null && !lostItem.getReward().isEmpty()) {
                Label rewardInfoLabel = new Label("💰 Reward Offered: " + lostItem.getReward());
                rewardInfoLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 13px; -fx-font-weight: bold;");
                card.getChildren().add(rewardInfoLabel);
            }
        }

        // Footer with location, date and actions
        HBox footer = new HBox();
        footer.setSpacing(15);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label locationLabel = new Label("📍 " + item.getLocation());
        locationLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        Label dateLabel = new Label("📅 " + item.getDate());
        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        // Show who verified the item if available
        if (item.getVerifiedBy() != null) {
            Label verifiedByLabel = new Label("✅ Verified by: " + item.getVerifiedBy());
            verifiedByLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
            footer.getChildren().add(verifiedByLabel);
        }

        // Show who reported the item
        Label reportedByLabel = new Label("👤 Reported by: " + item.getReportedBy());
        reportedByLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        footer.getChildren().add(reportedByLabel);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        viewDetailsBtn.setOnAction(e -> showItemDetails(item));

        // Add "My Rewards" button for user's own returned items
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        User currentUser = userService.getCurrentUser();
        if (currentUser != null && item.getReportedBy().equals(currentUser.getUsername())) {
            Button rewardsBtn = new Button("My Rewards");
            rewardsBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            rewardsBtn.setOnAction(e -> showUserRewards(item));
            actionButtons.getChildren().add(rewardsBtn);
        }

        // Add admin actions if user is admin and item is not verified
        if (currentUser != null && currentUser.isAdmin() && !item.isVerified()) {
            Button verifyBtn = new Button("Verify Return");
            verifyBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
            verifyBtn.setOnAction(e -> verifyItem(item));
            actionButtons.getChildren().add(verifyBtn);
        }

        actionButtons.getChildren().add(viewDetailsBtn);
        footer.getChildren().addAll(locationLabel, dateLabel, footerSpacer, actionButtons);

        card.getChildren().addAll(header, categoryLabel, descLabel, footer);
        return card;
    }

    private void verifyItem(LostFoundItem item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Verification");
        confirmAlert.setHeaderText("Verify Returned Item: " + item.getItemName());
        confirmAlert.setContentText("Are you sure you want to verify this returned item?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = itemService.verifyItem(item.getId(), userService.getCurrentUser().getUsername());
                if (success) {
                    showAlert("Success", "Returned item has been verified successfully!");
                    loadReturnedItems(); // Refresh the list
                } else {
                    showError("Failed to verify returned item");
                }
            }
        });
    }

    private void showUserRewards(LostFoundItem item) {
        User currentUser = userService.getCurrentUser();
        StringBuilder rewardsInfo = new StringBuilder();
        rewardsInfo.append("=== YOUR REWARD FOR RETURNED ITEM ===\n\n");
        rewardsInfo.append("Item: ").append(item.getItemName()).append("\n");
        rewardsInfo.append("Category: ").append(item.getCategory()).append("\n");
        rewardsInfo.append("Status: RETURNED 🎉\n\n");

        rewardsInfo.append("=== REWARD INFORMATION ===\n");
        rewardsInfo.append("💎 Your Current Tier: ").append(currentUser.getRewardTier()).append("\n");
        rewardsInfo.append("🏆 Your Points: ").append(currentUser.getRewardPoints()).append("\n");
        rewardsInfo.append("📦 Your Returned Items: ").append(currentUser.getItemsReturned()).append("\n\n");

        // Show reward if this was a lost item with reward
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            if (lostItem.getReward() != null && !lostItem.getReward().isEmpty()) {
                rewardsInfo.append("💰 You offered a reward: ").append(lostItem.getReward()).append("\n");
                rewardsInfo.append("🎁 This helped motivate someone to return your item!\n\n");
            }
        }

        rewardsInfo.append("=== TIER BENEFITS ===\n");
        rewardsInfo.append(currentUser.getTierBenefits()).append("\n\n");

        rewardsInfo.append("Thank you for being a valued member of our community! 🚀");

        TextArea textArea = new TextArea(rewardsInfo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 500);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your Rewards");
        alert.setHeaderText("Reward Profile - " + currentUser.getRewardTier() + " Tier");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void filterItems() {
        if (allReturnedItems == null) {
            return;
        }

        String searchText = searchField.getText().toLowerCase();
        String filterValue = filterCombo.getValue();

        List<LostFoundItem> filteredItems = allReturnedItems.stream()
                .filter(item -> item != null && (
                        item.getItemName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText) ||
                                item.getCategory().toLowerCase().contains(searchText) ||
                                item.getLocation().toLowerCase().contains(searchText) ||
                                (item.getVerifiedBy() != null && item.getVerifiedBy().toLowerCase().contains(searchText)) ||
                                item.getReportedBy().toLowerCase().contains(searchText)
                ))
                .filter(item -> {
                    if (filterValue == null || "All Items".equals(filterValue)) {
                        return true;
                    } else if ("Verified Only".equals(filterValue)) {
                        return item.isVerified();
                    } else if ("Pending Verification".equals(filterValue)) {
                        return !item.isVerified();
                    } else if ("With Rewards".equals(filterValue)) {
                        if (item instanceof LostItem) {
                            LostItem lostItem = (LostItem) item;
                            return lostItem.getReward() != null && !lostItem.getReward().isEmpty();
                        }
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        displayItems(filteredItems);
    }

    private void showItemDetails(LostFoundItem item) {
        StringBuilder details = new StringBuilder();
        details.append("=== RETURNED ITEM DETAILS ===\n\n");
        details.append("Item Name: ").append(item.getItemName()).append("\n");
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Description: ").append(item.getDescription()).append("\n");
        details.append("Location: ").append(item.getLocation()).append("\n");
        details.append("Date: ").append(item.getDate()).append("\n");
        details.append("Reported By: ").append(item.getReportedBy()).append("\n");
        details.append("Contact Info: ").append(item.getContactInfo() != null ? item.getContactInfo() : "Not provided").append("\n");
        details.append("Item ID: ").append(item.getId()).append("\n\n");

        // Reward information
        if (item instanceof LostItem) {
            LostItem lostItem = (LostItem) item;
            if (lostItem.getReward() != null && !lostItem.getReward().isEmpty()) {
                details.append("💰 Reward Offered: ").append(lostItem.getReward()).append("\n");
            }
        }

        details.append("=== VERIFICATION STATUS ===\n");
        if (item.isVerified()) {
            details.append("✅ VERIFIED\n");
            details.append("Verified By: ").append(item.getVerifiedBy()).append("\n");
            details.append("Verification Date: ").append(item.getVerificationDate()).append("\n");
        } else {
            details.append("⏳ PENDING VERIFICATION\n");
            details.append("This returned item is waiting for admin verification.\n");
        }

        details.append("\n=== RETURN INFORMATION ===\n");
        details.append("Status: ").append(item.getStatus()).append("\n");
        details.append("Item Type: ").append(item.getType()).append("\n");

        // Claim information
        String claimedBy = getClaimedBy(item);
        if (claimedBy != null) {
            details.append("Claimed/Returned by: ").append(claimedBy).append("\n");
        }

        details.append("This item has been successfully returned to its owner.");

        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Returned Item Details");
        alert.setHeaderText("Returned Item: " + item.getItemName());
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private String getClaimedBy(LostFoundItem item) {
        if (item instanceof LostItem) {
            return ((LostItem) item).getClaimedBy();
        } else if (item instanceof FoundItem) {
            return ((FoundItem) item).getClaimedBy();
        }
        return null;
    }

    @FXML
    private void handleRefresh() {
        loadReturnedItems();
        showAlert("Refreshed", "Returned items list has been updated.");
    }

    @FXML
    private void handleBackToDashboard() {
        closeWindow();
    }

    @FXML
    private void handleViewMyRewards() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return;

        // Get user's returned items
        List<LostFoundItem> userReturnedItems = itemService.getReturnedItemsForUser(currentUser.getUsername());

        StringBuilder rewardsInfo = new StringBuilder();
        rewardsInfo.append("=== YOUR REWARDS PROFILE ===\n\n");
        rewardsInfo.append("👤 User: ").append(currentUser.getFirstName()).append(" ").append(currentUser.getLastName()).append("\n");
        rewardsInfo.append("💎 Tier: ").append(currentUser.getRewardTier()).append("\n");
        rewardsInfo.append("🏆 Points: ").append(currentUser.getRewardPoints()).append("\n");
        rewardsInfo.append("📦 Items Returned: ").append(userReturnedItems.size()).append("\n\n");

        rewardsInfo.append("=== YOUR RETURNED ITEMS ===\n");
        if (userReturnedItems.isEmpty()) {
            rewardsInfo.append("No returned items yet. Keep participating!\n");
        } else {
            for (LostFoundItem item : userReturnedItems) {
                rewardsInfo.append("• ").append(item.getItemName());
                if (item instanceof LostItem) {
                    LostItem lostItem = (LostItem) item;
                    if (lostItem.getReward() != null && !lostItem.getReward().isEmpty()) {
                        rewardsInfo.append(" (With Reward: ").append(lostItem.getReward()).append(")");
                    }
                }
                rewardsInfo.append("\n");
            }
        }

        rewardsInfo.append("\n=== TIER BENEFITS ===\n");
        rewardsInfo.append(currentUser.getTierBenefits()).append("\n\n");

        rewardsInfo.append("=== HOW TO EARN MORE POINTS ===\n");
        rewardsInfo.append("• Report lost items with rewards: +25 bonus points\n");
        rewardsInfo.append("• Have your lost items returned: 50-150 random points\n");
        rewardsInfo.append("• Return found items to owners: 50-150 random points\n");
        rewardsInfo.append("• Active participation: Various bonus opportunities\n\n");

        rewardsInfo.append("Keep using the system to climb the reward tiers! 🚀");

        TextArea textArea = new TextArea(rewardsInfo.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 500);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your Rewards Profile");
        alert.setHeaderText("Reward Tier: " + currentUser.getRewardTier());
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleExportReport() {
        if (allReturnedItems == null || allReturnedItems.isEmpty()) {
            showAlert("No Data", "There are no returned items to export.");
            return;
        }

        // Enhanced export with reward information
        System.out.println("=== RETURNED ITEMS REPORT ===");
        System.out.println("Total Items: " + allReturnedItems.size());
        System.out.println("Generated by: " + userService.getCurrentUser().getUsername());
        System.out.println("=============================");

        for (LostFoundItem item : allReturnedItems) {
            String rewardInfo = "";
            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                if (lostItem.getReward() != null && !lostItem.getReward().isEmpty()) {
                    rewardInfo = " | Reward: " + lostItem.getReward();
                }
            }

            System.out.println("• " + item.getItemName() + " | " + item.getCategory() +
                    " | " + item.getLocation() + " | Verified: " + item.isVerified() +
                    " | Reported by: " + item.getReportedBy() + rewardInfo);
        }

        showAlert("Report Generated", "Returned items report has been generated in the console.\nTotal items: " + allReturnedItems.size());
    }

    private void closeWindow() {
        try {
            Stage currentStage = (Stage) itemsContainer.getScene().getWindow();
            currentStage.close();
            System.out.println("✅ Returned items window closed");
        } catch (Exception e) {
            System.err.println("❌ Error closing window: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
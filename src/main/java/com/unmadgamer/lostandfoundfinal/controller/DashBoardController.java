package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import com.unmadgamer.lostandfoundfinal.service.MessageService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class DashBoardController {

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private Label lostItemsLabel;

    @FXML
    private Label foundItemsLabel;

    @FXML
    private Label returnedItemsLabel;

    @FXML
    private Label rewardPointsLabel;

    @FXML
    private Label rank1Label;

    @FXML
    private Label rank2Label;

    @FXML
    private Label rank3Label;

    @FXML
    private Label score1Label;

    @FXML
    private Label score2Label;

    @FXML
    private Label score3Label;

    @FXML
    private Button adminVerificationBtn;

    @FXML
    private Label pendingClaimsLabel;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label verifiedItemsLabel;

    @FXML
    private Label rewardTierLabel;

    @FXML
    private Label itemsReturnedCountLabel;

    // NEW: Reward system debug buttons
    @FXML
    private Button checkRewardsBtn;

    @FXML
    private Button debugRewardsBtn;

    // NEW: Messaging system
    @FXML
    private Button chatButton;

    @FXML
    private Label unreadMessagesBadge;

    private UserService userService;
    private User currentUser;
    private ItemService itemService;
    private MessageService messageService;

    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        itemService = ItemService.getInstance();
        messageService = MessageService.getInstance();
        currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            System.out.println("🎯 Dashboard initialized for user: " + currentUser.getUsername());
            loadUserData();
            refreshDashboard();
            loadLeaderboard();
            setupAdminFeatures();
            setupRewardFeatures();

            // Debug data state
            debugDataState();
        } else {
            System.err.println("❌ No current user found in dashboard");
        }
    }

    // Setup admin-specific features
    private void setupAdminFeatures() {
        if (currentUser != null && currentUser.isAdmin()) {
            adminVerificationBtn.setVisible(true);
            adminVerificationBtn.setManaged(true);
            System.out.println("👑 Admin features enabled for: " + currentUser.getUsername());
        } else {
            adminVerificationBtn.setVisible(false);
            adminVerificationBtn.setManaged(false);
        }
    }

    // NEW: Setup reward system features
    private void setupRewardFeatures() {
        // Make reward debug buttons visible for testing
        if (checkRewardsBtn != null) {
            checkRewardsBtn.setVisible(true);
            checkRewardsBtn.setManaged(true);
        }
        if (debugRewardsBtn != null) {
            debugRewardsBtn.setVisible(true);
            debugRewardsBtn.setManaged(true);
        }
    }

    private void debugDataState() {
        System.out.println("=== DASHBOARD DATA STATE ===");
        System.out.println("Current user: " + currentUser.getUsername());
        System.out.println("Total items in system: " + itemService.getAllItems().size());
        System.out.println("Pending verification: " + itemService.getPendingVerificationCount());
        System.out.println("Verified items: " + itemService.getVerifiedItems().size());
        System.out.println("Pending claims: " + itemService.getPendingClaimItems().size());
        System.out.println("User reward points: " + currentUser.getRewardPoints());
        System.out.println("User items returned: " + currentUser.getItemsReturned());
        System.out.println("User reward tier: " + currentUser.getRewardTier());

        // NEW: Debug messaging system
        int unreadCount = messageService.getUnreadMessageCount(currentUser.getUsername());
        System.out.println("Unread messages: " + unreadCount);

        System.out.println("=== END DATA STATE ===");
    }

    private void loadUserData() {
        userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        userEmailLabel.setText(currentUser.getEmail());

        // Load profile image
        try {
            Image profileImage = new Image(getClass().getResourceAsStream("/com/unmadgamer/lostandfoundfinal/images/profile.png"));
            profileImageView.setImage(profileImage);
        } catch (Exception e) {
            System.out.println("ℹ️  Profile image not found, using default");
        }
    }

    // Public method to refresh statistics (can be called from other controllers)
    public void refreshDashboard() {
        System.out.println("🔄 Refreshing dashboard statistics...");

        // Force refresh from JSON files
        itemService.refreshItems();

        // Refresh user data to get latest rewards
        userService.refreshUsers();
        currentUser = userService.getCurrentUser(); // Update current user reference

        loadStatistics();
        loadLeaderboard();
        updateAdminStats();
        updateRewardDisplay();
        updateUnreadMessagesBadge(); // NEW: Update message badge
    }

    private void loadStatistics() {
        String currentUsername = userService.getCurrentUser().getUsername();

        // Calculate user-specific statistics
        int lostCount = (int) itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(currentUsername) && item.getType().equals("lost"))
                .count();

        int foundCount = (int) itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(currentUsername) && item.getType().equals("found"))
                .count();

        // Use actual returned items count from service
        int returnedCount = itemService.getReturnedItemsByUser(currentUsername).size();

        // Use actual reward points from user object
        int rewardPoints = currentUser.getRewardPoints();

        lostItemsLabel.setText(String.valueOf(lostCount));
        foundItemsLabel.setText(String.valueOf(foundCount));
        returnedItemsLabel.setText(String.valueOf(returnedCount));
        rewardPointsLabel.setText(String.valueOf(rewardPoints));

        System.out.println("📊 Statistics loaded - Lost: " + lostCount + ", Found: " + foundCount +
                ", Returned: " + returnedCount + ", Points: " + rewardPoints);
    }

    private void updateRewardDisplay() {
        if (currentUser != null) {
            // Update reward points label
            if (rewardPointsLabel != null) {
                rewardPointsLabel.setText(String.valueOf(currentUser.getRewardPoints()));
                rewardPointsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FFD700;");
            }

            // Update items returned count
            if (itemsReturnedCountLabel != null) {
                itemsReturnedCountLabel.setText(String.valueOf(currentUser.getItemsReturned()));
                itemsReturnedCountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
            }

            // Update reward tier
            if (rewardTierLabel != null) {
                rewardTierLabel.setText(currentUser.getRewardTier());
                // Color code the tier
                switch (currentUser.getRewardTier()) {
                    case "Platinum":
                        rewardTierLabel.setStyle("-fx-text-fill: #e5e4e2; -fx-font-weight: bold; -fx-font-size: 14;");
                        break;
                    case "Gold":
                        rewardTierLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 14;");
                        break;
                    case "Silver":
                        rewardTierLabel.setStyle("-fx-text-fill: #c0c0c0; -fx-font-weight: bold; -fx-font-size: 14;");
                        break;
                    default:
                        rewardTierLabel.setStyle("-fx-text-fill: #cd7f32; -fx-font-weight: bold; -fx-font-size: 14;");
                }
            }

            System.out.println("🎯 Reward display updated - Points: " + currentUser.getRewardPoints() +
                    ", Items Returned: " + currentUser.getItemsReturned() +
                    ", Tier: " + currentUser.getRewardTier());
        }
    }

    private void updateAdminStats() {
        if (currentUser.isAdmin()) {
            int pendingVerification = (int) itemService.getPendingVerificationCount();
            int totalVerified = (int) itemService.getTotalVerifiedCount();
            int pendingClaims = (int) itemService.getPendingClaimItems().size();
            int totalItems = itemService.getAllItems().size();

            // Update labels if they exist
            if (pendingClaimsLabel != null) {
                pendingClaimsLabel.setText(String.valueOf(pendingClaims));
            }
            if (totalItemsLabel != null) {
                totalItemsLabel.setText(String.valueOf(totalItems));
            }
            if (verifiedItemsLabel != null) {
                verifiedItemsLabel.setText(String.valueOf(totalVerified));
            }

            System.out.println("👑 Admin stats - Pending verification: " + pendingVerification +
                    ", Total verified: " + totalVerified +
                    ", Pending claims: " + pendingClaims);
        }
    }

    // NEW: Update unread messages badge
    private void updateUnreadMessagesBadge() {
        int unreadCount = messageService.getUnreadMessageCount(currentUser.getUsername());

        if (unreadCount > 0) {
            unreadMessagesBadge.setText(String.valueOf(unreadCount));
            unreadMessagesBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; " +
                    "-fx-padding: 2 5; -fx-background-radius: 10;");
            unreadMessagesBadge.setVisible(true);
            System.out.println("🔔 Unread messages: " + unreadCount);
        } else {
            unreadMessagesBadge.setVisible(false);
            System.out.println("✅ No unread messages");
        }
    }

    private void loadLeaderboard() {
        // Simple leaderboard based on reward points
        int userPoints = currentUser.getRewardPoints();

        rank1Label.setText("1. " + currentUser.getFirstName() + " " + currentUser.getLastName());
        score1Label.setText(String.valueOf(userPoints));

        // For demo purposes, show some sample users
        rank2Label.setText("2. John Smith");
        score2Label.setText(String.valueOf(Math.max(0, userPoints - 10)));

        rank3Label.setText("3. Sarah Johnson");
        score3Label.setText(String.valueOf(Math.max(0, userPoints - 20)));

        System.out.println("🏆 Leaderboard loaded - User rank: #1 with " + userPoints + " points");
    }

    // ===== REWARD SYSTEM METHODS =====

    @FXML
    private void handleCheckRewardStatus() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            System.out.println("=== CURRENT REWARD STATUS ===");
            System.out.println("User: " + currentUser.getUsername());
            System.out.println("Points: " + currentUser.getRewardPoints());
            System.out.println("Items Returned: " + currentUser.getItemsReturned());
            System.out.println("Tier: " + currentUser.getRewardTier());
            System.out.println("Multiplier: " + currentUser.getTierMultiplier());
            System.out.println("Next Tier: " + currentUser.getNextTierInfo());

            // Check JSON file directly
            try {
                String content = new String(java.nio.file.Files.readAllBytes(
                        java.nio.file.Paths.get("data/users.json")));
                System.out.println("Users JSON: " + content);
            } catch (Exception e) {
                System.err.println("Could not read users.json: " + e.getMessage());
            }
            System.out.println("=== END STATUS ===");

            String benefits = currentUser.getTierBenefits().replace("•", "\n•");

            showAlert("Reward System Status",
                    "🏆 Reward System Status\n\n" +
                            "👤 User: " + currentUser.getUsername() +
                            "\n📊 Points: " + currentUser.getRewardPoints() +
                            "\n📦 Items Returned: " + currentUser.getItemsReturned() +
                            "\n💎 Tier: " + currentUser.getRewardTier() +
                            "\n⭐ Multiplier: " + currentUser.getTierMultiplier() + "x" +
                            "\n\n🎯 " + currentUser.getNextTierInfo() +
                            "\n\nBenefits:\n" + benefits,
                    Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleDebugRewards() {
        itemService.debugRewardSystem();
        showAlert("Reward System Debug",
                "Complete reward system debug information printed to console.\n\n" +
                        "This shows:\n" +
                        "• All users and their reward points\n" +
                        "• Total returned items in system\n" +
                        "• Pending claims\n" +
                        "Check the console for detailed information.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewRewardDetails() {
        if (currentUser != null) {
            String benefits = currentUser.getTierBenefits().replace("•", "\n•");
            String details = "🎯 Reward System Details\n\n" +
                    "📊 Your Current Status:\n" +
                    "• Points: " + currentUser.getRewardPoints() + "\n" +
                    "• Items Returned: " + currentUser.getItemsReturned() + "\n" +
                    "• Tier: " + currentUser.getRewardTier() + "\n" +
                    "• Multiplier: " + currentUser.getTierMultiplier() + "x\n\n" +

                    "🏆 Tier System:\n" +
                    "• Bronze: 0-199 points (1.0x)\n" +
                    "• Silver: 200-499 points (1.25x)\n" +
                    "• Gold: 500-999 points (1.5x)\n" +
                    "• Platinum: 1000+ points (2.0x)\n\n" +

                    "💰 Earning Points:\n" +
                    "• Return lost item: 50 points\n" +
                    "• Help return found item: 50 points\n" +
                    "• Activity bonus: +20 points (after 5+ returns)\n" +
                    "• Random bonus: 0-100 points\n\n" +

                    "🎁 Your Benefits:\n" + benefits + "\n\n" +

                    "🎯 Progress: " + currentUser.getNextTierInfo();

            showAlert("Reward System Guide", details, Alert.AlertType.INFORMATION);
        }
    }

    // ===== MESSAGING SYSTEM METHODS =====

    @FXML
    private void handleOpenChat() {
        System.out.println("Clicked: Open Chat");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/chat.fxml"));
            Parent root = loader.load();

            Stage chatStage = new Stage();
            chatStage.setTitle("Messaging Center - Lost and Found System");
            chatStage.setScene(new Scene(root, 800, 600));
            chatStage.show();

            // Refresh dashboard when chat window closes to update unread count
            chatStage.setOnHidden(event -> {
                System.out.println("💬 Chat window closed, refreshing dashboard...");
                refreshDashboard();
            });

        } catch (IOException e) {
            System.err.println("❌ Error opening chat: " + e.getMessage());
            showError("Cannot open messaging center: " + e.getMessage());
        }
    }

    @FXML
    private void handleTestReward() {
        System.out.println("🧪 TESTING REWARD SYSTEM MANUALLY");

        User currentUser = userService.getCurrentUser();
        int currentPoints = currentUser.getRewardPoints();
        int currentItems = currentUser.getItemsReturned();

        // Manually add reward points
        currentUser.addRewardPoints(50);
        currentUser.incrementItemsReturned();

        // Save to JSON
        userService.saveUsers();

        // Refresh from JSON to verify
        userService.refreshUsers();
        currentUser = userService.getCurrentUser();

        // Refresh display
        refreshDashboard();

        System.out.println("🎁 Manually added 50 points to " + currentUser.getUsername());
        System.out.println("📊 Points: " + currentPoints + " → " + currentUser.getRewardPoints());
        System.out.println("📦 Items: " + currentItems + " → " + currentUser.getItemsReturned());

        showAlert("Reward Test",
                "Manually added 50 reward points!\n\n" +
                        "Points: " + currentPoints + " → " + currentUser.getRewardPoints() + "\n" +
                        "Items Returned: " + currentItems + " → " + currentUser.getItemsReturned() + "\n" +
                        "If this works, the reward system IS functional!",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDebugRewardFlow() {
        System.out.println("=== REWARD FLOW DEBUG ===");

        // Check current user reward status
        User currentUser = userService.getCurrentUser();
        System.out.println("👤 Current User: " + currentUser.getUsername());
        System.out.println("💰 Current Points: " + currentUser.getRewardPoints());
        System.out.println("📦 Items Returned: " + currentUser.getItemsReturned());
        System.out.println("💎 Tier: " + currentUser.getRewardTier());
        System.out.println("Multiplier: " + currentUser.getTierMultiplier());
        System.out.println("Next Tier: " + currentUser.getNextTierInfo());

        // Check if there are any returned items for this user
        int returnedItems = itemService.getReturnedItemsByUser(currentUser.getUsername()).size();
        System.out.println("🔄 Returned Items for User: " + returnedItems);

        // Check pending claims that could be approved
        int pendingClaims = itemService.getPendingClaimItems().size();
        System.out.println("⏳ Pending Claims in System: " + pendingClaims);

        // Check all users reward status
        System.out.println("=== ALL USERS REWARD STATUS ===");
        for (User user : userService.getAllUsers()) {
            System.out.println("   👤 " + user.getUsername() + " | Points: " + user.getRewardPoints() +
                    " | Items: " + user.getItemsReturned() + " | Tier: " + user.getRewardTier());
        }

        // Check messaging system
        int unreadMessages = messageService.getUnreadMessageCount(currentUser.getUsername());
        System.out.println("💬 Unread Messages: " + unreadMessages);

        System.out.println("=== END DEBUG ===");

        showAlert("Reward Debug",
                "Check console for detailed reward flow information.\n\n" +
                        "Current User: " + currentUser.getUsername() + "\n" +
                        "Points: " + currentUser.getRewardPoints() + "\n" +
                        "Items Returned: " + currentUser.getItemsReturned() + "\n" +
                        "Pending Claims: " + pendingClaims + "\n" +
                        "Returned Items: " + returnedItems + "\n" +
                        "Unread Messages: " + unreadMessages,
                Alert.AlertType.INFORMATION);
    }

    // ===== NAVIGATION METHODS =====

    @FXML
    private void handleLostForm() {
        System.out.println("Clicked: Lost Form");
        openWindowWithCallback("/com/unmadgamer/lostandfoundfinal/lost-form.fxml", "Report Lost Item");
    }

    @FXML
    private void handleFoundForm() {
        System.out.println("Clicked: Found Form");
        openWindowWithCallback("/com/unmadgamer/lostandfoundfinal/found-form.fxml", "Report Found Item");
    }

    @FXML
    private void handleViewLostItems() {
        System.out.println("Clicked: View Lost Items");
        openWindowWithCallback("/com/unmadgamer/lostandfoundfinal/lost-items.fxml", "Lost Items");
    }

    @FXML
    private void handleViewFoundItems() {
        System.out.println("Clicked: View Found Items");
        openWindowWithCallback("/com/unmadgamer/lostandfoundfinal/found-items.fxml", "Found Items");
    }

    @FXML
    private void handleViewReturnedItems() {
        System.out.println("Clicked: View Returned Items");
        openWindowWithCallback("/com/unmadgamer/lostandfoundfinal/returned-items.fxml", "Returned Items");
    }

    // UPDATED: Admin Verification Dashboard
    @FXML
    private void handleAdminVerification() {
        System.out.println("Clicked: Admin Verification");
        if (currentUser != null && currentUser.isAdmin()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/admin-verification-dashboard.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Admin Verification Dashboard - " + currentUser.getFirstName());
                stage.setScene(new Scene(root, 1200, 800));

                // Refresh dashboard when admin window closes
                stage.setOnHidden(event -> {
                    System.out.println("🔄 Admin verification dashboard closed, refreshing dashboard...");
                    refreshDashboard();
                });

                stage.show();

                System.out.println("✅ Admin verification dashboard opened");
            } catch (IOException e) {
                System.err.println("❌ Error opening admin verification dashboard: " + e.getMessage());
                showError("Cannot open Verification Dashboard: " + e.getMessage());
            }
        } else {
            showAlert("Access Denied", "You need administrator privileges to access the verification dashboard.");
        }
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Clicked: Edit Profile");
        showAlert("Edit Profile", "Edit profile functionality will be implemented soon!");
    }

    @FXML
    private void handleRefreshDashboard() {
        System.out.println("Clicked: Refresh Dashboard");
        refreshDashboard();
        showAlert("Refreshed", "Dashboard statistics have been updated with the latest data!");
    }

    @FXML
    private void handleDataDebug() {
        System.out.println("Clicked: Data Debug");
        debugDataState();
        showAlert("Data Debug", "Check console for data debug information");
    }

    @FXML
    private void handleDataReset() {
        System.out.println("Clicked: Data Reset");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Data");
        alert.setHeaderText("Reset All Data");
        alert.setContentText("This will delete ALL items and users (except default admin). Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Use JsonDataService to reset data properly
                    com.unmadgamer.lostandfoundfinal.service.JsonDataService jsonDataService =
                            new com.unmadgamer.lostandfoundfinal.service.JsonDataService();
                    jsonDataService.resetAllData();

                    // Refresh the dashboard
                    refreshDashboard();
                    showAlert("Data Reset", "All data has been reset successfully");
                } catch (Exception e) {
                    showError("Error resetting data: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        System.out.println("Clicked: Logout");
        userService.logout();
        try {
            // Close current dashboard
            Stage currentStage = (Stage) userNameLabel.getScene().getWindow();
            currentStage.close();

            // Open login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Lost and Found System - Login");
            loginStage.setScene(new Scene(root, 660, 400));
            loginStage.show();

            System.out.println("✅ Logout successful");
        } catch (IOException e) {
            showError("Cannot logout: " + e.getMessage());
        }
    }

    // ===== HELPER METHODS =====

    // Open window with callback for refresh
    private void openWindowWithCallback(String fxmlPath, String title) {
        try {
            System.out.println("🚪 Attempting to open: " + fxmlPath);

            java.net.URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("❌ FXML file not found: " + fxmlPath);
                showError("Cannot open " + title + ": File not found at " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Set up a listener for when the window closes
            Stage stage = new Stage();
            stage.setTitle(title + " - Lost and Found System");
            stage.setScene(new Scene(root));

            // When the form window closes, refresh the dashboard
            stage.setOnHidden(event -> {
                System.out.println("🔄 " + title + " window closed, refreshing dashboard...");
                refreshDashboard();
            });

            stage.show();

            System.out.println("✅ Successfully opened: " + title);

        } catch (Exception e) {
            System.err.println("❌ Error opening " + title + ": " + e.getMessage());
            e.printStackTrace();
            showError("Cannot open " + title + ": " + e.getMessage());
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
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

    private UserService userService;
    private User currentUser;
    private ItemService itemService;

    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        itemService = ItemService.getInstance();
        currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            System.out.println("🎯 Dashboard initialized for user: " + currentUser.getUsername());
            loadUserData();
            loadStatistics();
            loadLeaderboard();
            setupAdminFeatures();

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

    private void debugDataState() {
        System.out.println("=== DASHBOARD DATA STATE ===");
        System.out.println("Current user: " + currentUser.getUsername());
        System.out.println("Total items in system: " + itemService.getAllItems().size());
        System.out.println("Pending verification: " + itemService.getPendingVerificationCount());
        System.out.println("Verified items: " + itemService.getVerifiedItems().size());
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
            // Set a default image or leave as is
        }
    }

    // Public method to refresh statistics (can be called from other controllers)
    public void refreshStatistics() {
        System.out.println("🔄 Refreshing dashboard statistics...");
        loadStatistics();
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

        int returnedCount = (int) itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(currentUsername) && "returned".equals(item.getStatus()))
                .count();

        // Calculate reward points based on user activity
        int rewardPoints = calculateRewardPoints(currentUsername);

        lostItemsLabel.setText(String.valueOf(lostCount));
        foundItemsLabel.setText(String.valueOf(foundCount));
        returnedItemsLabel.setText(String.valueOf(returnedCount));
        rewardPointsLabel.setText(String.valueOf(rewardPoints));

        System.out.println("📊 Statistics loaded - Lost: " + lostCount + ", Found: " + foundCount + ", Returned: " + returnedCount + ", Points: " + rewardPoints);

        // Show admin stats if user is admin
        if (currentUser.isAdmin()) {
            int pendingVerification = (int) itemService.getPendingVerificationCount();
            int totalVerified = (int) itemService.getTotalVerifiedCount();
            System.out.println("👑 Admin stats - Pending verification: " + pendingVerification + ", Total verified: " + totalVerified);
        }
    }

    private int calculateRewardPoints(String username) {
        int points = 0;

        // Points for reporting found items
        long foundItems = itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(username) && item.getType().equals("found") && item.isVerified())
                .count();
        points += foundItems * 10;

        // Points for successful returns (items claimed by others)
        long returnedItems = itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(username) && "returned".equals(item.getStatus()))
                .count();
        points += returnedItems * 20;

        // Bonus points for verified items
        long verifiedItems = itemService.getAllItems().stream()
                .filter(item -> item.getReportedBy().equals(username) && item.isVerified())
                .count();
        points += verifiedItems * 5;

        return points;
    }

    private void loadLeaderboard() {
        // Simple leaderboard based on reward points
        // In a real application, you'd calculate this from all users
        int userPoints = Integer.parseInt(rewardPointsLabel.getText());

        rank1Label.setText("1. " + currentUser.getFirstName() + " " + currentUser.getLastName());
        score1Label.setText(String.valueOf(userPoints));

        // For demo purposes, show some sample users
        rank2Label.setText("2. John Smith");
        score2Label.setText(String.valueOf(userPoints - 10));

        rank3Label.setText("3. Sarah Johnson");
        score3Label.setText(String.valueOf(userPoints - 20));

        System.out.println("🏆 Leaderboard loaded - User rank: #1 with " + userPoints + " points");
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
        openWindow("/com/unmadgamer/lostandfoundfinal/lost-items.fxml", "Lost Items");
    }

    @FXML
    private void handleViewFoundItems() {
        System.out.println("Clicked: View Found Items");
        openWindow("/com/unmadgamer/lostandfoundfinal/found-items.fxml", "Found Items");
    }

    @FXML
    private void handleViewReturnedItems() {
        System.out.println("Clicked: View Returned Items");
        openWindow("/com/unmadgamer/lostandfoundfinal/returned-items.fxml", "Returned Items");
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
        refreshStatistics();
        loadLeaderboard();
        showAlert("Refreshed", "Dashboard statistics have been updated!");
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
                // Reset data through JsonDataService
                itemService.getAllItems().clear(); // Clear in-memory data
                userService.getAllUsers().removeIf(user -> !user.isAdmin()); // Keep only admin users

                // Save the reset state
                com.unmadgamer.lostandfoundfinal.service.JsonDataService jsonDataService =
                        new com.unmadgamer.lostandfoundfinal.service.JsonDataService();
                jsonDataService.saveItems(itemService.getAllItems());
                jsonDataService.saveUsers(userService.getAllUsers());

                showAlert("Data Reset", "All data has been reset successfully");
                refreshStatistics();
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
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // When the form window closes, refresh the dashboard
            stage.setOnHidden(event -> {
                System.out.println("🔄 Form window closed, refreshing dashboard...");
                refreshStatistics();
            });

            stage.show();

            System.out.println("✅ Successfully opened: " + title);

        } catch (Exception e) {
            System.err.println("❌ Error opening " + title + ": " + e.getMessage());
            e.printStackTrace();
            showError("Cannot open " + title + ": " + e.getMessage());
        }
    }

    private void openWindow(String fxmlPath, String title) {
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

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
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
}
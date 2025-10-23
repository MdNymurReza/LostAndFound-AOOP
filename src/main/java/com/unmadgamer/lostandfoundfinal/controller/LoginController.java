package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Label loginMessageField;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        setupKeyboardShortcuts();

        // Debug user data
        userService.debugUserJsonData();
        userService.debugAdminUser();
        debugUserList();
        testFxmlFiles();
    }

    private void setupKeyboardShortcuts() {
        // Enter key to login
        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Clear error message when typing
        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
    }

    private void clearErrorMessage() {
        if (!loginMessageField.getText().isEmpty()) {
            loginMessageField.setText("");
            usernameTextField.setStyle("");
            passwordTextField.setStyle("");
        }
    }

    private void debugUserList() {
        System.out.println("=== AVAILABLE USERS ===");
        userService.getAllUsers().forEach(user -> {
            System.out.println("👤 " + user.getUsername() + " | " + user.getPassword() + " | " + user.getRole() + " | Admin: " + user.isAdmin());
        });
        System.out.println("=== END USER LIST ===");
    }

    private void testFxmlFiles() {
        System.out.println("=== FXML FILE CHECK ===");
        String[] fxmlFiles = {
                "/com/unmadgamer/lostandfoundfinal/admin-verification-dashboard.fxml",
                "/com/unmadgamer/lostandfoundfinal/dashboard.fxml",
                "/com/unmadgamer/lostandfoundfinal/Login.fxml",
                "/com/unmadgamer/lostandfoundfinal/Registration.fxml"
        };

        for (String fxmlFile : fxmlFiles) {
            URL url = getClass().getResource(fxmlFile);
            if (url != null) {
                System.out.println("✅ FOUND: " + fxmlFile);
            } else {
                System.err.println("❌ NOT FOUND: " + fxmlFile);
            }
        }
        System.out.println("=== END FXML CHECK ===");
    }

    @FXML
    private void handleLogin() {
        String username = usernameTextField.getText().trim();
        String password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessageField.setText("Please enter both username and password");
            highlightEmptyFields(username, password);
            return;
        }

        System.out.println("🔐 Attempting login for: " + username);

        if (userService.login(username, password)) {
            loginMessageField.setText("Login successful!");
            loginMessageField.setStyle("-fx-text-fill: green;");
            System.out.println("✅ Login successful for: " + username);
            openDashboard();
        } else {
            loginMessageField.setText("Invalid username or password");
            loginMessageField.setStyle("-fx-text-fill: red;");
            usernameTextField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            passwordTextField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            System.out.println("❌ Login failed for: " + username);

            // Clear password field on failed login
            passwordTextField.clear();
            passwordTextField.requestFocus();
        }
    }

    private void highlightEmptyFields(String username, String password) {
        if (username.isEmpty()) {
            usernameTextField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
        if (password.isEmpty()) {
            passwordTextField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
    }

    @FXML
    private void switchToRegisterPage() {
        try {
            // Close login window
            Stage currentStage = (Stage) usernameTextField.getScene().getWindow();
            currentStage.close();

            // Open registration window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/Registration.fxml"));
            Parent root = loader.load();
            Stage registerStage = new Stage();
            registerStage.setTitle("User Registration - Lost and Found System");
            registerStage.setScene(new Scene(root, 600, 530));
            registerStage.show();
        } catch (IOException e) {
            showError("Cannot open registration form: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        usernameTextField.clear();
        passwordTextField.clear();
        loginMessageField.setText("");
        usernameTextField.setStyle("");
        passwordTextField.setStyle("");
        usernameTextField.requestFocus();
    }

    @FXML
    private void handleAdminLogin() {
        // Quick admin login for testing
        usernameTextField.setText("admin");
        passwordTextField.setText("admin123");
        System.out.println("🧪 ADMIN LOGIN TEST - Auto-filled credentials");
        handleLogin();
    }
//
//    @FXML
//    private void handleDemoLogin() {
//        // Quick demo user login for testing
//        usernameTextField.setText("demo");
//        passwordTextField.setText("demo123");
//        handleLogin();
//    }
//
//    @FXML
//    private void testAdminNavigation() {
//        // Force admin navigation test
//        System.out.println("🧪 TESTING ADMIN NAVIGATION DIRECTLY...");
//        usernameTextField.setText("admin");
//        passwordTextField.setText("admin123");
//        handleLogin();
//    }

    @FXML
    private void emergencyAdminFix() {
        System.out.println("🚨 EMERGENCY ADMIN FIX - Creating fresh admin user");

        try {
            // Create a fresh admin user
            User freshAdmin = new User(
                    "admin",
                    "admin123",
                    "admin@lostfound.com",
                    "System",
                    "Administrator",
                    "admin"  // Explicitly set role to "admin"
            );

            // Save this user directly
            List<User> users = new ArrayList<>();
            users.add(freshAdmin);
            userService.saveUsers(users);

            System.out.println("✅ Fresh admin user created with role: 'admin'");
            System.out.println("🔄 Please restart the application and try admin login again");

            showAlert("Admin Fix", "Fresh admin user created. Please restart the application.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("❌ Emergency admin fix failed: " + e.getMessage());
            showError("Emergency fix failed: " + e.getMessage());
        }
    }

    private void openDashboard() {
        try {
            // Close login window FIRST
            Stage currentStage = (Stage) usernameTextField.getScene().getWindow();
            currentStage.close();

            User currentUser = userService.getCurrentUser();

            if (currentUser == null) {
                showError("No user logged in. Please login again.");
                return;
            }

            System.out.println("\n🔍 ========== NAVIGATION DEBUG START ==========");
            System.out.println("🔍 Current User: " + currentUser.getUsername());
            System.out.println("🔍 Role: '" + currentUser.getRole() + "'");
            System.out.println("🔍 isAdmin(): " + currentUser.isAdmin());

            String fxmlFile;
            String title;

            // SIMPLIFIED Admin Detection - Use the isAdmin() method from User class
            boolean isAdminUser = currentUser.isAdmin();

            System.out.println("🔍 Final Admin Detection: " + isAdminUser);

            if (isAdminUser) {
                // ADMIN USERS go directly to Admin Verification Dashboard
                fxmlFile = "/com/unmadgamer/lostandfoundfinal/admin-verification-dashboard.fxml";
                title = "Admin Verification Dashboard - Lost and Found System";
                System.out.println("🚀 ADMIN USER - Opening Admin Verification Dashboard");
            } else {
                // REGULAR USERS go to regular Dashboard
                fxmlFile = "/com/unmadgamer/lostandfoundfinal/dashboard.fxml";
                title = "Dashboard - Lost and Found System";
                System.out.println("🎉 REGULAR USER - Opening User Dashboard");
            }

            // Debug: Check if FXML file exists
            System.out.println("🔍 Checking FXML file: " + fxmlFile);
            URL fxmlUrl = getClass().getResource(fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("❌ FXML FILE NOT FOUND: " + fxmlFile);
                showError("Dashboard file not found: " + fxmlFile);

                // Fallback: Open login window again
                openLoginWindow();
                return;
            }

            System.out.println("✅ FXML file found: " + fxmlUrl);

            // Open ONLY ONE dashboard window
            System.out.println("🔍 Loading FXML...");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            System.out.println("✅ FXML loaded successfully, creating stage...");

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle(title);

            // Set appropriate window size based on dashboard type
            if (isAdminUser) {
                dashboardStage.setScene(new Scene(root, 1200, 800)); // Larger for admin
                System.out.println("✅ Setting up ADMIN dashboard window (1200x800)");
            } else {
                dashboardStage.setScene(new Scene(root, 750, 600)); // Regular size for users
                System.out.println("✅ Setting up REGULAR USER dashboard window (800x600)");
            }

            dashboardStage.show();

            System.out.println("✅ " + title + " opened successfully!");
            System.out.println("🔍 ========== NAVIGATION DEBUG END ==========\n");

        } catch (IOException e) {
            System.err.println("❌ Error opening dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Cannot open dashboard: " + e.getMessage());

            // Fallback: Open login window again
            openLoginWindow();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());

            // Fallback: Open login window again
            openLoginWindow();
        }
    }

    // Add this helper method for fallback
    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/Login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Lost and Found System - Login");
            loginStage.setScene(new Scene(root, 660, 400));
            loginStage.show();
        } catch (Exception e) {
            System.err.println("❌ Critical: Cannot even open login window: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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
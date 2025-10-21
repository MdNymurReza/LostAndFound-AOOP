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
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;

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

        // Debug: Show available users
        debugUserList();
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
            System.out.println("👤 " + user.getUsername() + " | " + user.getPassword() + " | " + user.getRole());
        });
        System.out.println("=== END USER LIST ===");
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
        handleLogin();
    }

    @FXML
    private void handleDemoLogin() {
        // Quick demo user login for testing
        usernameTextField.setText("demo");
        passwordTextField.setText("demo123");
        handleLogin();
    }

    private void openDashboard() {
        try {
            // Close login window
            Stage currentStage = (Stage) usernameTextField.getScene().getWindow();
            currentStage.close();

            User currentUser = userService.getCurrentUser();
            String fxmlFile;
            String title;

            // Check if user is admin and redirect accordingly
            if (currentUser != null && currentUser.isAdmin()) {
                fxmlFile = "/com/unmadgamer/lostandfoundfinal/admin-verification-dashboard.fxml";
                title = "Admin Dashboard - Lost and Found System";
                System.out.println("🚀 Redirecting admin to admin dashboard: " + currentUser.getUsername());
            } else {
                fxmlFile = "/com/unmadgamer/lostandfoundfinal/dashboard.fxml";
                title = "Dashboard - Lost and Found System";
                System.out.println("🎉 Redirecting user to regular dashboard: " +
                        (currentUser != null ? currentUser.getUsername() : "Unknown"));
            }

            // Open appropriate dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle(title);
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();

        } catch (IOException e) {
            showError("Cannot open dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
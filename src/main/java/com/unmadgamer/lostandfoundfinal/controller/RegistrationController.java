package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField firstnameField;

    @FXML
    private TextField lastnameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label registerMessageField;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = UserService.getInstance();
        setupFieldListeners();
    }

    private void setupFieldListeners() {
        // Clear error message when user starts typing
        firstnameField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        lastnameField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> clearErrorMessage());
    }

    private void clearErrorMessage() {
        if (!registerMessageField.getText().isEmpty()) {
            registerMessageField.setText("");
        }
    }

    @FXML
    private void handleRegister() {
        String firstname = firstnameField.getText().trim();
        String lastname = lastnameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (!validateInput(firstname, lastname, username, email, password, confirmPassword)) {
            return;
        }

        // Attempt registration - FIXED: Using registerUser instead of register
        if (userService.registerUser(username, password, email, firstname, lastname)) {
            // Try to auto-login immediately after registration
            if (userService.login(username, password)) {
                showSuccess("Registration successful! Logging you in...");
                openDashboard();
            } else {
                // If auto-login fails, go to login page
                showSuccess("Registration successful! Please login with your credentials.");
                switchToLoginScene();
            }
        } else {
            registerMessageField.setText("Username already exists. Please choose a different username.");
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
    }

    private boolean validateInput(String firstname, String lastname, String username,
                                  String email, String password, String confirmPassword) {
        // Reset field styles
        resetFieldStyles();

        if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerMessageField.setText("Please fill in all fields");
            highlightEmptyFields(firstname, lastname, username, email, password, confirmPassword);
            return false;
        }

        if (firstname.length() < 2) {
            registerMessageField.setText("First name must be at least 2 characters");
            firstnameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (lastname.length() < 2) {
            registerMessageField.setText("Last name must be at least 2 characters");
            lastnameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (username.length() < 3) {
            registerMessageField.setText("Username must be at least 3 characters");
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            registerMessageField.setText("Username can only contain letters, numbers, and underscores");
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (!isValidEmail(email)) {
            registerMessageField.setText("Please enter a valid email address");
            emailField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (password.length() < 6) {
            registerMessageField.setText("Password must be at least 6 characters long");
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            registerMessageField.setText("Passwords do not match");
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            confirmPasswordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            return false;
        }

        return true;
    }

    private void highlightEmptyFields(String firstname, String lastname, String username,
                                      String email, String password, String confirmPassword) {
        if (firstname.isEmpty()) firstnameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        if (lastname.isEmpty()) lastnameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        if (username.isEmpty()) usernameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        if (email.isEmpty()) emailField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        if (password.isEmpty()) passwordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        if (confirmPassword.isEmpty()) confirmPasswordField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
    }

    private void resetFieldStyles() {
        firstnameField.setStyle("");
        lastnameField.setStyle("");
        usernameField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
    }

    @FXML
    private void switchToLoginScene() {
        try {
            // Close current registration window
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.close();

            // Open login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/Login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Lost and Found System - Login");
            loginStage.setScene(new Scene(root, 660, 400));
            loginStage.show();
        } catch (IOException e) {
            showError("Cannot open login form: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        firstnameField.clear();
        lastnameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        registerMessageField.setText("");
        resetFieldStyles();
        firstnameField.requestFocus();
    }

    @FXML
    private void handleEnterKey() {
        handleRegister();
    }

    private boolean isValidEmail(String email) {
        // Basic email validation
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void openDashboard() {
        try {
            // Close registration window
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.close();

            // Open dashboard window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();

            System.out.println("✅ User registered and logged in: " + userService.getCurrentUser().getUsername());
        } catch (IOException e) {
            showError("Cannot open dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
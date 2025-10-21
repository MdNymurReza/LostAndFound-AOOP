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
        if (firstname.isEmpty() || lastname.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerMessageField.setText("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerMessageField.setText("Passwords do not match");
            return;
        }

        if (password.length() < 6) {
            registerMessageField.setText("Password must be at least 6 characters long");
            return;
        }

        if (!isValidEmail(email)) {
            registerMessageField.setText("Please enter a valid email address");
            return;
        }

        // Attempt registration
        if (userService.register(username, password, email, firstname, lastname)) {
            // Try to auto-login immediately after registration
            if (userService.login(username, password)) {
                showSuccess("Registration successful! Logging you in...");
                openMainApplication();
            } else {
                // If auto-login fails, go to login page
                showSuccess("Registration successful! Please login with your credentials.");
                switchToLoginScene();
            }
        } else {
            registerMessageField.setText("Username already exists. Please choose a different username.");
        }
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

    private boolean isValidEmail(String email) {
        // Basic email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void openMainApplication() {
        try {
            // Close registration window
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.close();

            // Open main application window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/main.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("Lost and Found System - Welcome " + userService.getCurrentUser().getFirstName());
            mainStage.setScene(new Scene(root, 800, 600));
            mainStage.show();
        } catch (IOException e) {
            showError("Cannot open main application: " + e.getMessage());
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
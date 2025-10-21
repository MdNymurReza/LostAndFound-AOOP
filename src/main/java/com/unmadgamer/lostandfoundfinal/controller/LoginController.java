package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    }

    @FXML
    private void handleLogin() {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessageField.setText("Please enter both username and password");
            return;
        }

        if (userService.login(username, password)) {
            loginMessageField.setText("Login successful!");
            openMainApplication();
        } else {
            loginMessageField.setText("Invalid username or password");
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
            registerStage.setTitle("User Registration");
            registerStage.setScene(new Scene(root, 600, 530));
            registerStage.show();
        } catch (IOException e) {
            showError("Cannot open registration form: " + e.getMessage());
        }
    }

    private void openMainApplication() {
        try {
            // Close login window
            Stage currentStage = (Stage) usernameTextField.getScene().getWindow();
            currentStage.close();

            // Open dashboard instead of main application
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();

            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();
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
}
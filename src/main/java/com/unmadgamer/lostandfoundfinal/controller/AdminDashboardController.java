package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label adminWelcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label pendingVerificationLabel;
    @FXML private Label returnedItemsLabel;
    @FXML private Label systemHealthLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colActions;

    @FXML private TableView<LostFoundItem> recentItemsTable;
    @FXML private TableColumn<LostFoundItem, String> colItemName;
    @FXML private TableColumn<LostFoundItem, String> colItemCategory;
    @FXML private TableColumn<LostFoundItem, String> colItemStatus;
    @FXML private TableColumn<LostFoundItem, String> colItemVerification;

    @FXML private VBox statsContainer;
    @FXML private VBox chartsContainer;

    private ItemService itemService;
    private UserService userService;
    private User currentAdmin;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        currentAdmin = userService.getCurrentUser();

        if (currentAdmin != null && currentAdmin.isAdmin()) {
            adminWelcomeLabel.setText("Admin Dashboard - Welcome, " + currentAdmin.getFirstName() + " " + currentAdmin.getLastName());
            setupTables();
            loadDashboardData();
            createCharts();
        } else {
            showError("Access Denied", "You don't have administrator privileges.");
            closeWindow();
        }
    }

    private void setupTables() {
        // Users Table
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colStatus.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.isActive()) {
                        setText("Active");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("Inactive");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<User, String>() {
            private final Button toggleBtn = new Button();
            private final Button editBtn = new Button("Edit");

            {
                toggleBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8;");
                editBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 4 8;");

                toggleBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBtn.setText(user.isActive() ? "Deactivate" : "Activate");

                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(toggleBtn, editBtn);
                    setGraphic(buttons);
                }
            }
        });

        // Recent Items Table
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colItemCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colItemVerification.setCellFactory(col -> new TableCell<LostFoundItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    LostFoundItem lostFoundItem = getTableView().getItems().get(getIndex());
                    if (lostFoundItem.isVerified()) {
                        setText("Verified");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("Pending");
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadDashboardData() {
        // Load statistics
        List<User> allUsers = userService.getAllUsers();
        List<LostFoundItem> allItems = itemService.getAllItems();

        // Get pending verification items
        List<LostFoundItem> pendingItems = itemService.getPendingVerificationItems();

        // Get returned items
        List<LostFoundItem> returnedItems = allItems.stream()
                .filter(item -> "returned".equals(item.getStatus()) || "claimed".equals(item.getStatus()))
                .collect(Collectors.toList());

        totalUsersLabel.setText(String.valueOf(allUsers.size()));
        totalItemsLabel.setText(String.valueOf(allItems.size()));
        pendingVerificationLabel.setText(String.valueOf(pendingItems.size()));
        returnedItemsLabel.setText(String.valueOf(returnedItems.size()));

        // Calculate system health (percentage of verified items)
        long verifiedCount = allItems.stream().filter(LostFoundItem::isVerified).count();
        double healthPercentage = allItems.isEmpty() ? 100 : (verifiedCount * 100.0 / allItems.size());
        systemHealthLabel.setText(String.format("%.1f%%", healthPercentage));

        // Load users table
        ObservableList<User> usersData = FXCollections.observableArrayList(allUsers);
        usersTable.setItems(usersData);

        // Load recent items (last 10 items)
        List<LostFoundItem> recentItems = allItems.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // Sort by creation date descending
                .limit(10)
                .collect(Collectors.toList());
        ObservableList<LostFoundItem> itemsData = FXCollections.observableArrayList(recentItems);
        recentItemsTable.setItems(itemsData);

        System.out.println("   Admin Dashboard loaded:");
        System.out.println("   Users: " + allUsers.size());
        System.out.println("   Items: " + allItems.size());
        System.out.println("   Pending: " + pendingItems.size());
        System.out.println("   Returned: " + returnedItems.size());
    }

    private void createCharts() {
        // Clear existing charts
        chartsContainer.getChildren().clear();

        List<LostFoundItem> allItems = itemService.getAllItems();

        // Items by Type Chart
        CategoryAxis typeAxis = new CategoryAxis();
        NumberAxis typeCountAxis = new NumberAxis();
        BarChart<String, Number> typeChart = new BarChart<>(typeAxis, typeCountAxis);
        typeChart.setTitle("Items by Type");
        typeChart.setLegendVisible(false);
        typeChart.setPrefHeight(250);

        XYChart.Series<String, Number> typeSeries = new XYChart.Series<>();

        long lostCount = allItems.stream()
                .filter(item -> "lost".equals(item.getType()))
                .count();
        long foundCount = allItems.stream()
                .filter(item -> "found".equals(item.getType()))
                .count();

        typeSeries.getData().add(new XYChart.Data<>("Lost", lostCount));
        typeSeries.getData().add(new XYChart.Data<>("Found", foundCount));

        typeChart.getData().add(typeSeries);

        // Verification Status Chart
        CategoryAxis verificationAxis = new CategoryAxis();
        NumberAxis verificationCountAxis = new NumberAxis();
        BarChart<String, Number> verificationChart = new BarChart<>(verificationAxis, verificationCountAxis);
        verificationChart.setTitle("Verification Status");
        verificationChart.setLegendVisible(false);
        verificationChart.setPrefHeight(250);

        XYChart.Series<String, Number> verificationSeries = new XYChart.Series<>();

        long verifiedCount = allItems.stream().filter(LostFoundItem::isVerified).count();
        long pendingCount = allItems.stream().filter(item -> !item.isVerified()).count();

        verificationSeries.getData().add(new XYChart.Data<>("Verified", verifiedCount));
        verificationSeries.getData().add(new XYChart.Data<>("Pending", pendingCount));

        verificationChart.getData().add(verificationSeries);

        // Item Status Chart
        CategoryAxis statusAxis = new CategoryAxis();
        NumberAxis statusCountAxis = new NumberAxis();
        BarChart<String, Number> statusChart = new BarChart<>(statusAxis, statusCountAxis);
        statusChart.setTitle("Item Status");
        statusChart.setLegendVisible(false);
        statusChart.setPrefHeight(250);

        XYChart.Series<String, Number> statusSeries = new XYChart.Series<>();

        long pendingStatus = allItems.stream()
                .filter(item -> "pending".equals(item.getStatus()))
                .count();
        long verifiedStatus = allItems.stream()
                .filter(item -> "verified".equals(item.getStatus()))
                .count();
        long claimedStatus = allItems.stream()
                .filter(item -> "claimed".equals(item.getStatus()))
                .count();
        long returnedStatus = allItems.stream()
                .filter(item -> "returned".equals(item.getStatus()))
                .count();

        statusSeries.getData().add(new XYChart.Data<>("Pending", pendingStatus));
        statusSeries.getData().add(new XYChart.Data<>("Verified", verifiedStatus));
        statusSeries.getData().add(new XYChart.Data<>("Claimed", claimedStatus));
        statusSeries.getData().add(new XYChart.Data<>("Returned", returnedStatus));

        statusChart.getData().add(statusSeries);

        // Add charts to container in rows
        HBox firstRow = new HBox(20);
        firstRow.getChildren().addAll(typeChart, verificationChart);

        HBox secondRow = new HBox(20);
        secondRow.getChildren().add(statusChart);

        chartsContainer.getChildren().addAll(firstRow, secondRow);
    }

    private void toggleUserStatus(User user) {
        if (user.getUsername().equals(currentAdmin.getUsername())) {
            showError("Cannot Modify", "You cannot modify your own account status.");
            return;
        }

        String action = user.isActive() ? "deactivate" : "activate";
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm User " + action);
        confirmAlert.setHeaderText(action.substring(0, 1).toUpperCase() + action.substring(1) + " User: " + user.getUsername());
        confirmAlert.setContentText("Are you sure you want to " + action + " this user?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Toggle user active status
                user.setActive(!user.isActive());
                userService.getAllUsers(); // This will trigger save through the service
                showAlert("Success", "User " + user.getUsername() + " has been " + action + "d.");
                loadDashboardData();
            }
        });
    }

    private void editUser(User user) {
        showAlert("Edit User", "Edit functionality for user: " + user.getUsername() + "\nThis feature will be implemented in the next version.");
    }

    @FXML
    private void handleUserManagement() {
        showAlert("User Management", "Advanced user management features will be implemented here.");
    }

    @FXML
    private void handleItemVerification() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/admin-verification-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Admin Verification Dashboard");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (IOException e) {
            showError("Error", "Cannot open verification dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleSystemSettings() {
        showAlert("System Settings", "System configuration and settings will be available here.");
    }

    @FXML
    private void handleGenerateReports() {
        // Generate system report
        List<User> allUsers = userService.getAllUsers();
        List<LostFoundItem> allItems = itemService.getAllItems();
        List<LostFoundItem> pendingItems = itemService.getPendingVerificationItems();
        List<LostFoundItem> returnedItems = allItems.stream()
                .filter(item -> "returned".equals(item.getStatus()) || "claimed".equals(item.getStatus()))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();
        report.append("=== SYSTEM REPORT ===\n\n");
        report.append("Statistics:\n");
        report.append("• Total Users: ").append(allUsers.size()).append("\n");
        report.append("• Total Items: ").append(allItems.size()).append("\n");
        report.append("• Pending Verification: ").append(pendingItems.size()).append("\n");
        report.append("• Returned/Claimed Items: ").append(returnedItems.size()).append("\n\n");

        report.append("User Breakdown:\n");
        long adminCount = allUsers.stream().filter(User::isAdmin).count();
        long userCount = allUsers.stream().filter(user -> !user.isAdmin()).count();
        report.append("• Administrators: ").append(adminCount).append("\n");
        report.append("• Regular Users: ").append(userCount).append("\n\n");

        report.append("Item Breakdown:\n");
        long lostCount = allItems.stream().filter(item -> "lost".equals(item.getType())).count();
        long foundCount = allItems.stream().filter(item -> "found".equals(item.getType())).count();
        long verifiedCount = allItems.stream().filter(LostFoundItem::isVerified).count();
        report.append("• Lost Items: ").append(lostCount).append("\n");
        report.append("• Found Items: ").append(foundCount).append("\n");
        report.append("• Verified Items: ").append(verifiedCount).append("\n\n");

        report.append("Recent Activity (Last 5 items):\n");
        allItems.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(item ->
                        report.append("• ").append(item.getItemName())
                                .append(" (").append(item.getType())
                                .append(") - ").append(item.getStatus())
                                .append(" - ").append(item.isVerified() ? "Verified" : "Pending")
                                .append("\n")
                );

        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setPrefSize(500, 500);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Report");
        alert.setHeaderText("Lost and Found System Report");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
        chartsContainer.getChildren().clear();
        createCharts();
        showAlert("Refreshed", "Dashboard data has been updated.");
    }

    @FXML
    private void handleBackToMainDashboard() {
        closeWindow();
    }

    @FXML
    private void handleLogout() {
        userService.logout();
        closeWindow();

        // Open login window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Lost and Found System - Login");
            loginStage.setScene(new Scene(root, 660, 400));
            loginStage.show();
        } catch (IOException e) {
            showError("Error", "Cannot open login window: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) adminWelcomeLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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
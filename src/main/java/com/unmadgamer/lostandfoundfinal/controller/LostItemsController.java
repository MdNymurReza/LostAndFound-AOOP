package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.service.ItemService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class LostItemsController {

    @FXML
    private TableView<LostFoundItem> itemsTable;

    private ItemService itemService;
    private UserService userService;
    private ObservableList<LostFoundItem> itemsData;

    @FXML
    public void initialize() {
        itemService = ItemService.getInstance();
        userService = UserService.getInstance();
        itemsData = FXCollections.observableArrayList();

        itemsTable.setItems(itemsData);
        loadLostItems();
    }

    private void loadLostItems() {
        List<LostFoundItem> lostItems = itemService.getItemsByStatus("lost");
        itemsData.setAll(lostItems);

        System.out.println("Loaded " + lostItems.size() + " lost items");
        for (LostFoundItem item : lostItems) {
            System.out.println(" - " + item.getItemName() + " | " + item.getCategory() + " | " + item.getDate());
        }
    }

    @FXML
    private void handleRefresh() {
        loadLostItems();
        showAlert("Refreshed", "Lost items list has been refreshed.");
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) itemsTable.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 600, 450));
            dashboardStage.show();
        } catch (IOException e) {
            showError("Cannot return to dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
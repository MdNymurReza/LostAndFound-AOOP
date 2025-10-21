package com.unmadgamer.lostandfoundfinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Start with login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/Login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 660, 400);
        stage.setTitle("Lost and Found System - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
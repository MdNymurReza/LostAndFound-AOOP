package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonDataService {
    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = DATA_DIR + "users.json";
    private static final String ITEMS_FILE = DATA_DIR + "items.json";

    private final ObjectMapper objectMapper;

    public JsonDataService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // User methods
    public List<User> loadUsers() {
        try {
            Path filePath = Paths.get(USERS_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("Users file doesn't exist, creating empty list");
                return new ArrayList<>();
            }

            List<User> users = objectMapper.readValue(
                    filePath.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class)
            );
            System.out.println("Successfully loaded " + users.size() + " users from JSON");
            return users;
        } catch (IOException e) {
            System.err.println("Error loading users from JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean saveUsers(List<User> users) {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            Path filePath = Paths.get(USERS_FILE);
            objectMapper.writeValue(filePath.toFile(), users);
            System.out.println("Successfully saved " + users.size() + " users to JSON");
            return true;
        } catch (Exception e) {
            System.err.println("Error saving users to JSON: " + e.getMessage());
            return false;
        }
    }

    // LostFoundItem methods
    public List<LostFoundItem> loadItems() {
        try {
            Path filePath = Paths.get(ITEMS_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("Items file doesn't exist, creating empty list");
                return new ArrayList<>();
            }

            List<LostFoundItem> items = objectMapper.readValue(
                    filePath.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LostFoundItem.class)
            );
            System.out.println("Successfully loaded " + items.size() + " items from JSON");

            // Debug: Print loaded items
            for (LostFoundItem item : items) {
                System.out.println("Loaded: " + item.getItemName() + " | " + item.getDate() + " | " + item.getStatus());
            }

            return items;
        } catch (IOException e) {
            System.err.println("Error loading items from JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean saveItems(List<LostFoundItem> items) {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            Path filePath = Paths.get(ITEMS_FILE);

            System.out.println("Saving " + items.size() + " items to JSON:");
            for (LostFoundItem item : items) {
                System.out.println(" - " + item.getItemName() + " | " + item.getDate() + " | " + item.getStatus());
            }

            objectMapper.writeValue(filePath.toFile(), items);
            System.out.println("✓ Successfully saved " + items.size() + " items to JSON");

            // Verify
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath);
                System.out.println("✓ File verification: " + fileSize + " bytes");
                return true;
            } else {
                System.out.println("✗ File verification: file not found after save");
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Error saving items to JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Add the missing verifyJsonFile method
    public void verifyJsonFile() {
        try {
            Path filePath = Paths.get(ITEMS_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("JSON file does not exist");
                return;
            }

            String content = Files.readString(filePath);
            System.out.println("=== JSON FILE CONTENT ===");
            System.out.println(content);
            System.out.println("=== END JSON CONTENT ===");

        } catch (Exception e) {
            System.err.println("Error verifying JSON file: " + e.getMessage());
        }
    }

    // Add the missing resetJsonFile method
    public void resetJsonFile() {
        try {
            Path filePath = Paths.get(ITEMS_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Deleted existing JSON file");
            }
        } catch (IOException e) {
            System.err.println("Error resetting JSON file: " + e.getMessage());
        }
    }

    public void debugFileOperations() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            Path itemsFile = Paths.get(ITEMS_FILE);

            System.out.println("=== FILE OPERATIONS DEBUG ===");
            System.out.println("Data directory: " + dataDir.toAbsolutePath());
            System.out.println("Items file: " + itemsFile.toAbsolutePath());
            System.out.println("Data directory exists: " + Files.exists(dataDir));
            System.out.println("Items file exists: " + Files.exists(itemsFile));

            if (Files.exists(itemsFile)) {
                String content = Files.readString(itemsFile);
                System.out.println("Items file content: " + content);
            }
            System.out.println("=== END DEBUG ===");
        } catch (Exception e) {
            System.err.println("Debug error: " + e.getMessage());
        }
    }
}
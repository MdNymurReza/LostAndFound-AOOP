package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonDataService {
    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = DATA_DIR + "users.json";
    private static final String ITEMS_FILE = DATA_DIR + "items.json";
    private static final String BACKUP_DIR = DATA_DIR + "backups/";

    private final ObjectMapper objectMapper;

    public JsonDataService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        // Register subtypes for polymorphic deserialization
        objectMapper.registerSubtypes(LostItem.class, FoundItem.class);

        // Initialize data directory
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                System.out.println("✅ Created data directory: " + dataDir.toAbsolutePath());
            }

            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                System.out.println("✅ Created backup directory: " + backupDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create data directory: " + e.getMessage());
        }
    }

    // User methods
    public List<User> loadUsers() {
        try {
            Path filePath = Paths.get(USERS_FILE);
            if (!Files.exists(filePath)) {
                System.out.println("📁 Users file doesn't exist, creating empty list");
                saveUsers(new ArrayList<>());
                return new ArrayList<>();
            }

            if (Files.size(filePath) == 0) {
                System.out.println("📄 Users file is empty, returning empty list");
                return new ArrayList<>();
            }

            // Read file content for validation
            String fileContent = Files.readString(filePath);
            if (fileContent.trim().isEmpty()) {
                System.out.println("📄 Users file contains only whitespace, returning empty list");
                return new ArrayList<>();
            }

            // Validate JSON syntax first
            if (!isValidJson(fileContent)) {
                System.err.println("❌ Invalid JSON in users file, creating backup and resetting...");
                createBackup(filePath, "users_corrupted_backup.json");

                // Try to recover data from corrupted JSON
                List<User> recoveredUsers = attemptUserDataRecovery(fileContent);
                System.out.println("🔧 Recovered " + recoveredUsers.size() + " users from corrupted file");

                // Save recovered users
                saveUsers(recoveredUsers);
                return recoveredUsers;
            }

            // Try to parse the JSON
            List<User> users = objectMapper.readValue(
                    filePath.toFile(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, User.class)
            );
            System.out.println("✅ Successfully loaded " + users.size() + " users from JSON");
            return users;
        } catch (IOException e) {
            System.err.println("❌ Error loading users from JSON: " + e.getMessage());

            // Create backup of corrupted file
            try {
                Path filePath = Paths.get(USERS_FILE);
                if (Files.exists(filePath)) {
                    createBackup(filePath, "users_error_backup.json");
                }
            } catch (Exception backupError) {
                System.err.println("Failed to create backup: " + backupError.getMessage());
            }

            // Reset to empty list
            saveUsers(new ArrayList<>());
            return new ArrayList<>();
        }
    }

    public boolean saveUsers(List<User> users) {
        try {
            Path filePath = Paths.get(USERS_FILE);

            // Create backup before saving
            if (Files.exists(filePath)) {
                createBackup(filePath, "users_pre_save_backup.json");
            }

            // Use temporary file for atomic save
            Path tempFile = Paths.get(USERS_FILE + ".tmp");
            objectMapper.writeValue(tempFile.toFile(), users);

            // Atomic move to final location
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Successfully saved " + users.size() + " users to JSON");

            // Verify save
            return verifySave(filePath, users.size());
        } catch (Exception e) {
            System.err.println("❌ Error saving users to JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // LostFoundItem methods with polymorphic support
    public List<LostFoundItem> loadItems() {
        try {
            Path filePath = Paths.get(ITEMS_FILE);

            if (!Files.exists(filePath)) {
                System.out.println("📁 Items file doesn't exist, creating new one with empty list");
                saveItems(new ArrayList<>());
                return new ArrayList<>();
            }

            if (Files.size(filePath) == 0) {
                System.out.println("📄 Items file is empty, returning empty list");
                return new ArrayList<>();
            }

            String fileContent = Files.readString(filePath);
            if (fileContent.trim().isEmpty()) {
                System.out.println("📄 Items file contains only whitespace, returning empty list");
                return new ArrayList<>();
            }

            if (!isValidJson(fileContent)) {
                System.err.println("❌ Invalid JSON syntax detected, creating backup and resetting");
                createBackup(filePath, "items_corrupted_backup.json");
                List<LostFoundItem> recoveredItems = attemptItemDataRecovery(fileContent);
                System.out.println("🔧 Recovered " + recoveredItems.size() + " items from corrupted file");
                saveItems(recoveredItems);
                return recoveredItems;
            }

            // Read JSON as tree to handle polymorphism
            JsonNode rootNode = objectMapper.readTree(fileContent);
            List<LostFoundItem> items = new ArrayList<>();

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    LostFoundItem item = deserializeItem(node);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }

            System.out.println("✅ Successfully loaded " + items.size() + " items from JSON");

            // Debug: Show item types
            long lostCount = items.stream().filter(item -> item instanceof LostItem).count();
            long foundCount = items.stream().filter(item -> item instanceof FoundItem).count();
            System.out.println("📊 Item breakdown: " + lostCount + " lost items, " + foundCount + " found items");

            return items;

        } catch (IOException e) {
            System.err.println("❌ Error loading items from JSON: " + e.getMessage());

            try {
                Path filePath = Paths.get(ITEMS_FILE);
                if (Files.exists(filePath)) {
                    createBackup(filePath, "items_error_backup.json");
                }
            } catch (Exception backupError) {
                System.err.println("Failed to create backup: " + backupError.getMessage());
            }

            saveItems(new ArrayList<>());
            return new ArrayList<>();
        }
    }

    // Helper method to deserialize items with proper type handling
    private LostFoundItem deserializeItem(JsonNode node) {
        try {
            // Check the type field to determine which subclass to use
            if (node.has("type")) {
                String type = node.get("type").asText();
                switch (type) {
                    case "lost":
                        return objectMapper.treeToValue(node, LostItem.class);
                    case "found":
                        return objectMapper.treeToValue(node, FoundItem.class);
                    default:
                        System.err.println("❌ Unknown item type: " + type);
                        return objectMapper.treeToValue(node, LostFoundItem.class);
                }
            } else {
                // Fallback: try to determine type based on available fields
                if (node.has("lostDate") || node.has("reward")) {
                    return objectMapper.treeToValue(node, LostItem.class);
                } else if (node.has("foundDate") || node.has("storageLocation")) {
                    return objectMapper.treeToValue(node, FoundItem.class);
                } else {
                    // Default to base class if type cannot be determined
                    return objectMapper.treeToValue(node, LostFoundItem.class);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error deserializing item: " + e.getMessage());
            return null;
        }
    }

    public boolean saveItems(List<LostFoundItem> items) {
        try {
            Path filePath = Paths.get(ITEMS_FILE);

            System.out.println("💾 Saving " + items.size() + " items to JSON:");
            for (LostFoundItem item : items) {
                String type = item instanceof LostItem ? "Lost" : "Found";
                System.out.println("   📝 " + type + " | " + item.getItemName() + " | " +
                        item.getStatus() + " | " + item.getReportedBy() + " | Verified: " + item.isVerified());
            }

            if (Files.exists(filePath)) {
                createBackup(filePath, "items_pre_save_backup.json");
            }

            Path tempFile = Paths.get(ITEMS_FILE + ".tmp");
            objectMapper.writeValue(tempFile.toFile(), items);

            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Successfully saved " + items.size() + " items to JSON");

            // Verify and log item types after save
            verifySavedItems(filePath);

            return verifySave(filePath, items.size());
        } catch (Exception e) {
            System.err.println("❌ Error saving items to JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Verify saved items by reading them back
    private void verifySavedItems(Path filePath) {
        try {
            if (Files.exists(filePath) && Files.size(filePath) > 0) {
                List<LostFoundItem> savedItems = loadItems();
                long lostCount = savedItems.stream().filter(item -> item instanceof LostItem).count();
                long foundCount = savedItems.stream().filter(item -> item instanceof FoundItem).count();
                System.out.println("🔍 Save verification: " + lostCount + " lost, " + foundCount + " found items persisted correctly");
            }
        } catch (Exception e) {
            System.err.println("❌ Save verification failed: " + e.getMessage());
        }
    }

    // Attempt to recover item data from corrupted JSON
    private List<LostFoundItem> attemptItemDataRecovery(String corruptedJson) {
        List<LostFoundItem> recoveredItems = new ArrayList<>();

        try {
            // Simple string parsing to extract item data
            String[] lines = corruptedJson.split("\n");
            LostFoundItem currentItem = null;
            String currentType = null;

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.contains("\"itemName\"")) {
                    // Save previous item if exists
                    if (currentItem != null) {
                        recoveredItems.add(currentItem);
                    }
                    // Reset for new item
                    currentItem = null;
                    currentType = null;
                } else if (trimmed.contains("\"type\"") && currentItem == null) {
                    currentType = extractValue(trimmed);
                    if ("lost".equals(currentType)) {
                        currentItem = new LostItem();
                    } else if ("found".equals(currentType)) {
                        currentItem = new FoundItem();
                    } else {
                        currentItem = new LostItem(); // Default to LostItem
                    }
                } else if (currentItem == null && (trimmed.contains("\"lostDate\"") || trimmed.contains("\"reward\""))) {
                    // Determine type from fields
                    currentItem = new LostItem();
                    currentType = "lost";
                } else if (currentItem == null && (trimmed.contains("\"foundDate\"") || trimmed.contains("\"storageLocation\""))) {
                    currentItem = new FoundItem();
                    currentType = "found";
                } else if (currentItem == null && trimmed.contains("{")) {
                    // Start of new object without type info, default to LostItem
                    currentItem = new LostItem();
                    currentType = "lost";
                }

                // Populate item fields
                if (currentItem != null) {
                    if (trimmed.contains("\"itemName\"")) {
                        currentItem.setItemName(extractValue(trimmed));
                    } else if (trimmed.contains("\"category\"")) {
                        currentItem.setCategory(extractValue(trimmed));
                    } else if (trimmed.contains("\"description\"")) {
                        currentItem.setDescription(extractValue(trimmed));
                    } else if (trimmed.contains("\"location\"")) {
                        currentItem.setLocation(extractValue(trimmed));
                    } else if (trimmed.contains("\"date\"")) {
                        currentItem.setDate(extractValue(trimmed));
                    } else if (trimmed.contains("\"status\"")) {
                        currentItem.setStatus(extractValue(trimmed));
                    } else if (trimmed.contains("\"reportedBy\"")) {
                        currentItem.setReportedBy(extractValue(trimmed));
                    } else if (trimmed.contains("\"verificationStatus\"")) {
                        currentItem.setVerificationStatus(extractValue(trimmed));
                    } else if (trimmed.contains("\"verifiedBy\"")) {
                        currentItem.setVerifiedBy(extractValue(trimmed));
                    } else if (trimmed.contains("\"verificationDate\"")) {
                        currentItem.setVerificationDate(extractValue(trimmed));
                    } else if (currentItem instanceof LostItem) {
                        LostItem lostItem = (LostItem) currentItem;
                        if (trimmed.contains("\"lostDate\"")) {
                            lostItem.setLostDate(extractValue(trimmed));
                        } else if (trimmed.contains("\"reward\"")) {
                            lostItem.setReward(extractValue(trimmed));
                        } else if (trimmed.contains("\"claimedBy\"")) {
                            lostItem.setClaimedBy(extractValue(trimmed));
                        } else if (trimmed.contains("\"claimStatus\"")) {
                            lostItem.setClaimStatus(extractValue(trimmed));
                        }
                    } else if (currentItem instanceof FoundItem) {
                        FoundItem foundItem = (FoundItem) currentItem;
                        if (trimmed.contains("\"foundDate\"")) {
                            foundItem.setFoundDate(extractValue(trimmed));
                        } else if (trimmed.contains("\"storageLocation\"")) {
                            foundItem.setStorageLocation(extractValue(trimmed));
                        } else if (trimmed.contains("\"claimedBy\"")) {
                            foundItem.setClaimedBy(extractValue(trimmed));
                        } else if (trimmed.contains("\"claimStatus\"")) {
                            foundItem.setClaimStatus(extractValue(trimmed));
                        }
                    }
                }
            }

            // Add the last item
            if (currentItem != null) {
                recoveredItems.add(currentItem);
            }

        } catch (Exception e) {
            System.err.println("❌ Could not recover item data from corrupted JSON: " + e.getMessage());
        }

        System.out.println("🔧 Total recovered items: " + recoveredItems.size());
        return recoveredItems;
    }

    // Attempt to recover user data from corrupted JSON
    private List<User> attemptUserDataRecovery(String corruptedJson) {
        List<User> recoveredUsers = new ArrayList<>();

        try {
            // Simple string parsing to extract user data
            String[] lines = corruptedJson.split("\n");
            User currentUser = null;

            for (String line : lines) {
                String trimmed = line.trim();

                if (trimmed.contains("\"username\"")) {
                    // Save previous user if exists
                    if (currentUser != null) {
                        recoveredUsers.add(currentUser);
                    }
                    // Start new user
                    currentUser = new User();
                    currentUser.setUsername(extractValue(trimmed));
                } else if (trimmed.contains("\"password\"") && currentUser != null) {
                    currentUser.setPassword(extractValue(trimmed));
                } else if (trimmed.contains("\"email\"") && currentUser != null) {
                    currentUser.setEmail(extractValue(trimmed));
                } else if (trimmed.contains("\"firstName\"") && currentUser != null) {
                    currentUser.setFirstName(extractValue(trimmed));
                } else if (trimmed.contains("\"lastName\"") && currentUser != null) {
                    currentUser.setLastName(extractValue(trimmed));
                } else if (trimmed.contains("\"role\"") && currentUser != null) {
                    currentUser.setRole(extractValue(trimmed));
                }
            }

            // Add the last user
            if (currentUser != null) {
                recoveredUsers.add(currentUser);
            }

        } catch (Exception e) {
            System.err.println("❌ Could not recover user data from corrupted JSON: " + e.getMessage());
        }

        System.out.println("🔧 Total recovered users: " + recoveredUsers.size());
        return recoveredUsers;
    }

    // Helper method to extract values from JSON lines
    private String extractValue(String jsonLine) {
        if (jsonLine == null || !jsonLine.contains(":")) {
            return null;
        }

        try {
            String[] parts = jsonLine.split(":", 2);
            if (parts.length == 2) {
                String value = parts[1].trim();
                // Remove trailing comma if present
                if (value.endsWith(",")) {
                    value = value.substring(0, value.length() - 1);
                }
                // Remove quotes
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value;
            }
        } catch (Exception e) {
            // Ignore extraction errors
        }
        return null;
    }

    // Helper method to validate JSON syntax
    private boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            System.err.println("❌ JSON Validation failed: " + e.getMessage());
            return false;
        }
    }

    private void createBackup(Path originalFile, String backupName) {
        try {
            Path backupFile = Paths.get(BACKUP_DIR + backupName);
            Files.copy(originalFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📋 Created backup: " + backupFile.getFileName());
        } catch (IOException e) {
            System.err.println("❌ Failed to create backup: " + e.getMessage());
        }
    }

    private boolean verifySave(Path filePath, int expectedCount) {
        try {
            if (!Files.exists(filePath)) {
                System.err.println("❌ File verification: file not found after save");
                return false;
            }

            long fileSize = Files.size(filePath);
            System.out.println("✅ File verification: " + fileSize + " bytes");

            if (fileSize == 0) {
                System.err.println("❌ File verification: file is empty after save");
                return false;
            }

            String content = Files.readString(filePath);
            if (!isValidJson(content)) {
                System.err.println("❌ File verification: saved content is not valid JSON");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("❌ File verification failed: " + e.getMessage());
            return false;
        }
    }

    public void debugFileOperations() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            Path itemsFile = Paths.get(ITEMS_FILE);
            Path usersFile = Paths.get(USERS_FILE);

            System.out.println("=== FILE OPERATIONS DEBUG ===");
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            System.out.println("Data directory: " + dataDir.toAbsolutePath());
            System.out.println("Items file: " + itemsFile.toAbsolutePath());
            System.out.println("Users file: " + usersFile.toAbsolutePath());
            System.out.println("Data directory exists: " + Files.exists(dataDir));
            System.out.println("Items file exists: " + Files.exists(itemsFile));
            System.out.println("Users file exists: " + Files.exists(usersFile));

            if (Files.exists(itemsFile)) {
                long fileSize = Files.size(itemsFile);
                System.out.println("Items file size: " + fileSize + " bytes");

                if (fileSize > 0) {
                    String content = Files.readString(itemsFile);
                    System.out.println("Items file content (first 500 chars): " +
                            content.substring(0, Math.min(content.length(), 500)));
                    System.out.println("JSON valid: " + isValidJson(content));

                    // Load and analyze items
                    List<LostFoundItem> items = loadItems();
                    System.out.println("Loaded items count: " + items.size());
                    for (LostFoundItem item : items) {
                        String type = item instanceof LostItem ? "Lost" : "Found";
                        System.out.println("  - " + type + ": " + item.getItemName() + " (" + item.getStatus() + ")");
                    }
                }
            }

            if (Files.exists(usersFile)) {
                long fileSize = Files.size(usersFile);
                System.out.println("Users file size: " + fileSize + " bytes");

                if (fileSize > 0) {
                    String content = Files.readString(usersFile);
                    System.out.println("Users file content (first 500 chars): " +
                            content.substring(0, Math.min(content.length(), 500)));
                    System.out.println("JSON valid: " + isValidJson(content));
                }
            }

            System.out.println("=== END DEBUG ===");
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
        }
    }

    public void resetAllData() {
        try {
            System.out.println("🔄 RESETTING ALL DATA FILES");

            Path itemsFile = Paths.get(ITEMS_FILE);
            Path usersFile = Paths.get(USERS_FILE);

            if (Files.exists(itemsFile)) {
                createBackup(itemsFile, "items_reset_backup.json");
                Files.delete(itemsFile);
                System.out.println("✅ Deleted items file");
            }

            if (Files.exists(usersFile)) {
                createBackup(usersFile, "users_reset_backup.json");
                Files.delete(usersFile);
                System.out.println("✅ Deleted users file");
            }

            // Create new empty files
            saveItems(new ArrayList<>());
            saveUsers(new ArrayList<>());

            System.out.println("✅ All data files reset successfully");

        } catch (Exception e) {
            System.err.println("❌ Error resetting data files: " + e.getMessage());
        }
    }
}
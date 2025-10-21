package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private static UserService instance;
    private final JsonDataService jsonDataService;
    private List<User> users;
    private User currentUser;

    public UserService() {
        this.jsonDataService = new JsonDataService();
        loadUsers();
        initializeDefaultUsers();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    private void loadUsers() {
        users = jsonDataService.loadUsers();
        System.out.println("Total users in memory after loading: " + users.size());
    }

    private void initializeDefaultUsers() {
        // Only add default users if no users exist
        if (users.isEmpty()) {
            System.out.println("Initializing default users...");
            users.add(new User("admin", "admin123", "admin@lostfound.com", "System", "Administrator", "admin"));
            users.add(new User("user", "user123", "user@lostfound.com", "Regular", "User", "user"));
            saveUsersToJson();
        } else {
            System.out.println("Users already exist, skipping default initialization");
            System.out.println("Current users:");
            for (User user : users) {
                System.out.println(" - " + user.getUsername() + " | " + user.getEmail());
            }
        }
    }

    private void saveUsersToJson() {
        System.out.println("Saving users to JSON...");
        boolean success = jsonDataService.saveUsers(users);
        if (success) {
            System.out.println("✓ Users saved successfully to JSON");
            // Verify the JSON file
            jsonDataService.verifyJsonFile();
            // Reload to verify data consistency
            List<User> reloadedUsers = jsonDataService.loadUsers();
            System.out.println("Verification - reloaded " + reloadedUsers.size() + " users from JSON");
        } else {
            System.out.println("✗ FAILED to save users to JSON");
        }
    }

    public boolean login(String username, String password) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username + ", Password: " + password);

        // Always reload from JSON to get latest data
        loadUsers();

        System.out.println("Available users (" + users.size() + "):");
        for (User user : users) {
            System.out.println(" - " + user.getUsername() + " : " + user.getPassword() + " | " + user.getEmail());
        }

        Optional<User> user = users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();

        if (user.isPresent()) {
            currentUser = user.get();
            System.out.println("✓ LOGIN SUCCESSFUL for user: " + username);
            return true;
        } else {
            System.out.println("✗ LOGIN FAILED for user: " + username);
            return false;
        }
    }

    public boolean register(String username, String password, String email, String firstName, String lastName) {
        System.out.println("=== REGISTRATION ===");
        System.out.println("Registering: " + username + ", " + email + ", " + firstName + " " + lastName);

        // Reload to get current state
        loadUsers();

        // Check if username exists
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            System.out.println("✗ Registration failed - Username already exists: " + username);
            return false;
        }

        // Check if email exists
        if (users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email))) {
            System.out.println("✗ Registration failed - Email already exists: " + email);
            return false;
        }

        // Create and add user
        User newUser = new User(username, password, email, firstName, lastName, "user");
        users.add(newUser);
        System.out.println("✓ User added to memory: " + username);

        // Save to JSON
        saveUsersToJson();

        // Force reload to ensure data is consistent
        loadUsers();

        // Verify the user was actually saved
        boolean userSaved = users.stream().anyMatch(u -> u.getUsername().equals(username));
        System.out.println("Final verification - user exists in system: " + userSaved);

        return userSaved;
    }

    public void logout() {
        currentUser = null;
        System.out.println("User logged out");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }

    public List<User> getAllUsers() {
        return List.copyOf(users);
    }

    // Method to reset data for testing
    public void resetData() {
        jsonDataService.resetJsonFile();
        users.clear();
        currentUser = null;
        initializeDefaultUsers();
    }
}
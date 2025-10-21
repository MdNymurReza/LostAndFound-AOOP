package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {
    private static UserService instance;
    private final JsonDataService jsonDataService;
    private List<User> users;
    private User currentUser;

    private UserService() {
        this.jsonDataService = new JsonDataService();
        loadUsers();

        // Create default admin user if no users exist
        if (users.isEmpty()) {
            createDefaultAdmin();
        }

        System.out.println("✅ UserService initialized with " + users.size() + " users");
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    private void loadUsers() {
        users = jsonDataService.loadUsers();
    }

    private void saveUsers() {
        jsonDataService.saveUsers(users);
    }

    private void createDefaultAdmin() {
        User adminUser = new User(
                "admin",
                "admin123",
                "admin@lostfound.com",
                "System",
                "Administrator",
                "admin"
        );
        users.add(adminUser);
        saveUsers();
        System.out.println("👤 Created default admin user");
    }

    public boolean registerUser(String username, String password, String email, String firstName, String lastName) {
        // Check if username already exists
        if (getUserByUsername(username).isPresent()) {
            return false;
        }

        User newUser = new User(username, password, email, firstName, lastName, "user");
        users.add(newUser);
        saveUsers();

        System.out.println("✅ New user registered: " + username);
        return true;
    }

    public boolean login(String username, String password) {
        Optional<User> userOpt = getUserByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password) && user.isActive()) {
                currentUser = user;
                user.updateLastLogin();
                saveUsers();

                System.out.println("✅ User logged in: " + username + " (" + user.getRole() + ")");
                return true;
            }
        }

        System.out.println("❌ Login failed for: " + username);
        return false;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("👋 User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Optional<User> getUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // Debug method
    public void debugUsers() {
        System.out.println("=== USERS DEBUG ===");
        System.out.println("Total users: " + users.size());
        for (User user : users) {
            System.out.println("👤 " + user.getUsername() + " | " + user.getEmail() + " | " + user.getRole() + " | Created: " + user.getCreatedAt());
        }
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "None"));
        System.out.println("=== END DEBUG ===");
    }
}
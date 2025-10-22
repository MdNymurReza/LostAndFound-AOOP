package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (users == null) {
            users = new ArrayList<>();
            System.out.println("⚠️  No users found, creating new user list");
        }
    }

    // UPDATED: Make saveUsers public and accept List parameter
    public boolean saveUsers(List<User> usersToSave) {
        this.users = new ArrayList<>(usersToSave);
        return jsonDataService.saveUsers(this.users);
    }

    // Keep the original saveUsers without parameters for backward compatibility
    public boolean saveUsers() {
        return jsonDataService.saveUsers(users);
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
        System.out.println("👤 Created default admin user: admin/admin123");
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
                System.out.println("🔐 Login details - Username: " + username + ", Password provided: " + password + ", Stored password: " + user.getPassword());
                return true;
            } else {
                System.out.println("❌ Password mismatch or inactive account for: " + username);
                System.out.println("🔐 Provided password: " + password + ", Stored password: " + user.getPassword() + ", Active: " + user.isActive());
            }
        } else {
            System.out.println("❌ User not found: " + username);
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

    // Enhanced debug method
    public void debugAdminUser() {
        System.out.println("=== ADMIN USER VERIFICATION ===");
        Optional<User> adminOpt = getUserByUsername("admin");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            System.out.println("✅ ADMIN USER FOUND:");
            System.out.println("   👤 Username: " + admin.getUsername());
            System.out.println("   🔑 Password: " + admin.getPassword());
            System.out.println("   🎯 Role: '" + admin.getRole() + "'");
            System.out.println("   👑 Is Admin: " + admin.isAdmin());
            System.out.println("   ✅ Active: " + admin.isActive());
            System.out.println("   📧 Email: " + admin.getEmail());
            System.out.println("   📅 Created: " + admin.getCreatedAt());
        } else {
            System.err.println("❌ ADMIN USER NOT FOUND! Creating default admin...");
            createDefaultAdmin();
            debugAdminUser(); // Recursive call to verify creation
        }
        System.out.println("=== END ADMIN VERIFICATION ===");
    }

    public void debugUserJsonData() {
        try {
            Path filePath = Paths.get("data/users.json");
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                System.out.println("=== CURRENT users.json CONTENT ===");
                System.out.println(content);
                System.out.println("=== END users.json CONTENT ===");
            } else {
                System.out.println("❌ users.json file does not exist!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading users.json: " + e.getMessage());
        }
    }

    // Debug method
    public void debugUsers() {
        System.out.println("=== USERS DEBUG ===");
        System.out.println("Total users: " + users.size());
        for (User user : users) {
            System.out.println("👤 " + user.getUsername() + " | " + user.getEmail() + " | " + user.getRole() + " | Admin: " + user.isAdmin() + " | Created: " + user.getCreatedAt());
        }
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() + " (Admin: " + currentUser.isAdmin() + ")" : "None"));
        System.out.println("=== END DEBUG ===");
    }

    public void refreshUsers() {
        loadUsers();
        System.out.println("🔄 Users refreshed from JSON file");
    }
}
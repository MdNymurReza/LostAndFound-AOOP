package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class User {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String createdAt;
    private String lastLogin;
    private boolean active;

    // Required no-arg constructor for Jackson
    public User() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.active = true;
    }

    public User(String username, String password, String email, String firstName, String lastName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role != null ? role : "user";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Helper Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getInitials() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return String.valueOf(firstName.charAt(0)) + lastName.charAt(0);
        } else if (firstName != null && !firstName.isEmpty()) {
            return String.valueOf(firstName.charAt(0));
        } else if (username != null && !username.isEmpty()) {
            return String.valueOf(username.charAt(0));
        }
        return "U";
    }

    // Role-based Methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "user".equalsIgnoreCase(role) || role == null || role.isEmpty();
    }

    public boolean isModerator() {
        return "moderator".equalsIgnoreCase(role);
    }

    public boolean canManageItems() {
        return isAdmin() || isModerator();
    }

    public boolean canVerifyItems() {
        return isAdmin() || isModerator();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    // Status Methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Validation Methods
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                role != null && !role.trim().isEmpty();
    }

    public boolean hasRequiredFields() {
        return username != null && password != null && email != null &&
                firstName != null && lastName != null && role != null;
    }

    public boolean isEmailValid() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean isUsernameValid() {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    // Security Methods
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Profile Methods
    public String getDisplayName() {
        return getFullName() + " (" + username + ")";
    }

    public String getRoleDisplay() {
        if (isAdmin()) return "Administrator";
        if (isModerator()) return "Moderator";
        return "User";
    }

    // Utility Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', email='%s', role='%s', active=%s, created=%s}",
                username, email, role, active, createdAt);
    }
}
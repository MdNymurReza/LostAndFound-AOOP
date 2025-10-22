package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

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

    // NEW: Reward system fields
    private int rewardPoints;
    private int itemsReturned;
    private String rewardTier;

    // Required no-arg constructor for Jackson
    public User() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.active = true;
        this.rewardPoints = 0;
        this.itemsReturned = 0;
        this.rewardTier = "Bronze";
    }

    public User(String username, String password, String email, String firstName, String lastName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role != null ? role : "user";
        System.out.println("👤 User created: " + username + " with role: '" + this.role + "'");
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
    public void setRole(String role) {
        this.role = role;
        System.out.println("🎯 Role set to: '" + role + "' for user: " + username);
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // NEW: Reward system getters and setters
    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
        updateRewardTier();
    }

    public int getItemsReturned() { return itemsReturned; }
    public void setItemsReturned(int itemsReturned) { this.itemsReturned = itemsReturned; }

    public String getRewardTier() { return rewardTier; }
    public void setRewardTier(String rewardTier) { this.rewardTier = rewardTier; }

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

    // Role-based Methods - FIXED VERSION
    public boolean isAdmin() {
        if (role == null) {
            System.out.println("⚠️  Role is null for user: " + username);
            return false;
        }

        // Trim and case-insensitive comparison
        String trimmedRole = role.trim().toLowerCase();
        boolean isAdmin = "admin".equals(trimmedRole);

        System.out.println("🔍 User.isAdmin() check: " + username +
                " | Role: '" + role + "' -> '" + trimmedRole + "' | isAdmin: " + isAdmin);
        return isAdmin;
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

    // NEW: Reward System Methods
    public void addRewardPoints(int points) {
        this.rewardPoints += points;
        updateRewardTier();
        System.out.println("🎁 Added " + points + " reward points to user: " + username + " | Total: " + rewardPoints);
    }

    public void incrementItemsReturned() {
        this.itemsReturned++;
        System.out.println("📦 Incremented returned items count for user: " + username + " | Total: " + itemsReturned);
    }

    private void updateRewardTier() {
        if (rewardPoints >= 1000) {
            rewardTier = "Platinum";
        } else if (rewardPoints >= 500) {
            rewardTier = "Gold";
        } else if (rewardPoints >= 200) {
            rewardTier = "Silver";
        } else {
            rewardTier = "Bronze";
        }
    }

    public int calculateRandomReward() {
        // Base points + random bonus
        Random random = new Random();
        int basePoints = 50;
        int randomBonus = random.nextInt(101); // 0-100 random bonus

        // Bonus for being an active user
        int activityBonus = (itemsReturned > 5) ? 20 : 0;

        return basePoints + randomBonus + activityBonus;
    }

    public int calculateRewardWithBonus(boolean hadReward) {
        int baseReward = calculateRandomReward();

        // Additional bonus if the lost item had a reward offer
        if (hadReward) {
            baseReward += 25; // Bonus for offering reward
            System.out.println("💰 Reward bonus applied: +25 points for offering reward");
        }

        return baseReward;
    }

    public String getTierBenefits() {
        switch (rewardTier) {
            case "Platinum":
                return "• Priority support\n• Exclusive features\n• Maximum rewards (2x points)\n• Instant verification\n• Dedicated account manager";
            case "Gold":
                return "• Faster verification\n• Bonus points (1.5x)\n• Premium features\n• Early access to new features";
            case "Silver":
                return "• Quick responses\n• Extra points (1.25x)\n• Enhanced visibility\n• Priority listing";
            default:
                return "• Standard benefits\n• Basic rewards\n• Community support\n• Regular verification";
        }
    }

    public double getTierMultiplier() {
        switch (rewardTier) {
            case "Platinum": return 2.0;
            case "Gold": return 1.5;
            case "Silver": return 1.25;
            default: return 1.0;
        }
    }

    public String getNextTierInfo() {
        int pointsNeeded = 0;
        String nextTier = "";

        switch (rewardTier) {
            case "Bronze":
                pointsNeeded = 200 - rewardPoints;
                nextTier = "Silver";
                break;
            case "Silver":
                pointsNeeded = 500 - rewardPoints;
                nextTier = "Gold";
                break;
            case "Gold":
                pointsNeeded = 1000 - rewardPoints;
                nextTier = "Platinum";
                break;
            case "Platinum":
                return "You've reached the highest tier! 🏆";
            default:
                pointsNeeded = 200 - rewardPoints;
                nextTier = "Silver";
        }

        return pointsNeeded > 0 ?
                String.format("Need %d more points to reach %s tier", pointsNeeded, nextTier) :
                "Congratulations! You've reached " + nextTier + " tier!";
    }

    public String getRewardSummary() {
        return String.format(
                "🏆 %s Tier | 📊 %d Points | 📦 %d Items Returned",
                rewardTier, rewardPoints, itemsReturned
        );
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

    // NEW: Enhanced display with rewards
    public String getDisplayNameWithTier() {
        return getFullName() + " 🏆 " + rewardTier;
    }

    public String getProfileSummary() {
        return String.format(
                "👤 %s %s\n📧 %s\n💎 %s Tier\n🏆 %d Points\n📦 %d Items Returned",
                firstName, lastName, email, rewardTier, rewardPoints, itemsReturned
        );
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
        return String.format(
                "User{username='%s', email='%s', role='%s', active=%s, rewardTier='%s', rewardPoints=%d, itemsReturned=%d, created=%s}",
                username, email, role, active, rewardTier, rewardPoints, itemsReturned, createdAt
        );
    }
}
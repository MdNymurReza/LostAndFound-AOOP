package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LostFoundItem {
    private String itemName;
    private String category;
    private String description;
    private String location;
    private String date;
    private String status; // "lost", "found", "returned"
    private String reportedBy;
    private boolean verified;
    private String verifiedBy;
    private String verificationDate;
    private String contactInfo;
    private String imagePath;
    private String uniqueId;

    // Required no-arg constructor for Jackson
    public LostFoundItem() {
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.verified = false;
        this.uniqueId = generateUniqueId();
    }

    public LostFoundItem(String itemName, String category, String description, String location,
                         String status, String reportedBy) {
        this();
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.status = status;
        this.reportedBy = reportedBy;
    }

    // Generate unique ID for the item
    private String generateUniqueId() {
        return "ITEM_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerificationDate() { return verificationDate; }
    public void setVerificationDate(String verificationDate) { this.verificationDate = verificationDate; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    // Helper methods for status
    public boolean isLost() {
        return "lost".equalsIgnoreCase(status);
    }

    public boolean isFound() {
        return "found".equalsIgnoreCase(status);
    }

    public boolean isReturned() {
        return "returned".equalsIgnoreCase(status);
    }

    // Verification methods
    public void verify(String adminUsername) {
        this.verified = true;
        this.verifiedBy = adminUsername;
        this.verificationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void unverify() {
        this.verified = false;
        this.verifiedBy = null;
        this.verificationDate = null;
    }

    // Validation methods
    public boolean isValid() {
        return itemName != null && !itemName.trim().isEmpty() &&
                category != null && !category.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                location != null && !location.trim().isEmpty() &&
                status != null && !status.trim().isEmpty() &&
                reportedBy != null && !reportedBy.trim().isEmpty();
    }

    public boolean canBeReturned() {
        return isFound() && isVerified() && !isReturned();
    }

    // Display methods
    public String getStatusDisplay() {
        if (isReturned()) return "Returned";
        if (isLost()) return "Lost";
        if (isFound()) return "Found";
        return "Unknown";
    }

    public String getVerificationStatus() {
        return verified ? "Verified ✓" : "Pending Verification ⏳";
    }

    public String getDetailedInfo() {
        return String.format(
                "Item: %s\nCategory: %s\nDescription: %s\nLocation: %s\nStatus: %s\nReported By: %s\nVerified: %s%s",
                itemName, category, description, location, getStatusDisplay(), reportedBy,
                verified ? "Yes" : "No",
                verified ? "\nVerified By: " + verifiedBy + "\nDate: " + verificationDate : ""
        );
    }

    // Utility methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LostFoundItem that = (LostFoundItem) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }

    @Override
    public String toString() {
        return String.format(
                "LostFoundItem{name='%s', category='%s', status='%s', reportedBy='%s', verified=%s}",
                itemName, category, status, reportedBy, verified
        );
    }
}
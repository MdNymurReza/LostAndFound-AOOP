package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class LostFoundItem {
    protected String id;
    protected String itemName;
    protected String category;
    protected String description;
    protected String location;
    protected String date;
    protected String reportedBy;
    protected String status; // "pending", "verified", "rejected", "claimed", "returned"
    protected String verificationStatus; // "pending", "verified", "rejected"
    protected String verifiedBy;
    protected String verificationDate;
    protected String createdAt;
    protected String imagePath;
    protected String contactInfo;

    public LostFoundItem() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "pending";
        this.verificationStatus = "pending";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public String getVerificationDate() { return verificationDate; }
    public void setVerificationDate(String verificationDate) { this.verificationDate = verificationDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    // Verification methods
    public boolean isVerified() {
        return "verified".equals(verificationStatus);
    }

    public boolean isPendingVerification() {
        return "pending".equals(verificationStatus);
    }

    public boolean isRejected() {
        return "rejected".equals(verificationStatus);
    }

    public void verify(String adminUsername) {
        this.verificationStatus = "verified";
        this.verifiedBy = adminUsername;
        this.verificationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "verified";
    }

    public void reject(String adminUsername) {
        this.verificationStatus = "rejected";
        this.verifiedBy = adminUsername;
        this.verificationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "rejected";
    }

    public abstract String getType();
}
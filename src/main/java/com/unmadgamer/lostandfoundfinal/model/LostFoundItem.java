package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class LostFoundItem {
    protected String id;
    protected String itemName;
    protected String category;
    protected String description;
    protected String location;
    protected String date;
    protected String reportedBy;
    protected String status; // "active", "returned", "expired"
    protected String verificationStatus; // "pending", "verified", "rejected"
    protected String verifiedBy;
    protected String verificationDate;
    protected String createdAt;

    // Abstract method to get item type
    public abstract String getType();

    public LostFoundItem() {
        this.id = UUID.randomUUID().toString();
        this.status = "active";
        this.verificationStatus = "pending";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public LostFoundItem(String itemName, String category, String description, String location,
                         String date, String reportedBy) {
        this();
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
        this.reportedBy = reportedBy;
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

    // Business logic methods
    public boolean isVerified() {
        return "verified".equals(verificationStatus);
    }

    public boolean isPendingVerification() {
        return "pending".equals(verificationStatus);
    }

    public boolean isRejected() {
        return "rejected".equals(verificationStatus);
    }

    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isReturned() {
        return "returned".equals(status);
    }

    @Override
    public String toString() {
        return String.format(
                "LostFoundItem{id='%s', itemName='%s', category='%s', type='%s', status='%s', verificationStatus='%s', reportedBy='%s'}",
                id, itemName, category, getType(), status, verificationStatus, reportedBy
        );
    }
}
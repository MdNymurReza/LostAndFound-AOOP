package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDate;

public class FoundItem {
    private String id;
    private String itemName;
    private String category;
    private String description;
    private LocalDate date;
    private String location;
    private String status; // "lost" or "found"
    private String reportedBy;
    private LocalDate reportedDate;
    private boolean isReturned;
    private String contactInfo;

    public FoundItem() {
        this.id = generateId();
        this.reportedDate = LocalDate.now();
        this.isReturned = false;
    }

    public FoundItem(String itemName, String category, String description, LocalDate date,
                     String location, String status, String reportedBy, String contactInfo) {
        this();
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.date = date;
        this.location = location;
        this.status = status;
        this.reportedBy = reportedBy;
        this.contactInfo = contactInfo;
    }

    private String generateId() {
        return "ITEM_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public LocalDate getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDate reportedDate) { this.reportedDate = reportedDate; }

    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { isReturned = returned; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", itemName, category, status);
    }
}
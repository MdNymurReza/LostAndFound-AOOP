package com.unmadgamer.lostandfoundfinal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LostFoundItem {
    private String id;
    private String itemName;
    private String category;
    private String description;
    private String date; // Store as String
    private String location;
    private String status;
    private String reportedBy;
    private String reportedDate; // Store as String
    private boolean isReturned;
    private String contactInfo;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LostFoundItem() {
        this.id = generateId();
        this.reportedDate = LocalDate.now().format(DATE_FORMATTER);
        this.isReturned = false;
    }

    public LostFoundItem(String itemName, String category, String description, LocalDate date,
                         String location, String status, String reportedBy, String contactInfo) {
        this();
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        setDateFromLocalDate(date); // Convert LocalDate to String
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

    // Date as String
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // Helper methods for LocalDate conversion - tell Jackson to ignore these
    @JsonIgnore
    public LocalDate getDateAsLocalDate() {
        try {
            return date != null ? LocalDate.parse(date, DATE_FORMATTER) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public void setDateFromLocalDate(LocalDate localDate) {
        this.date = localDate != null ? localDate.format(DATE_FORMATTER) : null;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getReportedDate() { return reportedDate; }
    public void setReportedDate(String reportedDate) { this.reportedDate = reportedDate; }

    @JsonIgnore
    public LocalDate getReportedDateAsLocalDate() {
        try {
            return reportedDate != null ? LocalDate.parse(reportedDate, DATE_FORMATTER) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isReturned() { return isReturned; }
    public void setReturned(boolean returned) { isReturned = returned; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", itemName, category, status);
    }
}
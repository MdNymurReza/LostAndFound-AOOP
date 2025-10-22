package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FoundItem extends LostFoundItem {
    private String foundDate;
    private String storageLocation;
    private String contactInfo; // Added contact info field
    private String claimedBy; // Username of user who claimed this found item
    private String claimStatus; // "none", "pending", "approved", "rejected"

    public FoundItem() {
        super();
    }

    public FoundItem(String itemName, String category, String description, String location,
                     String date, String reportedBy, String foundDate, String storageLocation, String contactInfo) {
        super(itemName, category, description, location, date, reportedBy);
        this.foundDate = foundDate;
        this.storageLocation = storageLocation;
        this.contactInfo = contactInfo;
        this.claimStatus = "none";
    }

    // Implement the abstract method
    @Override
    public String getType() {
        return "found";
    }

    // Getters and Setters
    public String getFoundDate() { return foundDate; }
    public void setFoundDate(String foundDate) { this.foundDate = foundDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    // Business logic methods
    public boolean canBeClaimed() {
        return isVerified() &&
                "none".equals(this.claimStatus) &&
                !isReturned();
    }

    public boolean isClaimPending() {
        return "pending".equals(claimStatus);
    }

    public boolean isClaimApproved() {
        return "approved".equals(claimStatus);
    }

    public boolean isClaimRejected() {
        return "rejected".equals(claimStatus);
    }

    public void claimItem(String claimantUsername) {
        if (canBeClaimed()) {
            this.claimedBy = claimantUsername;
            this.claimStatus = "pending";
            System.out.println("✅ Found item '" + itemName + "' claimed by: " + claimantUsername);
        }
    }

    public void approveClaim() {
        this.claimStatus = "approved";
        this.status = "returned";
    }

    public void rejectClaim() {
        this.claimStatus = "rejected";
        this.claimedBy = null;
    }

    @Override
    public String toString() {
        return String.format(
                "FoundItem{id='%s', itemName='%s', category='%s', foundDate='%s', storageLocation='%s', contactInfo='%s', status='%s', claimStatus='%s', reportedBy='%s'}",
                id, itemName, category, foundDate, storageLocation, contactInfo, status, claimStatus, reportedBy
        );
    }
}
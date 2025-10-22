package com.unmadgamer.lostandfoundfinal.model;

public class LostItem extends LostFoundItem {
    private String lostDate;
    private String reward;
    private String contactInfo;
    private String claimedBy;
    private String claimedFoundItemId;
    private String claimStatus;

    public LostItem() {
        super();
    }

    public LostItem(String itemName, String category, String description, String location,
                    String date, String reportedBy, String lostDate, String reward, String contactInfo) {
        super(itemName, category, description, location, date, reportedBy);
        this.lostDate = lostDate;
        this.reward = reward;
        this.contactInfo = contactInfo;
        this.claimStatus = "none";
    }

    @Override
    public String getType() {
        return "lost";
    }

    // Getters and Setters
    public String getLostDate() { return lostDate; }
    public void setLostDate(String lostDate) { this.lostDate = lostDate; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }

    public String getClaimedFoundItemId() { return claimedFoundItemId; }
    public void setClaimedFoundItemId(String claimedFoundItemId) { this.claimedFoundItemId = claimedFoundItemId; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    // Business logic methods
    public boolean canClaimFoundItem() {
        return isVerified() &&
                "none".equals(this.claimStatus) &&
                !isReturned();
    }

    public void claimFoundItem(String foundItemId) {
        if (canClaimFoundItem()) {
            this.claimedFoundItemId = foundItemId;
            this.claimStatus = "pending";
        }
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

    public void approveClaim() {
        this.claimStatus = "approved";
        this.status = "returned";
    }

    public void rejectClaim() {
        this.claimStatus = "rejected";
        this.claimedBy = null;
        this.claimedFoundItemId = null;
    }

    @Override
    public String toString() {
        return String.format(
                "LostItem{id='%s', itemName='%s', category='%s', lostDate='%s', reward='%s', contactInfo='%s', status='%s', claimStatus='%s', reportedBy='%s'}",
                id, itemName, category, lostDate, reward, contactInfo, status, claimStatus, reportedBy
        );
    }
}
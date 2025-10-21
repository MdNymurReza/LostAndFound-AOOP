package com.unmadgamer.lostandfoundfinal.model;

public class LostItem extends LostFoundItem {
    private String lostDate;
    private String reward;
    private String claimedBy;
    private String claimStatus; // "pending", "approved", "rejected"

    public LostItem() {
        super();
        this.claimStatus = "pending";
    }

    public String getLostDate() { return lostDate; }
    public void setLostDate(String lostDate) { this.lostDate = lostDate; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    @Override
    public String getType() {
        return "lost";
    }

    // Claim methods
    public void claimItem(String claimantUsername) {
        this.claimedBy = claimantUsername;
        this.claimStatus = "pending";
    }

    public void approveClaim(String adminUsername) {
        this.claimStatus = "approved";
        this.status = "claimed";
        this.verifiedBy = adminUsername;
    }

    public void rejectClaim(String adminUsername) {
        this.claimStatus = "rejected";
        this.claimedBy = null;
    }
}
package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;
import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemService {
    private static ItemService instance;
    private final JsonDataService jsonDataService;
    private final UserService userService;
    private List<LostFoundItem> items;

    private ItemService() {
        this.jsonDataService = new JsonDataService();
        this.userService = UserService.getInstance();
        loadItems();
        System.out.println("✅ ItemService initialized with " + items.size() + " items");
        debugCurrentItems(); // Debug on startup
    }

    public static synchronized ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }

    private void loadItems() {
        items = jsonDataService.loadItems();
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    private void saveItems() {
        boolean success = jsonDataService.saveItems(items);
        if (success) {
            System.out.println("✅ Items saved successfully. Total items: " + items.size());
        } else {
            System.err.println("❌ Failed to save items!");
        }
    }

    // Add new items
    public boolean addLostItem(LostItem lostItem) {
        items.add(lostItem);
        saveItems();
        System.out.println("✅ Lost item added: " + lostItem.getItemName());
        debugCurrentItems(); // Debug after adding
        return true;
    }

    public boolean addFoundItem(FoundItem foundItem) {
        items.add(foundItem);
        saveItems();
        System.out.println("✅ Found item added: " + foundItem.getItemName());
        debugCurrentItems(); // Debug after adding
        return true;
    }

    // Get items by type
    public List<LostItem> getLostItems() {
        return items.stream()
                .filter(item -> item instanceof LostItem)
                .map(item -> (LostItem) item)
                .collect(Collectors.toList());
    }

    public List<FoundItem> getFoundItems() {
        return items.stream()
                .filter(item -> item instanceof FoundItem)
                .map(item -> (FoundItem) item)
                .collect(Collectors.toList());
    }

    // Get available items for claiming (only verified and active items)
    public List<LostFoundItem> getAvailableLostItems() {
        List<LostFoundItem> availableItems = items.stream()
                .filter(item -> item instanceof LostItem)
                .filter(item -> item.isVerified() && item.isActive())
                .collect(Collectors.toList());

        System.out.println("🔍 Available lost items: " + availableItems.size());
        return availableItems;
    }

    public List<LostFoundItem> getAvailableFoundItems() {
        List<LostFoundItem> availableItems = items.stream()
                .filter(item -> item instanceof FoundItem)
                .filter(item -> item.isVerified() && item.isActive())
                .collect(Collectors.toList());

        System.out.println("🔍 Available found items: " + availableItems.size());
        return availableItems;
    }

    // Get items for current user
    public List<LostFoundItem> getLostItemsByUser(String username) {
        return items.stream()
                .filter(item -> item instanceof LostItem)
                .filter(item -> username.equals(item.getReportedBy()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getFoundItemsByUser(String username) {
        return items.stream()
                .filter(item -> item instanceof FoundItem)
                .filter(item -> username.equals(item.getReportedBy()))
                .collect(Collectors.toList());
    }

    // Simple claim item method
    public boolean claimItem(String itemId, String claimant) {
        Optional<LostFoundItem> itemOpt = items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();

            // Check if item can be claimed
            if (item.isVerified() && item.isActive()) {
                // Set item as claimed based on type
                if (item instanceof LostItem) {
                    LostItem lostItem = (LostItem) item;
                    if (lostItem.canClaimFoundItem()) {
                        lostItem.setClaimStatus("pending");
                        lostItem.setClaimedBy(claimant);
                        lostItem.setStatus("claimed");
                    }
                } else if (item instanceof FoundItem) {
                    FoundItem foundItem = (FoundItem) item;
                    if (foundItem.canBeClaimed()) {
                        foundItem.claimItem(claimant);
                        foundItem.setStatus("claimed");
                    }
                }

                saveItems();
                System.out.println("✅ Item claimed: " + item.getItemName() + " by " + claimant);
                return true;
            }
        }
        return false;
    }

    // Get items for admin verification
    public List<LostFoundItem> getPendingVerificationItems() {
        return items.stream()
                .filter(item -> item.isPendingVerification())
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getVerifiedItems() {
        return items.stream()
                .filter(item -> item.isVerified())
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getPendingClaimItems() {
        List<LostFoundItem> pendingClaims = new ArrayList<>();

        // Get lost items with pending claims
        pendingClaims.addAll(getLostItems().stream()
                .filter(LostItem::isClaimPending)
                .collect(Collectors.toList()));

        // Get found items with pending claims
        pendingClaims.addAll(getFoundItems().stream()
                .filter(FoundItem::isClaimPending)
                .collect(Collectors.toList()));

        return pendingClaims;
    }

    // NEW: Get successfully returned items
    public List<LostFoundItem> getReturnedItems() {
        return items.stream()
                .filter(item -> "returned".equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    // NEW: Get returned items by user
    public List<LostFoundItem> getReturnedItemsByUser(String username) {
        return items.stream()
                .filter(item -> "returned".equals(item.getStatus()))
                .filter(item -> {
                    if (item instanceof LostItem) {
                        LostItem lostItem = (LostItem) item;
                        return username.equals(lostItem.getClaimedBy()) || username.equals(item.getReportedBy());
                    } else if (item instanceof FoundItem) {
                        FoundItem foundItem = (FoundItem) item;
                        return username.equals(foundItem.getClaimedBy()) || username.equals(item.getReportedBy());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // Verification methods
    public boolean verifyItem(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            item.setVerificationStatus("verified");
            item.setVerifiedBy(adminUsername);
            item.setVerificationDate(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            saveItems();
            System.out.println("✅ Item verified: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        return false;
    }

    public boolean rejectItem(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            item.setVerificationStatus("rejected");
            item.setVerifiedBy(adminUsername);
            item.setVerificationDate(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            saveItems();
            System.out.println("❌ Item rejected: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        return false;
    }

    // NEW: Complete Reward System for Successful Returns
    public boolean completeSuccessfulReturn(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                if (lostItem.isClaimPending()) {
                    // Reward the user who found and returned the item
                    User finder = userService.getUserByUsername(lostItem.getClaimedBy()).orElse(null);
                    if (finder != null) {
                        int rewardPoints = 50; // Base reward points
                        finder.addRewardPoints(rewardPoints);
                        finder.incrementItemsReturned();

                        // Update item status
                        lostItem.setStatus("returned");
                        lostItem.setClaimStatus("approved");

                        saveItems();
                        userService.saveUsers(); // Save user data to persist rewards

                        System.out.println("🎁 Rewarded " + finder.getUsername() + " with " + rewardPoints +
                                " points for returning: " + lostItem.getItemName());
                        return true;
                    }
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                if (foundItem.isClaimPending()) {
                    // Reward the user who reported the found item
                    User reporter = userService.getUserByUsername(foundItem.getReportedBy()).orElse(null);
                    if (reporter != null) {
                        int rewardPoints = 50; // Base reward points
                        reporter.addRewardPoints(rewardPoints);
                        reporter.incrementItemsReturned();

                        // Update item status
                        foundItem.setStatus("returned");
                        foundItem.setClaimStatus("approved");

                        saveItems();
                        userService.saveUsers(); // Save user data to persist rewards

                        System.out.println("🎁 Rewarded " + reporter.getUsername() + " with " + rewardPoints +
                                " points for helping return: " + foundItem.getItemName());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Claim approval methods
    public boolean approveClaim(String itemId, String adminUsername) {
        return completeSuccessfulReturn(itemId, adminUsername);
    }

    public boolean rejectClaim(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                if (lostItem.isClaimPending()) {
                    lostItem.rejectClaim();
                    lostItem.setStatus("active"); // Make item available again
                    saveItems();
                    return true;
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                if (foundItem.isClaimPending()) {
                    foundItem.rejectClaim();
                    foundItem.setStatus("active"); // Make item available again
                    saveItems();
                    return true;
                }
            }
        }
        return false;
    }

    // Statistics
    public long getPendingVerificationCount() {
        return getPendingVerificationItems().size();
    }

    public long getVerifiedTodayCount() {
        String today = java.time.LocalDate.now().toString();
        return items.stream()
                .filter(item -> today.equals(item.getVerificationDate()))
                .count();
    }

    public long getTotalVerifiedCount() {
        return items.stream()
                .filter(item -> item.isVerified())
                .count();
    }

    public long getReturnedItemsCount() {
        return getReturnedItems().size();
    }

    public double getVerificationRate() {
        long total = items.size();
        long verified = getTotalVerifiedCount();
        return total > 0 ? (verified * 100.0 / total) : 100.0;
    }

    // Helper methods
    public Optional<LostFoundItem> getItemById(String id) {
        return items.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst();
    }

    public List<LostFoundItem> getAllItems() {
        return new ArrayList<>(items);
    }

    // Debug method to see current items
    public void debugCurrentItems() {
        System.out.println("=== CURRENT ITEMS DEBUG ===");
        System.out.println("Total items in memory: " + items.size());

        List<LostItem> lostItems = getLostItems();
        List<FoundItem> foundItems = getFoundItems();

        System.out.println("📦 Lost items: " + lostItems.size());
        for (LostItem item : lostItems) {
            System.out.println("   - " + item.getItemName() +
                    " | Status: " + item.getStatus() +
                    " | Claimed by: " + item.getClaimedBy() +
                    " | Verified: " + item.isVerified());
        }

        System.out.println("📦 Found items: " + foundItems.size());
        for (FoundItem item : foundItems) {
            System.out.println("   - " + item.getItemName() +
                    " | Status: " + item.getStatus() +
                    " | Claimed by: " + item.getClaimedBy() +
                    " | Verified: " + item.isVerified());
        }

        System.out.println("🔍 Available lost items: " + getAvailableLostItems().size());
        System.out.println("🔍 Available found items: " + getAvailableFoundItems().size());
        System.out.println("⏳ Pending verification: " + getPendingVerificationCount());
        System.out.println("✅ Returned items: " + getReturnedItemsCount());
        System.out.println("=== END DEBUG ===");
    }

    // Force refresh items from JSON file
    public void refreshItems() {
        loadItems();
        System.out.println("🔄 Items refreshed from JSON file");
        debugCurrentItems();
    }
}
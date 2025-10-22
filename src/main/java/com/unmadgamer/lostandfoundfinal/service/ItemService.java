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
                        System.out.println("✅ Lost item claimed: " + item.getItemName() + " by " + claimant);
                    }
                } else if (item instanceof FoundItem) {
                    FoundItem foundItem = (FoundItem) item;
                    if (foundItem.canBeClaimed()) {
                        foundItem.claimItem(claimant);
                        foundItem.setStatus("claimed");
                        System.out.println("✅ Found item claimed: " + item.getItemName() + " by " + claimant);
                    }
                }

                saveItems();
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

    // Get successfully returned items
    public List<LostFoundItem> getReturnedItems() {
        return items.stream()
                .filter(item -> "returned".equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    // Get returned items by user
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

    // FIXED: Complete Reward System for Successful Returns
    public boolean completeSuccessfulReturn(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            System.out.println("🎯 Processing return for item: " + item.getItemName() + " | Type: " + item.getType());

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                System.out.println("🔍 Lost Item - Claimed By: " + lostItem.getClaimedBy() + " | Claim Status: " + lostItem.getClaimStatus());

                if (lostItem.isClaimPending() && lostItem.getClaimedBy() != null) {
                    // Reward the user who found and returned the item (the claimant)
                    User finder = userService.getUserByUsername(lostItem.getClaimedBy()).orElse(null);
                    if (finder != null) {
                        int rewardPoints = 50; // Base reward points
                        finder.addRewardPoints(rewardPoints);
                        finder.incrementItemsReturned();

                        // Update item status
                        lostItem.setStatus("returned");
                        lostItem.setClaimStatus("approved");
                        lostItem.setVerificationStatus("verified"); // Ensure it's verified

                        saveItems();
                        userService.saveUsers(); // Save user data to persist rewards

                        System.out.println("🎁 SUCCESS: Rewarded " + finder.getUsername() +
                                " with " + rewardPoints + " points for returning: " + lostItem.getItemName());
                        System.out.println("📊 User now has: " + finder.getRewardPoints() + " points and " +
                                finder.getItemsReturned() + " items returned");
                        return true;
                    } else {
                        System.err.println("❌ ERROR: Could not find user: " + lostItem.getClaimedBy());
                    }
                } else {
                    System.err.println("❌ ERROR: Lost item not in claimable state - Pending: " +
                            lostItem.isClaimPending() + " | ClaimedBy: " + lostItem.getClaimedBy());
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                System.out.println("🔍 Found Item - Claimed By: " + foundItem.getClaimedBy() + " | Claim Status: " + foundItem.getClaimStatus());

                if (foundItem.isClaimPending() && foundItem.getClaimedBy() != null) {
                    // For found items, reward the user who reported the found item
                    User reporter = userService.getUserByUsername(foundItem.getReportedBy()).orElse(null);
                    if (reporter != null) {
                        int rewardPoints = 50; // Base reward points
                        reporter.addRewardPoints(rewardPoints);
                        reporter.incrementItemsReturned();

                        // Update item status
                        foundItem.setStatus("returned");
                        foundItem.setClaimStatus("approved");
                        foundItem.setVerificationStatus("verified"); // Ensure it's verified

                        saveItems();
                        userService.saveUsers(); // Save user data to persist rewards

                        System.out.println("🎁 SUCCESS: Rewarded " + reporter.getUsername() +
                                " with " + rewardPoints + " points for helping return: " + foundItem.getItemName());
                        System.out.println("📊 User now has: " + reporter.getRewardPoints() + " points and " +
                                reporter.getItemsReturned() + " items returned");
                        return true;
                    } else {
                        System.err.println("❌ ERROR: Could not find user: " + foundItem.getReportedBy());
                    }
                } else {
                    System.err.println("❌ ERROR: Found item not in claimable state - Pending: " +
                            foundItem.isClaimPending() + " | ClaimedBy: " + foundItem.getClaimedBy());
                }
            }
        } else {
            System.err.println("❌ ERROR: Item not found with ID: " + itemId);
        }
        return false;
    }

    // Claim approval methods
    public boolean approveClaim(String itemId, String adminUsername) {
        System.out.println("🔄 Approving claim for item: " + itemId);
        boolean result = completeSuccessfulReturn(itemId, adminUsername);
        if (result) {
            System.out.println("✅ Claim approved successfully");
        } else {
            System.err.println("❌ Failed to approve claim");
        }
        return result;
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

    // NEW: Debug method for reward system
    public void debugRewardFlow(String itemId) {
        System.out.println("=== REWARD FLOW DEBUG ===");
        Optional<LostFoundItem> itemOpt = getItemById(itemId);

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            System.out.println("Item: " + item.getItemName() + " | Type: " + item.getType());
            System.out.println("Status: " + item.getStatus() + " | Verified: " + item.isVerified());

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                System.out.println("Claimed By: " + lostItem.getClaimedBy());
                System.out.println("Claim Status: " + lostItem.getClaimStatus());
                System.out.println("Can Reward: " + (lostItem.isClaimPending() && lostItem.getClaimedBy() != null));

                if (lostItem.getClaimedBy() != null) {
                    User user = userService.getUserByUsername(lostItem.getClaimedBy()).orElse(null);
                    System.out.println("User Exists: " + (user != null));
                    if (user != null) {
                        System.out.println("Current Points: " + user.getRewardPoints());
                        System.out.println("Current Items Returned: " + user.getItemsReturned());
                    }
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                System.out.println("Claimed By: " + foundItem.getClaimedBy());
                System.out.println("Claim Status: " + foundItem.getClaimStatus());
                System.out.println("Reported By: " + foundItem.getReportedBy());
                System.out.println("Can Reward: " + (foundItem.isClaimPending() && foundItem.getClaimedBy() != null));

                User user = userService.getUserByUsername(foundItem.getReportedBy()).orElse(null);
                System.out.println("User Exists: " + (user != null));
                if (user != null) {
                    System.out.println("Current Points: " + user.getRewardPoints());
                    System.out.println("Current Items Returned: " + user.getItemsReturned());
                }
            }
        } else {
            System.out.println("❌ Item not found: " + itemId);
        }
        System.out.println("=== END DEBUG ===");
    }

    // NEW: Debug reward system for all users
    public void debugRewardSystem() {
        System.out.println("=== REWARD SYSTEM DEBUG ===");

        List<User> allUsers = userService.getAllUsers();
        System.out.println("Total Users: " + allUsers.size());
        for (User user : allUsers) {
            System.out.println("👤 " + user.getUsername() +
                    " | Points: " + user.getRewardPoints() +
                    " | Items Returned: " + user.getItemsReturned() +
                    " | Tier: " + user.getRewardTier());
        }

        long returnedItems = getReturnedItems().size();
        long pendingClaims = getPendingClaimItems().size();
        System.out.println("📦 Total Returned Items in System: " + returnedItems);
        System.out.println("⏳ Pending Claims: " + pendingClaims);
        System.out.println("=== END REWARD DEBUG ===");
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
                    " | Claim Status: " + item.getClaimStatus() +
                    " | Verified: " + item.isVerified());
        }

        System.out.println("📦 Found items: " + foundItems.size());
        for (FoundItem item : foundItems) {
            System.out.println("   - " + item.getItemName() +
                    " | Status: " + item.getStatus() +
                    " | Claimed by: " + item.getClaimedBy() +
                    " | Claim Status: " + item.getClaimStatus() +
                    " | Verified: " + item.isVerified());
        }

        System.out.println("🔍 Available lost items: " + getAvailableLostItems().size());
        System.out.println("🔍 Available found items: " + getAvailableFoundItems().size());
        System.out.println("⏳ Pending verification: " + getPendingVerificationCount());
        System.out.println("✅ Returned items: " + getReturnedItemsCount());
        System.out.println("⏳ Pending claims: " + getPendingClaimItems().size());
        System.out.println("=== END DEBUG ===");
    }

    // Force refresh items from JSON file
    public void refreshItems() {
        loadItems();
        System.out.println("🔄 Items refreshed from JSON file");
        debugCurrentItems();
    }
}
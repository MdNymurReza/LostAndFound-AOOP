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
        jsonDataService.saveItems(items);
    }

    // Add new items
    public boolean addLostItem(LostItem lostItem) {
        items.add(lostItem);
        saveItems();
        System.out.println("✅ Lost item added: " + lostItem.getItemName());
        return true;
    }

    public boolean addFoundItem(FoundItem foundItem) {
        items.add(foundItem);
        saveItems();
        System.out.println("✅ Found item added: " + foundItem.getItemName());
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

    // Get available items for claiming
    public List<LostFoundItem> getAvailableLostItems() {
        return items.stream()
                .filter(item -> item instanceof LostItem)
                .filter(item -> item.isVerified() && !"claimed".equals(item.getStatus()) && !"returned".equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getAvailableFoundItems() {
        return items.stream()
                .filter(item -> item instanceof FoundItem)
                .filter(item -> item.isVerified() && !"claimed".equals(item.getStatus()) && !"returned".equals(item.getStatus()))
                .collect(Collectors.toList());
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
            if (item.isVerified() && !"claimed".equals(item.getStatus()) && !"returned".equals(item.getStatus())) {
                // Set item as claimed
                item.setStatus("claimed");

                // Set claim details based on item type
                if (item instanceof LostItem) {
                    LostItem lostItem = (LostItem) item;
                    lostItem.setClaimedBy(claimant);
                    lostItem.setClaimStatus("pending");
                } else if (item instanceof FoundItem) {
                    FoundItem foundItem = (FoundItem) item;
                    foundItem.setClaimedBy(claimant);
                    foundItem.setClaimStatus("pending");
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
                .filter(item -> "pending".equals(item.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getVerifiedItems() {
        return items.stream()
                .filter(item -> "verified".equals(item.getVerificationStatus()))
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

    // Claim approval methods
    public boolean approveClaim(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();

            if (item instanceof LostItem) {
                LostItem lostItem = (LostItem) item;
                if (lostItem.isClaimPending()) {
                    lostItem.approveClaim();

                    // Reward the user who found the item
                    User finder = userService.getUserByUsername(lostItem.getClaimedBy()).orElse(null);
                    if (finder != null) {
                        finder.addRewardPoints(50);
                        finder.incrementItemsReturned();
                        System.out.println("🎁 Rewarded user: " + finder.getUsername() + " with 50 points");
                    }

                    saveItems();
                    return true;
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                if (foundItem.isClaimPending()) {
                    foundItem.approveClaim();

                    // Reward the user who reported the found item
                    User reporter = userService.getUserByUsername(foundItem.getReportedBy()).orElse(null);
                    if (reporter != null) {
                        reporter.addRewardPoints(50);
                        reporter.incrementItemsReturned();
                        System.out.println("🎁 Rewarded user: " + reporter.getUsername() + " with 50 points");
                    }

                    saveItems();
                    return true;
                }
            }
        }
        return false;
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
                    saveItems();
                    return true;
                }
            } else if (item instanceof FoundItem) {
                FoundItem foundItem = (FoundItem) item;
                if (foundItem.isClaimPending()) {
                    foundItem.rejectClaim();
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
                .filter(item -> "verified".equals(item.getVerificationStatus()))
                .count();
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

    // Debug method
    public void debugItems() {
        System.out.println("=== ITEMS DEBUG ===");
        System.out.println("Total items: " + items.size());

        long lostCount = getLostItems().size();
        long foundCount = getFoundItems().size();
        long pendingVerification = getPendingVerificationCount();
        long verified = getTotalVerifiedCount();

        System.out.println("📊 Item breakdown:");
        System.out.println("   Lost items: " + lostCount);
        System.out.println("   Found items: " + foundCount);
        System.out.println("   Pending verification: " + pendingVerification);
        System.out.println("   Verified items: " + verified);
        System.out.println("   Verification rate: " + getVerificationRate() + "%");

        System.out.println("=== END DEBUG ===");
    }
}
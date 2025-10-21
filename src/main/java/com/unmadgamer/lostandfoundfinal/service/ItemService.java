package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.FoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;
import com.unmadgamer.lostandfoundfinal.model.LostItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemService {
    private static ItemService instance;
    private final JsonDataService jsonDataService;
    private List<LostFoundItem> items;

    private ItemService() {
        this.jsonDataService = new JsonDataService();
        loadItems();
        System.out.println("✅ ItemService initialized with " + items.size() + " items");

        // Debug: Print all loaded items
        debugLoadedItems();
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
            System.out.println("⚠️  Items list was null, created new empty list");
        }
    }

    private void saveItems() {
        jsonDataService.saveItems(items);
    }

    // Debug method to print all loaded items
    private void debugLoadedItems() {
        System.out.println("=== LOADED ITEMS DEBUG ===");
        for (LostFoundItem item : items) {
            System.out.println("📦 " + item.getItemName() +
                    " | Type: " + item.getType() +
                    " | Status: " + item.getStatus() +
                    " | Verification: " + item.getVerificationStatus() +
                    " | Reported by: " + item.getReportedBy() +
                    " | ID: " + item.getId());
        }
        System.out.println("=== END DEBUG ===");
    }

    // Add new items with better error handling
    public boolean addLostItem(LostItem item) {
        try {
            if (item == null) {
                System.err.println("❌ Cannot add null lost item");
                return false;
            }

            item.setId(generateId());

            // Ensure proper initialization
            if (item.getStatus() == null) {
                item.setStatus("pending");
            }
            if (item.getVerificationStatus() == null) {
                item.setVerificationStatus("pending");
            }

            items.add(item);
            saveItems();
            System.out.println("✅ Lost item added: " + item.getItemName() + " (ID: " + item.getId() + ")");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error adding lost item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addFoundItem(FoundItem item) {
        try {
            if (item == null) {
                System.err.println("❌ Cannot add null found item");
                return false;
            }

            item.setId(generateId());

            // Ensure proper initialization
            if (item.getStatus() == null) {
                item.setStatus("pending");
            }
            if (item.getVerificationStatus() == null) {
                item.setVerificationStatus("pending");
            }

            items.add(item);
            saveItems();
            System.out.println("✅ Found item added: " + item.getItemName() + " (ID: " + item.getId() + ")");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error adding found item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Verification methods
    public boolean verifyItem(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            item.verify(adminUsername);
            saveItems();
            System.out.println("✅ Item verified: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        System.err.println("❌ Item not found for verification: " + itemId);
        return false;
    }

    public boolean rejectItem(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            item.reject(adminUsername);
            saveItems();
            System.out.println("❌ Item rejected: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        System.err.println("❌ Item not found for rejection: " + itemId);
        return false;
    }

    // Claim methods
    public boolean claimItem(String itemId, String claimantUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent() && itemOpt.get().isVerified()) {
            LostFoundItem item = itemOpt.get();
            if (item instanceof LostItem) {
                ((LostItem) item).claimItem(claimantUsername);
            } else if (item instanceof FoundItem) {
                ((FoundItem) item).claimItem(claimantUsername);
            }
            saveItems();
            System.out.println("📝 Item claimed: " + item.getItemName() + " by " + claimantUsername);
            return true;
        } else if (itemOpt.isPresent() && !itemOpt.get().isVerified()) {
            System.err.println("❌ Cannot claim unverified item: " + itemId);
        } else {
            System.err.println("❌ Item not found for claim: " + itemId);
        }
        return false;
    }

    public boolean approveClaim(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            if (item instanceof LostItem) {
                ((LostItem) item).approveClaim(adminUsername);
            } else if (item instanceof FoundItem) {
                ((FoundItem) item).approveClaim(adminUsername);
            }
            saveItems();
            System.out.println("✅ Claim approved: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        System.err.println("❌ Item not found for claim approval: " + itemId);
        return false;
    }

    public boolean rejectClaim(String itemId, String adminUsername) {
        Optional<LostFoundItem> itemOpt = getItemById(itemId);
        if (itemOpt.isPresent()) {
            LostFoundItem item = itemOpt.get();
            if (item instanceof LostItem) {
                ((LostItem) item).rejectClaim(adminUsername);
            } else if (item instanceof FoundItem) {
                ((FoundItem) item).rejectClaim(adminUsername);
            }
            saveItems();
            System.out.println("❌ Claim rejected: " + item.getItemName() + " by " + adminUsername);
            return true;
        }
        System.err.println("❌ Item not found for claim rejection: " + itemId);
        return false;
    }

    // Get items for verification
    public List<LostFoundItem> getPendingVerificationItems() {
        return items.stream()
                .filter(item -> item != null && "pending".equals(item.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getVerifiedItems() {
        return items.stream()
                .filter(item -> item != null && "verified".equals(item.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getPendingClaimItems() {
        return items.stream()
                .filter(item -> {
                    if (item == null) return false;
                    if (item instanceof LostItem) {
                        return "pending".equals(((LostItem) item).getClaimStatus());
                    } else if (item instanceof FoundItem) {
                        return "pending".equals(((FoundItem) item).getClaimStatus());
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // Get items for regular users - FIXED LOGIC
    public List<LostFoundItem> getAvailableLostItems() {
        return items.stream()
                .filter(item -> item != null &&
                        item instanceof LostItem &&
                        "verified".equals(item.getVerificationStatus()) &&
                        !"claimed".equals(item.getStatus()) &&
                        !"returned".equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getAvailableFoundItems() {
        return items.stream()
                .filter(item -> item != null &&
                        item instanceof FoundItem &&
                        "verified".equals(item.getVerificationStatus()) &&
                        !"claimed".equals(item.getStatus()) &&
                        !"returned".equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    // Get items by status for returned items controller
    public List<LostFoundItem> getItemsByStatus(String status) {
        return items.stream()
                .filter(item -> item != null && status.equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    // Get items by reporter
    public List<LostFoundItem> getItemsByReporter(String username) {
        return items.stream()
                .filter(item -> item != null && username.equals(item.getReportedBy()))
                .collect(Collectors.toList());
    }

    // Helper methods
    private String generateId() {
        return "ITEM_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public Optional<LostFoundItem> getItemById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return items.stream()
                .filter(item -> item != null && id.equals(item.getId()))
                .findFirst();
    }

    public List<LostFoundItem> getAllItems() {
        return new ArrayList<>(items);
    }

    // Statistics - FIXED METHODS
    public long getPendingVerificationCount() {
        return getPendingVerificationItems().size();
    }

    public long getVerifiedTodayCount() {
        String today = java.time.LocalDate.now().toString();
        return items.stream()
                .filter(item -> item != null && today.equals(item.getVerificationDate()))
                .count();
    }

    public long getTotalVerifiedCount() {
        return getVerifiedItems().size();
    }

    public double getVerificationRate() {
        long total = items.size();
        long verified = getVerifiedItems().size();
        return total > 0 ? (verified * 100.0 / total) : 100.0;
    }

    // Debug method for controllers
    public void debugCurrentData() {
        System.out.println("=== ITEM SERVICE CURRENT DATA ===");
        System.out.println("Total items: " + items.size());

        long lostCount = items.stream().filter(item -> item instanceof LostItem).count();
        long foundCount = items.stream().filter(item -> item instanceof FoundItem).count();
        long pendingVerification = getPendingVerificationCount();
        long verified = getTotalVerifiedCount();

        System.out.println("Lost items: " + lostCount);
        System.out.println("Found items: " + foundCount);
        System.out.println("Pending verification: " + pendingVerification);
        System.out.println("Verified items: " + verified);
        System.out.println("Verification rate: " + getVerificationRate() + "%");

        // Print recent items
        System.out.println("Recent items (last 5):");
        items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(item ->
                        System.out.println("  - " + item.getItemName() +
                                " (" + item.getType() + ") - " +
                                item.getStatus() + " - " +
                                item.getVerificationStatus())
                );
        System.out.println("=== END DEBUG ===");
    }

    // Refresh data (reload from JSON)
    public void refreshData() {
        System.out.println("🔄 Refreshing item data from JSON...");
        loadItems();
        System.out.println("✅ Item data refreshed. Total items: " + items.size());
    }

    // Reset all data (for testing)
    public void resetAllData() {
        System.out.println("🔄 Resetting all item data...");
        items.clear();
        saveItems();
        System.out.println("✅ All item data reset");
    }
}
package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemService {
    private static ItemService instance;
    private final JsonDataService jsonDataService;
    private List<LostFoundItem> items;

    private ItemService() {
        this.jsonDataService = new JsonDataService();
        this.items = new ArrayList<>();
        loadItems();

        System.out.println("🔄 ItemService initialized with " + items.size() + " items");
        debugCurrentData();
    }

    public static synchronized ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }

    private void loadItems() {
        try {
            items = jsonDataService.loadItems();
            System.out.println("📥 Loaded " + items.size() + " items into memory");

        } catch (Exception e) {
            System.err.println("❌ Critical error loading items: " + e.getMessage());
            items = new ArrayList<>(); // Ensure we always have a valid list
        }
    }

    private void saveItems() {
        System.out.println("💾 Attempting to save " + items.size() + " items...");
        boolean success = jsonDataService.saveItems(items);

        if (success) {
            System.out.println("✅ Items saved successfully");
            // Force reload to ensure consistency
            forceReloadItems();
        } else {
            System.err.println("❌ Save failed, attempting emergency recovery...");
            emergencyDataRecovery();
        }
    }

    // Force reload items from file
    private void forceReloadItems() {
        System.out.println("🔄 Force reloading items from file...");
        List<LostFoundItem> fileItems = jsonDataService.loadItems();
        if (fileItems.size() == items.size()) {
            System.out.println("✅ Force reload successful: " + fileItems.size() + " items");
            items = fileItems; // Replace in-memory list with file list
        } else {
            System.err.println("⚠️  Force reload mismatch: memory=" + items.size() + ", file=" + fileItems.size());
            // Use whichever has more items (data recovery strategy)
            if (fileItems.size() > items.size()) {
                items = fileItems;
                System.out.println("🔧 Using file data for recovery");
            }
        }
    }

    public void addItem(LostFoundItem item) {
        if (item == null) {
            System.err.println("❌ Cannot add null item");
            return;
        }

        System.out.println("➕ Adding new item: " + item.getItemName() + " by " + item.getReportedBy());

        // Add to memory
        items.add(item);
        System.out.println("📊 Memory items count: " + items.size());

        // Immediate save with force reload
        saveItems();

        System.out.println("✅ Item addition completed");

        // Debug after addition
        debugCurrentData();
    }

    // Refresh data method for controllers to call
    public void refreshData() {
        System.out.println("🔄 Manual data refresh requested");
        loadItems();
        debugCurrentData();
    }

    // Get fresh data from file (bypass cache)
    public List<LostFoundItem> getFreshItemsByStatus(String status) {
        System.out.println("🔄 Getting fresh items with status: " + status);
        List<LostFoundItem> freshItems = jsonDataService.loadItems();
        return freshItems.stream()
                .filter(item -> item.getStatus() != null && item.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // Get fresh user-specific data
    public List<LostFoundItem> getFreshUserItemsByStatus(String username, String status) {
        System.out.println("🔄 Getting fresh items for user: " + username + " status: " + status);
        List<LostFoundItem> freshItems = jsonDataService.loadItems();
        return freshItems.stream()
                .filter(item -> status.equalsIgnoreCase(item.getStatus()) &&
                        username.equals(item.getReportedBy()))
                .collect(Collectors.toList());
    }

    // Update existing methods to use fresh data when needed
    public List<LostFoundItem> getItemsByStatus(String status) {
        // Use fresh data for important queries
        return getFreshItemsByStatus(status);
    }

    public List<LostFoundItem> getUserItemsByStatus(String username, String status) {
        // Use fresh data for user-specific queries
        return getFreshUserItemsByStatus(username, status);
    }

    // Keep other existing methods the same
    public List<LostFoundItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public List<LostFoundItem> getItemsByUser(String username) {
        return items.stream()
                .filter(item -> item.getReportedBy() != null && item.getReportedBy().equals(username))
                .collect(Collectors.toList());
    }

    public int getLostItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.isLost() && username.equals(item.getReportedBy()))
                .count();
    }

    public int getFoundItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.isFound() && username.equals(item.getReportedBy()))
                .count();
    }

    public int getReturnedItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.isReturned() && username.equals(item.getReportedBy()))
                .count();
    }

    public boolean updateItemStatus(String itemName, String newStatus, String verifiedBy) {
        for (LostFoundItem item : items) {
            if (item.getItemName().equals(itemName)) {
                item.setStatus(newStatus);
                if (verifiedBy != null) {
                    item.setVerified(true);
                    item.setVerifiedBy(verifiedBy);
                }
                saveItems(); // This will trigger force reload
                return true;
            }
        }
        return false;
    }

    // ===== NEW ADMIN VERIFICATION METHODS =====

    public boolean verifyItem(String itemName, String adminUsername) {
        for (LostFoundItem item : items) {
            if (item.getItemName().equals(itemName)) {
                item.verify(adminUsername);
                saveItems();
                System.out.println("✅ Item verified: " + itemName + " by " + adminUsername);
                return true;
            }
        }
        return false;
    }

    public boolean verifyItemById(String uniqueId, String adminUsername) {
        for (LostFoundItem item : items) {
            if (item.getUniqueId().equals(uniqueId)) {
                item.verify(adminUsername);
                saveItems();
                System.out.println("✅ Item verified (by ID): " + item.getItemName() + " by " + adminUsername);
                return true;
            }
        }
        return false;
    }

    public boolean unverifyItem(String itemName) {
        for (LostFoundItem item : items) {
            if (item.getItemName().equals(itemName)) {
                item.unverify();
                saveItems();
                System.out.println("⚠️ Item unverified: " + itemName);
                return true;
            }
        }
        return false;
    }

    public List<LostFoundItem> getItemsPendingVerification() {
        return items.stream()
                .filter(item -> !item.isVerified() && !item.isReturned())
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getVerifiedItems() {
        return items.stream()
                .filter(LostFoundItem::isVerified)
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getUserItemsByVerificationStatus(String username, boolean verified) {
        return items.stream()
                .filter(item -> username.equals(item.getReportedBy()) && item.isVerified() == verified)
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getUnverifiedItemsByStatus(String status) {
        return items.stream()
                .filter(item -> status.equalsIgnoreCase(item.getStatus()) && !item.isVerified())
                .collect(Collectors.toList());
    }

    public int getPendingVerificationCount() {
        return (int) items.stream()
                .filter(item -> !item.isVerified() && !item.isReturned())
                .count();
    }

    public int getVerifiedItemsCount() {
        return (int) items.stream()
                .filter(LostFoundItem::isVerified)
                .count();
    }

    public LostFoundItem getItemByName(String itemName) {
        return items.stream()
                .filter(item -> item.getItemName().equals(itemName))
                .findFirst()
                .orElse(null);
    }

    public LostFoundItem getItemById(String uniqueId) {
        return items.stream()
                .filter(item -> item.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    // Debug and maintenance methods
    public void debugCurrentData() {
        System.out.println("=== DATA DEBUG INFORMATION ===");
        System.out.println("In-memory items count: " + items.size());
        System.out.println("Pending verification: " + getPendingVerificationCount());
        System.out.println("Verified items: " + getVerifiedItemsCount());

        for (LostFoundItem item : items) {
            System.out.println("📦 " + item.getItemName() + " | " + item.getStatus() + " | " +
                    item.getReportedBy() + " | " + item.getDate() + " | Verified: " + item.isVerified() +
                    (item.isVerified() ? " by " + item.getVerifiedBy() : ""));
        }
        System.out.println("=== END DATA DEBUG ===");
    }

    public void emergencyDataRecovery() {
        System.out.println("🚨 EMERGENCY DATA RECOVERY INITIATED");
        jsonDataService.resetAllData();
        items = new ArrayList<>();
        System.out.println("✅ Emergency recovery completed");
    }

    public void resetAllData() {
        jsonDataService.resetAllData();
        items = new ArrayList<>();
        System.out.println("✅ All data reset completed");
    }
}
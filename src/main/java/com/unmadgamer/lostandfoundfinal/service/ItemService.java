package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.LostFoundItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemService {
    private static ItemService instance;
    private final JsonDataService jsonDataService;
    private List<LostFoundItem> items;

    private ItemService() {
        this.jsonDataService = new JsonDataService();
        loadItems();
        jsonDataService.debugFileOperations(); // Debug file operations
    }

    public static ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }

    private void loadItems() {
        items = jsonDataService.loadItems();
        System.out.println("Loaded " + items.size() + " items from storage");

        // Debug: Print all loaded items
        for (LostFoundItem item : items) {
            System.out.println("Item: " + item.getItemName() + " | " + item.getStatus() + " | " + item.getReportedBy());
        }
    }

    private void saveItems() {
        System.out.println("Saving " + items.size() + " items to JSON...");
        boolean success = jsonDataService.saveItems(items);
        if (success) {
            System.out.println("✓ Items saved successfully");
            // Verify by reloading
            List<LostFoundItem> reloadedItems = jsonDataService.loadItems();
            System.out.println("Verification: " + reloadedItems.size() + " items reloaded");
        } else {
            System.out.println("✗ FAILED to save items");
        }
    }

    public void addItem(LostFoundItem item) {
        items.add(item);
        saveItems();
        System.out.println("Item added: " + item.getItemName() + " (" + item.getStatus() + ") by " + item.getReportedBy());
    }

    public List<LostFoundItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public List<LostFoundItem> getItemsByStatus(String status) {
        return items.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> getItemsByUser(String username) {
        return items.stream()
                .filter(item -> item.getReportedBy().equals(username))
                .collect(Collectors.toList());
    }

    public List<LostFoundItem> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllItems();
        }

        String searchTerm = keyword.toLowerCase();
        return items.stream()
                .filter(item ->
                        item.getItemName().toLowerCase().contains(searchTerm) ||
                                item.getCategory().toLowerCase().contains(searchTerm) ||
                                item.getDescription().toLowerCase().contains(searchTerm) ||
                                item.getLocation().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    public int getLostItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase("lost") &&
                        item.getReportedBy().equals(username))
                .count();
    }

    public int getFoundItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.getStatus().equalsIgnoreCase("found") &&
                        item.getReportedBy().equals(username))
                .count();
    }

    public int getReturnedItemsCount(String username) {
        return (int) items.stream()
                .filter(item -> item.isReturned() &&
                        item.getReportedBy().equals(username))
                .count();
    }

    public boolean markAsReturned(String itemId) {
        for (LostFoundItem item : items) {
            if (item.getId().equals(itemId)) {
                item.setReturned(true);
                saveItems();
                return true;
            }
        }
        return false;
    }
}
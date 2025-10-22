package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    private String id;
    private String user1;
    private String user2;
    private String itemId;
    private List<Message> messages;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadCount;
    private boolean isActive;

    public Conversation() {
        this.id = UUID.randomUUID().toString();
        this.messages = new ArrayList<>();
        this.lastMessageTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.unreadCount = 0;
        this.isActive = true;
    }

    public Conversation(String user1, String user2) {
        this();
        this.user1 = user1;
        this.user2 = user2;
    }

    public Conversation(String user1, String user2, String itemId) {
        this(user1, user2);
        this.itemId = itemId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser1() { return user1; }
    public void setUser1(String user1) { this.user1 = user1; }

    public String getUser2() { return user2; }
    public void setUser2(String user2) { this.user2 = user2; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public String getOtherUser(String currentUser) {
        if (currentUser.equals(user1)) {
            return user2;
        } else if (currentUser.equals(user2)) {
            return user1;
        }
        return null;
    }

    public boolean involvesUser(String username) {
        return user1.equals(username) || user2.equals(username);
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastMessage = message.getContent();
        lastMessageTime = message.getTimestamp();

        if (!message.getSenderUsername().equals(user1) && !message.getSenderUsername().equals(user2)) {
        } else if (message.getReceiverUsername().equals(user1) || message.getReceiverUsername().equals(user2)) {
            unreadCount++;
        }
    }

    public void markAsRead() {
        this.unreadCount = 0;
        for (Message message : messages) {
            message.setRead(true);
        }
    }

    public String getFormattedLastMessage() {
        if (lastMessage == null || lastMessage.isEmpty()) {
            return "No messages yet";
        }
        return lastMessage.length() > 30 ? lastMessage.substring(0, 30) + "..." : lastMessage;
    }

    @Override
    public String toString() {
        return String.format("Conversation{id='%s', users='%s & %s', messages=%d, unread=%d}",
                id, user1, user2, messages.size(), unreadCount);
    }
}
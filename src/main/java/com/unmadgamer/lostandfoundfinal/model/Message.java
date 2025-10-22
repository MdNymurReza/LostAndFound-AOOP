package com.unmadgamer.lostandfoundfinal.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message {
    private String id;
    private String conversationId;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private String timestamp;
    private boolean isRead;
    private String itemId;
    private String messageType;

    public Message() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isRead = false;
        this.messageType = "text";
    }

    public Message(String conversationId, String senderUsername, String receiverUsername, String content) {
        this();
        this.conversationId = conversationId;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content = content;
    }

    public Message(String conversationId, String senderUsername, String receiverUsername, String content, String itemId) {
        this(conversationId, senderUsername, receiverUsername, content);
        this.itemId = itemId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    // Helper methods
    public String getFormattedTime() {
        try {
            LocalDateTime time = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return time.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } catch (Exception e) {
            return timestamp;
        }
    }

    public boolean isSystemMessage() {
        return "system".equals(messageType);
    }

    @Override
    public String toString() {
        return String.format("Message{id='%s', from='%s', to='%s', content='%s', read=%s}",
                id, senderUsername, receiverUsername, content, isRead);
    }
}
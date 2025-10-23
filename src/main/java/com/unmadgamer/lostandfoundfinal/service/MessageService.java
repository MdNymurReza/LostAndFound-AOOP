package com.unmadgamer.lostandfoundfinal.service;

import com.unmadgamer.lostandfoundfinal.model.Conversation;
import com.unmadgamer.lostandfoundfinal.model.Message;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class MessageService {
    private static MessageService instance;
    private final JsonDataService jsonDataService;
    private final UserService userService;
    private List<Conversation> conversations;
    private ObservableList<Conversation> observableConversations;
    private static final String CONVERSATIONS_FILE = "data/conversations.json";
    private List<MessageListener> messageListeners = new ArrayList<>();

    // Interface for real-time message updates
    public interface MessageListener {
        void onNewMessage(Message message);
        void onConversationUpdated(Conversation conversation);
    }

    private MessageService() {
        this.jsonDataService = new JsonDataService();
        this.userService = UserService.getInstance();
        this.observableConversations = FXCollections.observableArrayList();
        loadConversations();
        System.out.println("✅ MessageService initialized with " + conversations.size() + " conversations");
    }

    public static synchronized MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    // Add listener for real-time updates
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    // Remove listener
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    // Notify listeners about new message
    private void notifyNewMessage(Message message) {
        for (MessageListener listener : messageListeners) {
            listener.onNewMessage(message);
        }
    }

    // Notify listeners about conversation update
    private void notifyConversationUpdated(Conversation conversation) {
        for (MessageListener listener : messageListeners) {
            listener.onConversationUpdated(conversation);
        }
    }

    private void loadConversations() {
        try {
            Path filePath = Paths.get(CONVERSATIONS_FILE);
            if (!Files.exists(filePath)) {
                conversations = new ArrayList<>();
                saveConversations();
                return;
            }

            String content = Files.readString(filePath);
            if (content.trim().isEmpty()) {
                conversations = new ArrayList<>();
                return;
            }

            conversations = jsonDataService.getObjectMapper().readValue(
                    filePath.toFile(),
                    new TypeReference<List<Conversation>>() {}
            );

            if (conversations == null) {
                conversations = new ArrayList<>();
            }

            // Update observable list
            observableConversations.setAll(conversations);

            System.out.println("✅ Loaded " + conversations.size() + " conversations");

        } catch (Exception e) {
            System.err.println("❌ Error loading conversations: " + e.getMessage());
            conversations = new ArrayList<>();
        }
    }

    private void saveConversations() {
        try {
            Path filePath = Paths.get(CONVERSATIONS_FILE);
            Path parentDir = filePath.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Path tempFile = Paths.get(CONVERSATIONS_FILE + ".tmp");
            jsonDataService.getObjectMapper().writeValue(tempFile.toFile(), conversations);
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update observable list after save
            observableConversations.setAll(conversations);

            System.out.println("✅ Saved " + conversations.size() + " conversations");

        } catch (Exception e) {
            System.err.println("❌ Error saving conversations: " + e.getMessage());
        }
    }

    public Conversation getOrCreateConversation(String user1, String user2, String itemId) {
        Optional<Conversation> existing = conversations.stream()
                .filter(conv -> conv.involvesUser(user1) && conv.involvesUser(user2))
                .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation newConversation = new Conversation(user1, user2, itemId);
        conversations.add(newConversation);
        saveConversations();

        System.out.println("✅ Created new conversation between " + user1 + " and " + user2);
        return newConversation;
    }

    public List<Conversation> getUserConversations(String username) {
        return conversations.stream()
                .filter(conv -> conv.involvesUser(username))
                .sorted((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()))
                .collect(Collectors.toList());
    }

    // Get observable conversations for real-time updates
    public ObservableList<Conversation> getObservableUserConversations(String username) {
        return observableConversations.filtered(conv -> conv.involvesUser(username))
                .sorted((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()));
    }

    public Optional<Conversation> getConversationById(String conversationId) {
        return conversations.stream()
                .filter(conv -> conv.getId().equals(conversationId))
                .findFirst();
    }

    public boolean sendMessage(String conversationId, String senderUsername, String content) {
        Optional<Conversation> conversationOpt = getConversationById(conversationId);
        if (conversationOpt.isEmpty()) {
            return false;
        }

        Conversation conversation = conversationOpt.get();
        String receiverUsername = conversation.getOtherUser(senderUsername);

        Message message = new Message(conversationId, senderUsername, receiverUsername, content);
        conversation.addMessage(message);
        saveConversations();

        // Notify listeners about new message
        notifyNewMessage(message);
        notifyConversationUpdated(conversation);

        System.out.println("✅ Message sent in conversation " + conversationId + ": " + content);
        return true;
    }

    public boolean sendSystemMessage(String conversationId, String content) {
        Optional<Conversation> conversationOpt = getConversationById(conversationId);
        if (conversationOpt.isEmpty()) {
            return false;
        }

        Conversation conversation = conversationOpt.get();
        Message message = new Message(conversationId, "System", "", content);
        message.setMessageType("system");
        conversation.addMessage(message);
        saveConversations();

        // Notify listeners
        notifyNewMessage(message);
        notifyConversationUpdated(conversation);

        System.out.println("✅ System message sent: " + content);
        return true;
    }

    public void markConversationAsRead(String conversationId, String username) {
        getConversationById(conversationId).ifPresent(conversation -> {
            conversation.markAsRead();
            saveConversations();
            notifyConversationUpdated(conversation);
            System.out.println("✅ Marked conversation as read: " + conversationId);
        });
    }

    public int getUnreadMessageCount(String username) {
        return getUserConversations(username).stream()
                .mapToInt(Conversation::getUnreadCount)
                .sum();
    }

    public Conversation startItemConversation(String claimant, String itemOwner, String itemId, String itemName) {
        Conversation conversation = getOrCreateConversation(claimant, itemOwner, itemId);

        String systemMessage = String.format(
                "Conversation started about item: %s. %s is interested in this item.",
                itemName, claimant
        );
        sendSystemMessage(conversation.getId(), systemMessage);

        return conversation;
    }

    public void sendAdminMessageToUser(String adminUsername, String targetUsername, String content) {
        Conversation conversation = getOrCreateConversation(adminUsername, targetUsername, null);
        sendMessage(conversation.getId(), adminUsername, content);
    }

    // Force refresh from file (useful for multi-instance scenarios)
    public void refreshFromFile() {
        loadConversations();
    }

    public void debugConversations() {
        System.out.println("=== MESSAGING SYSTEM DEBUG ===");
        System.out.println("Total conversations: " + conversations.size());
        for (Conversation conv : conversations) {
            System.out.println("💬 " + conv.getUser1() + " ↔ " + conv.getUser2() +
                    " | Messages: " + conv.getMessages().size() +
                    " | Unread: " + conv.getUnreadCount());
        }
        System.out.println("=== END DEBUG ===");
    }
}
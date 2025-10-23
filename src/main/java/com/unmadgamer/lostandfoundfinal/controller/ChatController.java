package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.Conversation;
import com.unmadgamer.lostandfoundfinal.model.Message;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.MessageService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController implements MessageService.MessageListener {

    @FXML private ListView<Conversation> conversationsList;
    @FXML private Label chatWithLabel;
    @FXML private Label itemContextLabel;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox noConversationView;
    @FXML private Label noConversationLabel;
    @FXML private Label unreadCountLabel;
    @FXML private Circle onlineStatus;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private Button newChatButton;

    private MessageService messageService;
    private UserService userService;
    private User currentUser;
    private Conversation currentConversation;
    private ObservableList<Conversation> userConversations;
    private String initialConversationId;
    private Timer refreshTimer;
    private boolean isAutoRefreshing = true;

    @FXML
    public void initialize() {
        messageService = MessageService.getInstance();
        userService = UserService.getInstance();
        currentUser = userService.getCurrentUser();

        // Register as message listener for real-time updates
        messageService.addMessageListener(this);

        setupUI();
        loadConversations();
        setupEventHandlers();
        startAutoRefresh();

        System.out.println("✅ ChatController initialized for user: " + currentUser.getUsername());
        updateUnreadCount();

        if (initialConversationId != null) {
            selectInitialConversation();
        }
    }

    public void setInitialConversationId(String conversationId) {
        this.initialConversationId = conversationId;
    }

    private void setupUI() {
        // Configure conversations list with real-time updates
        conversationsList.setCellFactory(param -> new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String otherUser = conversation.getOtherUser(currentUser.getUsername());

                    HBox hbox = new HBox();
                    hbox.setSpacing(8);
                    hbox.setStyle("-fx-padding: 8; -fx-alignment: center-left;");

                    // User avatar with color based on username
                    Circle avatar = new Circle(20);
                    avatar.setFill(generateColorFromString(otherUser));

                    // Text content
                    VBox textBox = new VBox(2);
                    Label userLabel = new Label(otherUser);
                    userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

                    Label messageLabel = new Label(conversation.getFormattedLastMessage());
                    messageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
                    messageLabel.setWrapText(true);
                    messageLabel.setMaxWidth(150);

                    textBox.getChildren().addAll(userLabel, messageLabel);

                    // Unread badge
                    if (conversation.getUnreadCount() > 0) {
                        Label unreadBadge = new Label(String.valueOf(conversation.getUnreadCount()));
                        unreadBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; " +
                                "-fx-padding: 2 6; -fx-background-radius: 10; -fx-min-width: 20; -fx-alignment: center;");

                        HBox container = new HBox();
                        container.setSpacing(5);
                        container.getChildren().addAll(avatar, textBox, unreadBadge);
                        setGraphic(container);
                    } else {
                        HBox container = new HBox();
                        container.setSpacing(5);
                        container.getChildren().addAll(avatar, textBox);
                        setGraphic(container);
                    }
                }
            }
        });

        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        });

        // Initially show no conversation view
        showNoConversationView();
    }

    private Color generateColorFromString(String text) {
        int hash = text.hashCode();
        float hue = (hash & 0xFFFFFF) % 360;
        return Color.hsb(hue, 0.7, 0.9);
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isAutoRefreshing) {
                    Platform.runLater(() -> {
                        refreshConversationsData();
                        if (currentConversation != null) {
                            refreshCurrentConversation();
                        }
                    });
                }
            }
        }, 0, 2000); // Refresh every 2 seconds
    }

    private void refreshConversationsData() {
        // Force refresh from file to get updates from other instances
        messageService.refreshFromFile();

        List<Conversation> updatedConversations = messageService.getUserConversations(currentUser.getUsername());
        if (userConversations != null) {
            // Preserve selection
            Conversation selected = conversationsList.getSelectionModel().getSelectedItem();
            userConversations.setAll(updatedConversations);

            // Restore selection if it still exists
            if (selected != null) {
                Optional<Conversation> stillExists = updatedConversations.stream()
                        .filter(conv -> conv.getId().equals(selected.getId()))
                        .findFirst();
                if (stillExists.isPresent()) {
                    conversationsList.getSelectionModel().select(stillExists.get());
                }
            }
        }
        updateUnreadCount();
    }

    private void refreshCurrentConversation() {
        if (currentConversation != null) {
            // Reload the current conversation to get new messages
            Optional<Conversation> updatedConversation = messageService.getConversationById(currentConversation.getId());
            if (updatedConversation.isPresent()) {
                Conversation newConversation = updatedConversation.get();
                if (!newConversation.getMessages().equals(currentConversation.getMessages())) {
                    currentConversation = newConversation;
                    loadMessages(currentConversation);
                }
            }
        }
    }

    private void loadConversations() {
        // Use observable list for real-time updates
        userConversations = messageService.getObservableUserConversations(currentUser.getUsername());
        conversationsList.setItems(userConversations);

        // Listen for changes in the observable list
        userConversations.addListener((ListChangeListener.Change<? extends Conversation> change) -> {
            updateUnreadCount();
        });

        if (!userConversations.isEmpty()) {
            conversationsList.getSelectionModel().selectFirst();
            displayConversation(userConversations.get(0));
        } else {
            showNoConversationView();
        }
    }

    private void selectInitialConversation() {
        if (initialConversationId != null && userConversations != null) {
            Platform.runLater(() -> {
                Optional<Conversation> targetConversation = userConversations.stream()
                        .filter(conv -> conv.getId().equals(initialConversationId))
                        .findFirst();

                if (targetConversation.isPresent()) {
                    conversationsList.getSelectionModel().select(targetConversation.get());
                    displayConversation(targetConversation.get());
                }
            });
        }
    }

    private void setupEventHandlers() {
        conversationsList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayConversation(newValue);
                    }
                }
        );

        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
                // Allow new line with Shift+Enter
                return;
            } else if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                sendMessage();
            }
        });

        sendButton.setOnAction(event -> sendMessage());

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterConversations(newValue);
        });
    }

    private void filterConversations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            conversationsList.setItems(userConversations);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Conversation> filtered = userConversations.filtered(conversation -> {
            String otherUser = conversation.getOtherUser(currentUser.getUsername());
            return otherUser.toLowerCase().contains(lowerCaseFilter) ||
                    conversation.getFormattedLastMessage().toLowerCase().contains(lowerCaseFilter);
        });
        conversationsList.setItems(filtered);
    }

    private void displayConversation(Conversation conversation) {
        currentConversation = conversation;
        String otherUser = conversation.getOtherUser(currentUser.getUsername());

        Platform.runLater(() -> {
            chatWithLabel.setText("Chat with: " + otherUser);

            if (conversation.getItemId() != null && !conversation.getItemId().isEmpty()) {
                itemContextLabel.setText("Regarding item #" + conversation.getItemId());
                itemContextLabel.setVisible(true);
            } else {
                itemContextLabel.setVisible(false);
            }

            // Mark as read and update UI
            messageService.markConversationAsRead(conversation.getId(), currentUser.getUsername());
            updateUnreadCount();

            loadMessages(conversation);

            // Enable input
            messageInput.setDisable(false);
            sendButton.setDisable(false);
            noConversationView.setVisible(false);
            noConversationView.setManaged(false);
            messagesContainer.setVisible(true);

            // Update online status
            updateOnlineStatus(otherUser);
        });
    }

    private void updateOnlineStatus(String username) {
        // Simulate online status
        boolean isOnline = Math.random() > 0.3; // 70% chance online for demo
        Platform.runLater(() -> {
            if (isOnline) {
                onlineStatus.setFill(Color.LIMEGREEN);
                statusLabel.setText("Online");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");
            } else {
                onlineStatus.setFill(Color.LIGHTGRAY);
                statusLabel.setText("Offline");
                statusLabel.setStyle("-fx-text-fill: #95a5a6;");
            }
        });
    }

    private void loadMessages(Conversation conversation) {
        Platform.runLater(() -> {
            messagesContainer.getChildren().clear();

            for (Message message : conversation.getMessages()) {
                addMessageToDisplay(message);
            }

            // Auto-scroll to bottom
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> messagesScrollPane.setVvalue(1.0));
            pause.play();
        });
    }

    private void addMessageToDisplay(Message message) {
        Platform.runLater(() -> {
            HBox messageBox = new HBox();
            messageBox.setSpacing(10);
            messageBox.setStyle("-fx-padding: 5 15;");

            TextFlow textFlow = new TextFlow();
            Text contentText = new Text(message.getContent());
            Text timeText = new Text(" • " + message.getFormattedTime());

            timeText.setStyle("-fx-fill: #666; -fx-font-size: 10;");

            textFlow.getChildren().addAll(contentText, timeText);
            textFlow.setMaxWidth(400);

            if (message.getSenderUsername().equals(currentUser.getUsername())) {
                // Outgoing message (current user)
                messageBox.setStyle("-fx-padding: 5 15; -fx-alignment: center-right;");
                textFlow.setStyle("-fx-background-color: #007bff; -fx-background-radius: 15; -fx-padding: 10 15;");
                contentText.setStyle("-fx-fill: white;");
                messageBox.getChildren().add(textFlow);
            } else if (message.isSystemMessage()) {
                // System message
                messageBox.setStyle("-fx-padding: 5 15; -fx-alignment: center;");
                textFlow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 8 12;");
                contentText.setStyle("-fx-fill: #666; -fx-font-style: italic;");
                messageBox.getChildren().add(textFlow);
            } else {
                // Incoming message (other user)
                messageBox.setStyle("-fx-padding: 5 15; -fx-alignment: center-left;");
                textFlow.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 15; -fx-padding: 10 15;");
                contentText.setStyle("-fx-fill: #333;");

                // Add sender name for incoming messages
                Label senderLabel = new Label(message.getSenderUsername() + ":");
                senderLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-padding: 0 0 2 5;");

                VBox vbox = new VBox(senderLabel, textFlow);
                vbox.setSpacing(2);
                messageBox.getChildren().add(vbox);
            }

            messagesContainer.getChildren().add(messageBox);
        });
    }

    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null) {
            return;
        }

        if (messageService.sendMessage(currentConversation.getId(), currentUser.getUsername(), content)) {
            messageInput.clear();
            // Message will appear automatically via the listener
        } else {
            showAlert("Error", "Failed to send message", Alert.AlertType.ERROR);
        }
    }

    // Implement MessageListener interface for real-time updates
    @Override
    public void onNewMessage(Message message) {
        System.out.println("📨 Real-time message received: " + message.getContent());

        // If this message belongs to the current conversation, display it
        if (currentConversation != null && currentConversation.getId().equals(message.getConversationId())) {
            addMessageToDisplay(message);

            // Auto-scroll to bottom
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> messagesScrollPane.setVvalue(1.0));
            pause.play();
        }

        // Refresh conversations list to update last message and unread count
        Platform.runLater(this::refreshConversationsData);
    }

    @Override
    public void onConversationUpdated(Conversation conversation) {
        System.out.println("🔄 Conversation updated: " + conversation.getId());

        // Refresh if this is the current conversation
        if (currentConversation != null && currentConversation.getId().equals(conversation.getId())) {
            Platform.runLater(() -> {
                currentConversation = conversation;
                loadMessages(conversation);
            });
        }

        // Update conversations list
        Platform.runLater(this::refreshConversationsData);
    }

    @FXML
    private void startNewConversation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText("Start a new conversation");
        dialog.setContentText("Enter username:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String targetUsername = result.get().trim();

            if (userService.getUserByUsername(targetUsername).isEmpty()) {
                showAlert("Error", "User not found: " + targetUsername, Alert.AlertType.ERROR);
                return;
            }

            if (targetUsername.equals(currentUser.getUsername())) {
                showAlert("Error", "You cannot start a conversation with yourself", Alert.AlertType.ERROR);
                return;
            }

            Conversation newConversation = messageService.getOrCreateConversation(
                    currentUser.getUsername(), targetUsername, null
            );

            // The new conversation will appear automatically via the observable list
            conversationsList.getSelectionModel().select(newConversation);
            displayConversation(newConversation);
        }
    }

    @FXML
    private void refreshConversations() {
        refreshConversationsData();
        showAlert("Refreshed", "Conversations list updated", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToDashboard() {
        // Stop the refresh timer
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        // Remove message listener
        messageService.removeMessageListener(this);

        try {
            Stage currentStage = (Stage) conversationsList.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 750, 600));
            dashboardStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Cannot open dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateUnreadCount() {
        Platform.runLater(() -> {
            int unreadCount = messageService.getUnreadMessageCount(currentUser.getUsername());
            if (unreadCount > 0) {
                unreadCountLabel.setText("Unread: " + unreadCount);
                unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                unreadCountLabel.setText("No unread messages");
                unreadCountLabel.setStyle("-fx-text-fill: #27ae60;");
            }
        });
    }

    private void showNoConversationView() {
        Platform.runLater(() -> {
            noConversationView.setVisible(true);
            noConversationView.setManaged(true);
            messagesContainer.setVisible(false);
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            chatWithLabel.setText("Chat with: --");
            itemContextLabel.setVisible(false);
            onlineStatus.setFill(Color.LIGHTGRAY);
            statusLabel.setText("Offline");
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Clean up when controller is destroyed
    public void shutdown() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
        messageService.removeMessageListener(this);
    }
}
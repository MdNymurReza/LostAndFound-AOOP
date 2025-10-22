package com.unmadgamer.lostandfoundfinal.controller;

import com.unmadgamer.lostandfoundfinal.model.Conversation;
import com.unmadgamer.lostandfoundfinal.model.Message;
import com.unmadgamer.lostandfoundfinal.model.User;
import com.unmadgamer.lostandfoundfinal.service.MessageService;
import com.unmadgamer.lostandfoundfinal.service.UserService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ChatController {

    @FXML private ListView<Conversation> conversationsList;
    @FXML private Label chatWithLabel;
    @FXML private Label itemContextLabel;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private Label noConversationLabel;
    @FXML private Label unreadCountLabel;

    private MessageService messageService;
    private UserService userService;
    private User currentUser;
    private Conversation currentConversation;
    private ObservableList<Conversation> userConversations;
    private String initialConversationId;

    @FXML
    public void initialize() {
        messageService = MessageService.getInstance();
        userService = UserService.getInstance();
        currentUser = userService.getCurrentUser();

        setupUI();
        loadConversations();
        setupEventHandlers();

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
        conversationsList.setCellFactory(param -> new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String otherUser = conversation.getOtherUser(currentUser.getUsername());
                    String displayText = otherUser + "\n" + conversation.getFormattedLastMessage();

                    HBox hbox = new HBox();
                    Label userLabel = new Label(otherUser);
                    userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

                    Label messageLabel = new Label(conversation.getFormattedLastMessage());
                    messageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

                    VBox vbox = new VBox(userLabel, messageLabel);
                    vbox.setSpacing(2);

                    if (conversation.getUnreadCount() > 0) {
                        Label unreadBadge = new Label(String.valueOf(conversation.getUnreadCount()));
                        unreadBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; " +
                                "-fx-padding: 2 5; -fx-background-radius: 10;");

                        hbox.getChildren().addAll(vbox, unreadBadge);
                        hbox.setSpacing(10);
                    } else {
                        hbox.getChildren().add(vbox);
                    }

                    setGraphic(hbox);
                }
            }
        });

        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }

    private void loadConversations() {
        List<Conversation> conversations = messageService.getUserConversations(currentUser.getUsername());
        userConversations = FXCollections.observableArrayList(conversations);
        conversationsList.setItems(userConversations);

        if (!conversations.isEmpty()) {
            conversationsList.getSelectionModel().selectFirst();
            displayConversation(conversations.get(0));
        } else {
            showNoConversationView();
        }
    }

    private void selectInitialConversation() {
        Optional<Conversation> targetConversation = userConversations.stream()
                .filter(conv -> conv.getId().equals(initialConversationId))
                .findFirst();

        if (targetConversation.isPresent()) {
            conversationsList.getSelectionModel().select(targetConversation.get());
            displayConversation(targetConversation.get());
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
                return;
            } else if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                sendMessage();
            }
        });

        sendButton.setOnAction(event -> sendMessage());
    }

    private void displayConversation(Conversation conversation) {
        currentConversation = conversation;
        String otherUser = conversation.getOtherUser(currentUser.getUsername());

        chatWithLabel.setText("Chat with: " + otherUser);

        if (conversation.getItemId() != null && !conversation.getItemId().isEmpty()) {
            itemContextLabel.setText("Regarding item #" + conversation.getItemId());
            itemContextLabel.setVisible(true);
        } else {
            itemContextLabel.setVisible(false);
        }

        messageService.markConversationAsRead(conversation.getId(), currentUser.getUsername());
        updateUnreadCount();

        loadMessages(conversation);

        messageInput.setDisable(false);
        sendButton.setDisable(false);
        noConversationLabel.setVisible(false);
        messagesContainer.setVisible(true);
    }

    private void loadMessages(Conversation conversation) {
        messagesContainer.getChildren().clear();

        for (Message message : conversation.getMessages()) {
            addMessageToDisplay(message);
        }

        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(event -> messagesScrollPane.setVvalue(1.0));
        pause.play();
    }

    private void addMessageToDisplay(Message message) {
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        messageBox.setStyle("-fx-padding: 5 10;");

        TextFlow textFlow = new TextFlow();
        Text contentText = new Text(message.getContent());
        Text timeText = new Text(" • " + message.getFormattedTime());

        timeText.setStyle("-fx-fill: #666; -fx-font-size: 10;");

        textFlow.getChildren().addAll(contentText, timeText);
        textFlow.setMaxWidth(300);

        if (message.getSenderUsername().equals(currentUser.getUsername())) {
            messageBox.setStyle("-fx-padding: 5 10; -fx-alignment: center-right;");
            textFlow.setStyle("-fx-background-color: #007bff; -fx-background-radius: 15; -fx-padding: 8 12;");
            contentText.setStyle("-fx-fill: white;");
            messageBox.getChildren().add(textFlow);
        } else if (message.isSystemMessage()) {
            messageBox.setStyle("-fx-padding: 5 10; -fx-alignment: center;");
            textFlow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 5 10;");
            contentText.setStyle("-fx-fill: #666; -fx-font-style: italic;");
            messageBox.getChildren().add(textFlow);
        } else {
            messageBox.setStyle("-fx-padding: 5 10; -fx-alignment: center-left;");
            textFlow.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 15; -fx-padding: 8 12;");
            contentText.setStyle("-fx-fill: #333;");

            Label senderLabel = new Label(message.getSenderUsername() + ":");
            senderLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

            VBox vbox = new VBox(senderLabel, textFlow);
            vbox.setSpacing(2);
            messageBox.getChildren().add(vbox);
        }

        messagesContainer.getChildren().add(messageBox);
    }

    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null) {
            return;
        }

        if (messageService.sendMessage(currentConversation.getId(), currentUser.getUsername(), content)) {
            messageInput.clear();
            loadMessages(currentConversation);
            refreshConversationsList();
        } else {
            showAlert("Error", "Failed to send message", Alert.AlertType.ERROR);
        }
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

            userConversations.add(0, newConversation);
            conversationsList.getSelectionModel().select(newConversation);
            displayConversation(newConversation);
        }
    }

    @FXML
    private void refreshConversations() {
        loadConversations();
        showAlert("Refreshed", "Conversations list updated", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) conversationsList.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/unmadgamer/lostandfoundfinal/dashboard.fxml"));
            Parent root = loader.load();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard - Lost and Found System");
            dashboardStage.setScene(new Scene(root, 800, 600));
            dashboardStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Cannot open dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshConversationsList() {
        List<Conversation> updatedConversations = messageService.getUserConversations(currentUser.getUsername());
        userConversations.setAll(updatedConversations);
        updateUnreadCount();
    }

    private void updateUnreadCount() {
        int unreadCount = messageService.getUnreadMessageCount(currentUser.getUsername());
        if (unreadCount > 0) {
            unreadCountLabel.setText("Unread: " + unreadCount);
            unreadCountLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            unreadCountLabel.setText("No unread messages");
            unreadCountLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }

    private void showNoConversationView() {
        noConversationLabel.setVisible(true);
        messagesContainer.setVisible(false);
        messageInput.setDisable(true);
        sendButton.setDisable(true);
        chatWithLabel.setText("Chat with: --");
        itemContextLabel.setVisible(false);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
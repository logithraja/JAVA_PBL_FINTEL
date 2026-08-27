package org.example.views;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.example.controllers.FintelAIController;

import java.util.ArrayList;
import java.util.List;

public class FintelAIView {

    private String email;
    private TextField chatInput;
    private Button sendBtn;
    private ToggleButton voiceBtn;
    private Label aiStatusLabel;
    private TextArea aiAdviceArea;
    private Button backBtn;
    private VBox messagesBox;
    private ScrollPane messagesScroll;
    private StackPane typingIndicator;
    private final List<String> messageHistory = new ArrayList<>();

    public FintelAIView(String email) {
        this.email = email;
    }

    public void show() {
        VBox root = new VBox(0);
        root.getStyleClass().addAll("main-background", "root");

        HBox headerBox = createHeader();
        VBox chatArea = createChatArea();
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        HBox inputBox = createInputBox();
        inputBox.setPadding(new Insets(12, 20, 16, 20));

        root.getChildren().addAll(headerBox, chatArea, inputBox);

        addMessage("Hi! I'm Fintel AI. Ask me anything about your finances — budgets, spending patterns, savings goals, or investment advice.", false);

        Scene scene = new Scene(root, 860, 640);
        org.example.utils.ThemeManager.apply(scene);

        javafx.stage.Stage aiStage = new javafx.stage.Stage();
        aiStage.setTitle("Fintel AI");
        org.example.utils.ViewNavigator.applyAppIcon(aiStage);
        aiStage.setScene(scene);
        aiStage.show();

        new FintelAIController(this);
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(16, 20, 12, 20));
        headerBox.getStyleClass().add("sidebar");

        backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");

        Label aiAvatar = new Label("✨");
        aiAvatar.getStyleClass().addAll("chat-avatar", "chat-avatar-ai");

        Label titleLabel = new Label("Fintel AI");
        titleLabel.getStyleClass().add("h2");
        titleLabel.setStyle("-fx-font-weight: 800;");

        Label statusDot = new Label("Online");
        statusDot.getStyleClass().add("chat-typing-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, aiAvatar, titleLabel, statusDot, spacer);
        return headerBox;
    }

    private VBox createChatArea() {
        messagesBox = new VBox(8);
        messagesBox.setPadding(new Insets(16, 20, 8, 20));
        messagesBox.getStyleClass().add("chat-messages-container");

        typingIndicator = new StackPane();
        typingIndicator.setAlignment(Pos.CENTER_LEFT);
        typingIndicator.setPadding(new Insets(4, 0, 4, 0));
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);

        HBox typingBubble = new HBox(6);
        typingBubble.setAlignment(Pos.CENTER_LEFT);
        typingBubble.getStyleClass().addAll("chat-bubble", "chat-bubble-ai");
        typingBubble.setMaxWidth(300);

        Label dotLabel = new Label("●");
        dotLabel.getStyleClass().add("chat-typing-dot");

        Label dotLabel2 = new Label("●");
        dotLabel2.getStyleClass().add("chat-typing-dot");

        Label dotLabel3 = new Label("●");
        dotLabel3.getStyleClass().add("chat-typing-dot");

        animateTypingDot(dotLabel, 0);
        animateTypingDot(dotLabel2, 300);
        animateTypingDot(dotLabel3, 600);

        typingBubble.getChildren().addAll(dotLabel, dotLabel2, dotLabel3);
        typingIndicator.getChildren().add(typingBubble);

        aiAdviceArea = new TextArea();

        messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        messagesScroll.setFitToHeight(true);
        messagesScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        messagesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        StackPane scrollContent = new StackPane(messagesBox, typingIndicator);
        StackPane.setAlignment(messagesBox, Pos.TOP_LEFT);
        StackPane.setAlignment(typingIndicator, Pos.BOTTOM_LEFT);

        messagesScroll.setContent(scrollContent);
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        aiStatusLabel = new Label("● Ready to assist with your financial intelligence.");
        aiStatusLabel.getStyleClass().add("chat-status-bar");

        Label chatHeader = new Label("💬 Chat");
        chatHeader.getStyleClass().add("h4");
        chatHeader.setStyle("-fx-font-weight: 700; -fx-padding: 0 0 8 0;");

        VBox container = new VBox(0);
        container.getChildren().addAll(chatHeader, messagesScroll, aiStatusLabel);
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);
        return container;
    }

    private void animateTypingDot(Label dot, int delayMs) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(dot.opacityProperty(), 0.3)),
            new KeyFrame(Duration.millis(400), new KeyValue(dot.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(800), new KeyValue(dot.opacityProperty(), 0.3))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setDelay(Duration.millis(delayMs));
        timeline.play();
    }

    public void addMessage(String text, boolean isUser) {
        if (text == null || text.isEmpty()) return;

        HBox row = new HBox(10);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));

        Label avatar = new Label(isUser ? "👤" : "✨");
        avatar.getStyleClass().addAll("chat-avatar", isUser ? "chat-avatar-user" : "chat-avatar-ai");

        VBox bubbleColumn = new VBox(2);
        bubbleColumn.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label senderLabel = new Label(isUser ? "You" : "Fintel AI");
        senderLabel.getStyleClass().add("chat-sender-label");

        TextFlow textFlow = new TextFlow();
        Text textNode = new Text(text);
        textFlow.getChildren().add(textNode);

        VBox bubble = new VBox(4, senderLabel, textFlow);
        bubble.getStyleClass().addAll("chat-bubble", isUser ? "chat-bubble-user" : "chat-bubble-ai");
        bubble.setMaxWidth(520);

        if (isUser) {
            row.getChildren().addAll(bubble, avatar);
        } else {
            row.getChildren().addAll(avatar, bubble);
        }

        HBox.setHgrow(row, Priority.ALWAYS);

        messagesBox.getChildren().add(row);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), row);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        messageHistory.add((isUser ? "You" : "AI") + ": " + text);

        javafx.application.Platform.runLater(() -> {
            messagesScroll.setVvalue(1.0);
        });
    }

    private HBox createInputBox() {
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        chatInput = new TextField();
        chatInput.setPromptText("Ask about your budget, analyze spending, or get financial advice...");
        chatInput.getStyleClass().add("chat-input");
        chatInput.setStyle("-fx-font-size: 14px; -fx-pref-height: 44px;");
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        sendBtn = new Button("→");
        sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setStyle("-fx-pref-height: 44px; -fx-pref-width: 50px; -fx-font-size: 16px; -fx-font-weight: bold;");

        voiceBtn = new ToggleButton("🎤");
        voiceBtn.getStyleClass().add("chat-voice-btn");
        voiceBtn.setStyle("-fx-pref-height: 44px; -fx-pref-width: 50px; -fx-font-size: 16px;");

        inputBox.getChildren().addAll(chatInput, sendBtn, voiceBtn);
        return inputBox;
    }

    public void showTypingIndicator() {
        typingIndicator.setVisible(true);
        typingIndicator.setManaged(true);
        javafx.application.Platform.runLater(() -> messagesScroll.setVvalue(1.0));
    }

    public void hideTypingIndicator() {
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);
    }

    public String getEmail() { return email; }
    public TextField getChatInput() { return chatInput; }
    public Button getSendBtn() { return sendBtn; }
    public ToggleButton getVoiceBtn() { return voiceBtn; }
    public Label getAiStatusLabel() { return aiStatusLabel; }
    public TextArea getAiAdviceArea() { return aiAdviceArea; }
    public Button getBackBtn() { return backBtn; }
    public VBox getMessagesBox() { return messagesBox; }
}

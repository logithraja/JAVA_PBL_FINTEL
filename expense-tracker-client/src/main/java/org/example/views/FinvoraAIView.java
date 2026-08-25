package org.example.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controllers.FinvoraAIController;
import org.example.utils.Utilitie;

public class FinvoraAIView {

    private String email;
    private TextField chatInput;
    private Button sendBtn;
    private ToggleButton voiceBtn;
    private Label aiStatusLabel;
    private TextArea aiAdviceArea;
    private Button backBtn;

    public FinvoraAIView(String email) {
        this.email = email;
    }

    public void show() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().addAll("main-background", "root");

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        backBtn = new Button("✖ Close");
        backBtn.getStyleClass().add("btn-secondary");
        
        Label titleLabel = new Label("✨ Finvora AI Assistant");
        titleLabel.getStyleClass().add("h2");
        titleLabel.setStyle("-fx-text-fill: #4F46E5; -fx-font-weight: 800;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(backBtn, spacer, titleLabel);

        // AI Chat Area
        aiAdviceArea = new TextArea();
        aiAdviceArea.setPromptText("Ask Finvora AI anything about your expenses, budgets, savings goals, or investments...");
        aiAdviceArea.setWrapText(true);
        aiAdviceArea.setEditable(false);
        aiAdviceArea.getStyleClass().addAll("card-elevated", "input-field");
        aiAdviceArea.setStyle("-fx-font-size: 15px; -fx-padding: 16px; -fx-line-spacing: 4px;");
        VBox.setVgrow(aiAdviceArea, Priority.ALWAYS);

        // Status
        aiStatusLabel = new Label("● Ready to assist with your financial intelligence.");
        aiStatusLabel.getStyleClass().add("caption-text");
        aiStatusLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 600;");

        // Input Area
        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER);
        
        chatInput = new TextField();
        chatInput.setPromptText("Ask about your budget, analyze spending patterns, or simulate investments...");
        chatInput.getStyleClass().add("input-field");
        chatInput.setStyle("-fx-font-size: 14px; -fx-pref-height: 46px;");
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setStyle("-fx-pref-height: 46px; -fx-pref-width: 90px;");
        
        voiceBtn = new ToggleButton("🎤 Voice");
        voiceBtn.getStyleClass().add("toggle-button");
        voiceBtn.setStyle("-fx-pref-height: 46px; -fx-pref-width: 100px;");
        
        inputBox.getChildren().addAll(chatInput, sendBtn, voiceBtn);

        root.getChildren().addAll(headerBox, aiAdviceArea, aiStatusLabel, inputBox);

        Scene scene = new Scene(root, 840, 620);
        org.example.utils.ThemeManager.apply(scene);

        javafx.stage.Stage aiStage = new javafx.stage.Stage();
        aiStage.setTitle("Finvora AI");
        org.example.utils.ViewNavigator.applyAppIcon(aiStage);
        aiStage.setScene(scene);
        aiStage.show();

        // Bind Controller
        new FinvoraAIController(this);
    }

    public String getEmail() { return email; }
    public TextField getChatInput() { return chatInput; }
    public Button getSendBtn() { return sendBtn; }
    public ToggleButton getVoiceBtn() { return voiceBtn; }
    public Label getAiStatusLabel() { return aiStatusLabel; }
    public TextArea getAiAdviceArea() { return aiAdviceArea; }
    public Button getBackBtn() { return backBtn; }
}

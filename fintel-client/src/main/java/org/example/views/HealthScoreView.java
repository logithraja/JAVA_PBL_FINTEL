package org.example.views;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.utils.ApiClient;
import org.example.utils.ThemeManager;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

public class HealthScoreView {

    private final String email;
    private final int userId;

    public HealthScoreView(String email, int userId) {
        this.email = email;
        this.userId = userId;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-background");

        HBox header = createHeader();
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane(createContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
        ThemeManager.apply(scene);
        ViewNavigator.switchViews(scene);
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 12, 24));
        header.getStyleClass().add("sidebar");

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> new DashboardView(email).show());

        Label icon = new Label("❤️");
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label("Financial Health Score");
        title.getStyleClass().add("h2");
        title.setStyle("-fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subtitle = new Label("Your overall financial wellness at a glance");
        subtitle.getStyleClass().add("caption-text");

        header.getChildren().addAll(backBtn, icon, title, spacer, subtitle);
        return header;
    }

    private VBox createContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 32, 20, 32));

        Label loadingLabel = new Label("Loading your health score...");
        loadingLabel.getStyleClass().add("body-text");
        loadingLabel.setStyle("-fx-text-fill: #94A3B8;");

        container.getChildren().add(loadingLabel);

        new Thread(() -> {
            JsonObject result = ApiClient.getHealthScore(userId);
            javafx.application.Platform.runLater(() -> {
                container.getChildren().clear();
                if (result == null) {
                    Label err = new Label("Could not load health score. Please try again later.");
                    err.getStyleClass().add("body-text");
                    err.setStyle("-fx-text-fill: #EF4444;");
                    container.getChildren().add(err);
                    return;
                }
                buildUI(container, result);
            });
        }).start();

        return container;
    }

    private void buildUI(VBox container, JsonObject result) {
        boolean sufficientData = result.has("sufficientData") && result.get("sufficientData").getAsBoolean();

        if (!sufficientData) {
            String msg = result.has("statusMessage") ? result.get("statusMessage").getAsString() : "Not enough data.";
            VBox noDataCard = new VBox(12);
            noDataCard.getStyleClass().addAll("card", "card-elevated");
            noDataCard.setPadding(new Insets(32));
            noDataCard.setAlignment(Pos.CENTER);

            Label emoji = new Label("📊");
            emoji.setStyle("-fx-font-size: 48px;");
            Label title = new Label("Not Enough Data");
            title.getStyleClass().add("h3");
            title.setStyle("-fx-font-weight: 700;");
            Label desc = new Label(msg);
            desc.getStyleClass().add("body-text");
            desc.setStyle("-fx-text-fill: #64748B;");
            desc.setWrapText(true);

            noDataCard.getChildren().addAll(emoji, title, desc);
            container.getChildren().add(noDataCard);
            return;
        }

        int score = result.has("overallScore") ? result.get("overallScore").getAsInt() : 0;
        String grade = result.has("grade") ? result.get("grade").getAsString() : "N/A";
        String summary = result.has("summary") ? result.get("summary").getAsString() : "";
        int months = result.has("monthsAnalyzed") ? result.get("monthsAnalyzed").getAsInt() : 0;

        VBox scoreCard = createScoreOverview(score, grade, summary, months);
        container.getChildren().add(scoreCard);

        if (result.has("savingsRate")) {
            container.getChildren().add(createComponentCard(result.getAsJsonObject("savingsRate"), "💰", "#10B981"));
        }
        if (result.has("spendingConsistency")) {
            container.getChildren().add(createComponentCard(result.getAsJsonObject("spendingConsistency"), "📈", "#6366F1"));
        }
        if (result.has("goalProgress")) {
            container.getChildren().add(createComponentCard(result.getAsJsonObject("goalProgress"), "🎯", "#F59E0B"));
        }
        if (result.has("budgetAdherence")) {
            container.getChildren().add(createComponentCard(result.getAsJsonObject("budgetAdherence"), "✅", "#06B6D4"));
        }

        if (result.has("recommendations")) {
            JsonArray recs = result.getAsJsonArray("recommendations");
            if (recs.size() > 0) {
                VBox recCard = new VBox(10);
                recCard.getStyleClass().addAll("card", "card-elevated");
                recCard.setPadding(new Insets(20));

                Label recTitle = new Label("💡 Recommendations");
                recTitle.getStyleClass().add("h4");
                recTitle.setStyle("-fx-font-weight: 700;");
                recCard.getChildren().add(recTitle);

                for (int i = 0; i < recs.size(); i++) {
                    Label recLabel = new Label("• " + recs.get(i).getAsString());
                    recLabel.getStyleClass().add("body-text");
                    recLabel.setWrapText(true);
                    recLabel.setStyle("-fx-text-fill: #475569;");
                    recCard.getChildren().add(recLabel);
                }

                container.getChildren().add(recCard);
            }
        }
    }

    private VBox createScoreOverview(int score, String grade, String summary, int months) {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(28));
        card.setAlignment(Pos.CENTER);

        Canvas gauge = new Canvas(200, 120);
        drawGauge(gauge, score);

        Label gradeLabel = new Label(grade);
        gradeLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 800; -fx-text-fill: " + scoreColor(score) + ";");

        Label scoreLabel = new Label(score + " / 100");
        scoreLabel.getStyleClass().add("body-text");
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B;");

        Label summaryLabel = new Label(summary);
        summaryLabel.getStyleClass().add("body-text");
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-text-alignment: center; -fx-max-width: 500px;");

        Label monthsLabel = new Label("Based on " + months + " month" + (months != 1 ? "s" : "") + " of data");
        monthsLabel.getStyleClass().add("caption-text");

        card.getChildren().addAll(gauge, gradeLabel, scoreLabel, summaryLabel, monthsLabel);
        return card;
    }

    private void drawGauge(Canvas canvas, int score) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double cx = w / 2;
        double cy = h - 10;
        double r = 85;

        gc.clearRect(0, 0, w, h);

        gc.setStroke(Color.web("#E2E8F0"));
        gc.setLineWidth(14);
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 0, 180, javafx.scene.shape.ArcType.OPEN);

        gc.setStroke(Color.web(scoreColor(score)));
        gc.setLineWidth(14);
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 180, (score / 100.0) * 180, javafx.scene.shape.ArcType.OPEN);
    }

    private VBox createComponentCard(JsonObject comp, String emoji, String color) {
        String name = comp.has("name") ? comp.get("name").getAsString() : "";
        int score = comp.has("score") ? comp.get("score").getAsInt() : 0;
        String label = comp.has("label") ? comp.get("label").getAsString() : "";
        String detail = comp.has("detail") ? comp.get("detail").getAsString() : "";

        VBox card = new VBox(8);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(18));

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 18px;");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("body-text");
        nameLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 15px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreBadge = new Label(score + "/100");
        scoreBadge.setStyle("-fx-background-color: " + scoreBgColor(score) + "; -fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + scoreColor(score) + ";");

        headerRow.getChildren().addAll(emojiLabel, nameLabel, spacer, scoreBadge);

        Label valueLabel = new Label(label);
        valueLabel.getStyleClass().add("body-text");
        valueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + "; -fx-font-weight: 600;");

        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("caption-text");
        detailLabel.setWrapText(true);
        detailLabel.setStyle("-fx-font-size: 13px;");

        card.getChildren().addAll(headerRow, valueLabel, detailLabel);
        return card;
    }

    private String scoreColor(int score) {
        if (score >= 80) return "#10B981";
        if (score >= 60) return "#6366F1";
        if (score >= 40) return "#F59E0B";
        return "#EF4444";
    }

    private String scoreBgColor(int score) {
        if (score >= 80) return "#D1FAE5";
        if (score >= 60) return "#E0E7FF";
        if (score >= 40) return "#FEF3C7";
        return "#FEE2E2";
    }
}

package org.example.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.utils.ThemeManager;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScenarioView {

    private String email;
    private int userId;
    private TextField amountField;
    private ComboBox<String> categoryComboBox;
    private ComboBox<Integer> monthComboBox;
    private ComboBox<Integer> yearComboBox;
    private Button simulateBtn;
    private Button backBtn;
    private VBox resultsContainer;
    private Label summaryLabel;
    private Label loadingLabel;

    public ScenarioView(String email, int userId) {
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
        new org.example.controllers.ScenarioController(this);
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 12, 24));
        header.getStyleClass().add("sidebar");

        backBtn = new Button("← Back");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> new DashboardView(email).show());

        Label icon = new Label("🔮");
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label("What-If Scenario");
        title.getStyleClass().add("h2");
        title.setStyle("-fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subtitle = new Label("See how a big purchase affects your finances");
        subtitle.getStyleClass().add("caption-text");

        header.getChildren().addAll(backBtn, icon, title, spacer, subtitle);
        return header;
    }

    private VBox createContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20, 32, 20, 32));

        VBox inputCard = createInputCard();
        resultsContainer = new VBox(16);
        resultsContainer.setStyle("-fx-background-color: transparent;");

        loadingLabel = new Label("Loading...");
        loadingLabel.setVisible(false);

        container.getChildren().addAll(inputCard, loadingLabel, resultsContainer);
        return container;
    }

    private VBox createInputCard() {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(24));

        Label cardTitle = new Label("🎯 Describe Your Scenario");
        cardTitle.getStyleClass().add("h3");
        cardTitle.setStyle("-fx-font-weight: 700;");

        Label cardSubtitle = new Label("What if you make a large purchase? We'll project the impact on your budget and savings goals.");
        cardSubtitle.getStyleClass().add("caption-text");
        cardSubtitle.setWrapText(true);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(12);

        Label amountLabel = new Label("Amount (₹)");
        amountLabel.getStyleClass().add("body-text");
        amountField = new TextField();
        amountField.setPromptText("e.g. 80000");
        amountField.getStyleClass().add("input-field");
        amountField.setStyle("-fx-pref-height: 40px;");
        HBox.setHgrow(amountField, Priority.ALWAYS);

        Label categoryLabel = new Label("Category");
        categoryLabel.getStyleClass().add("body-text");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select category...");
        categoryComboBox.getStyleClass().add("combo-box-custom");
        categoryComboBox.setStyle("-fx-pref-height: 40px;");
        categoryComboBox.getItems().addAll(
                "Food", "Transport", "Shopping", "Entertainment", "Rent",
                "Utilities", "Healthcare", "Education", "Travel", "Subscriptions", "Other"
        );

        Label monthLabel = new Label("Target Month");
        monthLabel.getStyleClass().add("body-text");
        monthComboBox = new ComboBox<>();
        monthComboBox.setPromptText("Month...");
        monthComboBox.getStyleClass().add("combo-box-custom");
        monthComboBox.setStyle("-fx-pref-height: 40px;");
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < 12; i++) {
            monthComboBox.getItems().add(i + 1);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());

        Label yearLabel = new Label("Year");
        yearLabel.getStyleClass().add("body-text");
        yearComboBox = new ComboBox<>();
        yearComboBox.getStyleClass().add("combo-box-custom");
        yearComboBox.setStyle("-fx-pref-height: 40px;");
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y <= currentYear + 2; y++) {
            yearComboBox.getItems().add(y);
        }
        yearComboBox.setValue(currentYear);

        formGrid.add(amountLabel, 0, 0);
        formGrid.add(amountField, 1, 0);
        formGrid.add(categoryLabel, 2, 0);
        formGrid.add(categoryComboBox, 3, 0);
        formGrid.add(monthLabel, 0, 1);
        formGrid.add(monthComboBox, 1, 1);
        formGrid.add(yearLabel, 2, 1);
        formGrid.add(yearComboBox, 3, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(80);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(100);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        simulateBtn = new Button("✨  Simulate Scenario");
        simulateBtn.getStyleClass().add("btn-primary");
        simulateBtn.setStyle("-fx-font-size: 15px; -fx-pref-height: 46px; -fx-font-weight: bold;");
        simulateBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(cardTitle, cardSubtitle, formGrid, simulateBtn);
        return card;
    }

    public void showResults(com.google.gson.JsonObject result) {
        resultsContainer.getChildren().clear();

        if (result == null) {
            Label errorLabel = new Label("Failed to simulate scenario. Please check your connection and try again.");
            errorLabel.getStyleClass().add("body-text");
            errorLabel.setStyle("-fx-text-fill: #EF4444;");
            resultsContainer.getChildren().add(errorLabel);
            return;
        }

        boolean sufficientData = result.has("sufficientData") && result.get("sufficientData").getAsBoolean();
        String overallSummary = result.has("overallSummary") ? result.get("overallSummary").getAsString() : "";

        Label summaryCard = new Label(overallSummary);
        summaryCard.getStyleClass().addAll("card", "card-elevated", "body-text");
        summaryCard.setWrapText(true);
        summaryCard.setPadding(new Insets(16));
        summaryCard.setStyle("-fx-font-size: 14px;");

        if (!sufficientData) {
            summaryCard.setStyle("-fx-font-size: 14px; -fx-background-color: #FEF3C7; -fx-text-fill: #92400E;");
        }

        resultsContainer.getChildren().add(summaryCard);

        if (result.has("goalImpacts")) {
            com.google.gson.JsonArray goalImpacts = result.getAsJsonArray("goalImpacts");
            if (goalImpacts.size() > 0) {
                VBox goalsCard = createGoalImpactsCard(goalImpacts);
                resultsContainer.getChildren().add(goalsCard);
            }
        }

        if (result.has("categoryAdjustments")) {
            com.google.gson.JsonArray adjustments = result.getAsJsonArray("categoryAdjustments");
            if (adjustments.size() > 0) {
                VBox adjCard = createCategoryAdjustmentsCard(adjustments);
                resultsContainer.getChildren().add(adjCard);
            }
        }

        if (result.has("monthlyProjection")) {
            com.google.gson.JsonObject projection = result.getAsJsonObject("monthlyProjection");
            if (projection.has("monthlyBreakdown")) {
                VBox projCard = createProjectionCard(projection);
                resultsContainer.getChildren().add(projCard);
            }
        }
    }

    private VBox createGoalImpactsCard(com.google.gson.JsonArray goalImpacts) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(20));

        Label title = new Label("🎯 Impact on Savings Goals");
        title.getStyleClass().add("h4");
        title.setStyle("-fx-font-weight: 700;");
        card.getChildren().add(title);

        for (int i = 0; i < goalImpacts.size(); i++) {
            com.google.gson.JsonObject g = goalImpacts.get(i).getAsJsonObject();
            String name = g.has("goalName") ? g.get("goalName").getAsString() : "Unknown";
            double additional = g.has("additionalMonthlyBurden") ? g.get("additionalMonthlyBurden").getAsDouble() : 0;
            boolean onTrack = g.has("onTrackAfter") && g.get("onTrackAfter").getAsBoolean();

            HBox goalRow = new HBox(12);
            goalRow.setAlignment(Pos.CENTER_LEFT);
            goalRow.setPadding(new Insets(8));
            goalRow.setStyle(onTrack ? "-fx-background-color: #D1FAE5; -fx-background-radius: 8;" :
                    "-fx-background-color: #FEE2E2; -fx-background-radius: 8;");

            Label goalIcon = new Label(onTrack ? "✅" : "⚠️");
            Label goalName = new Label(name);
            goalName.getStyleClass().add("body-text");
            goalName.setStyle("-fx-font-weight: 600;");

            Label burden = new Label(String.format("Additional ₹%.0f/mo needed", additional));
            burden.getStyleClass().add("caption-text");

            goalRow.getChildren().addAll(goalIcon, goalName, burden);
            card.getChildren().add(goalRow);
        }

        return card;
    }

    private VBox createCategoryAdjustmentsCard(com.google.gson.JsonArray adjustments) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(20));

        Label title = new Label("📊 Category Adjustments");
        title.getStyleClass().add("h4");
        title.setStyle("-fx-font-weight: 700;");
        card.getChildren().add(title);

        for (int i = 0; i < Math.min(adjustments.size(), 8); i++) {
            com.google.gson.JsonObject a = adjustments.get(i).getAsJsonObject();
            String catName = a.has("categoryName") ? a.get("categoryName").getAsString() : "";
            double avg = a.has("currentAvgMonthly") ? a.get("currentAvgMonthly").getAsDouble() : 0;
            double cut = a.has("percentCut") ? a.get("percentCut").getAsDouble() : 0;
            boolean vulnerable = a.has("vulnerable") && a.get("vulnerable").getAsBoolean();
            String rec = a.has("recommendation") ? a.get("recommendation").getAsString() : "";

            VBox adjRow = new VBox(4);
            adjRow.setPadding(new Insets(8));
            adjRow.setStyle(vulnerable ? "-fx-background-color: #FEF3C7; -fx-background-radius: 8;" :
                    "-fx-background-color: #F3F4F6; -fx-background-radius: 8;");

            HBox headerRow = new HBox(8);
            headerRow.setAlignment(Pos.CENTER_LEFT);
            Label catLabel = new Label((vulnerable ? "⚠️ " : "✅ ") + catName);
            catLabel.getStyleClass().add("body-text");
            catLabel.setStyle("-fx-font-weight: 600;");
            Label cutLabel = new Label(cut > 0 ? String.format("Cut: %.0f%%", cut) : "Stable");
            cutLabel.getStyleClass().add("caption-text");
            headerRow.getChildren().addAll(catLabel, cutLabel);

            Label recLabel = new Label(rec);
            recLabel.getStyleClass().add("caption-text");
            recLabel.setWrapText(true);

            adjRow.getChildren().addAll(headerRow, recLabel);
            card.getChildren().add(adjRow);
        }

        return card;
    }

    private VBox createProjectionCard(com.google.gson.JsonObject projection) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "card-elevated");
        card.setPadding(new Insets(20));

        Label title = new Label("📈 12-Month Projection");
        title.getStyleClass().add("h4");
        title.setStyle("-fx-font-weight: 700;");
        card.getChildren().add(title);

        double baseline = projection.has("totalBaselineNext12Months") ? projection.get("totalBaselineNext12Months").getAsDouble() : 0;
        double withScenario = projection.has("totalWithScenario") ? projection.get("totalWithScenario").getAsDouble() : 0;
        double impact = projection.has("scenarioImpact") ? projection.get("scenarioImpact").getAsDouble() : 0;

        HBox totalsRow = new HBox(20);
        totalsRow.setAlignment(Pos.CENTER_LEFT);

        VBox baselineBox = new VBox(2);
        Label blLabel = new Label("Baseline");
        blLabel.getStyleClass().add("caption-text");
        Label blValue = new Label(String.format("₹%.0f", baseline));
        blValue.getStyleClass().addAll("body-text", "stat-amount", "card-amount-income");
        baselineBox.getChildren().addAll(blLabel, blValue);

        VBox impactBox = new VBox(2);
        Label imLabel = new Label("Scenario Impact");
        imLabel.getStyleClass().add("caption-text");
        Label imValue = new Label(String.format("+₹%.0f", impact));
        imValue.getStyleClass().addAll("body-text", "stat-amount", "card-amount-expense");
        impactBox.getChildren().addAll(imLabel, imValue);

        VBox totalBox = new VBox(2);
        Label totLabel = new Label("New Total");
        totLabel.getStyleClass().add("caption-text");
        Label totValue = new Label(String.format("₹%.0f", withScenario));
        totValue.getStyleClass().addAll("body-text", "stat-amount", "card-amount-balance");
        totalBox.getChildren().addAll(totLabel, totValue);

        totalsRow.getChildren().addAll(baselineBox, impactBox, totalBox);
        card.getChildren().add(totalsRow);

        return card;
    }

    public Button getSimulateBtn() { return simulateBtn; }
    public TextField getAmountField() { return amountField; }
    public ComboBox<String> getCategoryComboBox() { return categoryComboBox; }
    public ComboBox<Integer> getMonthComboBox() { return monthComboBox; }
    public ComboBox<Integer> getYearComboBox() { return yearComboBox; }
    public Label getLoadingLabel() { return loadingLabel; }
    public VBox getResultsContainer() { return resultsContainer; }
    public String getEmail() { return email; }
    public int getUserId() { return userId; }
}

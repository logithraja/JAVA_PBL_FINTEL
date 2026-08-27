package org.example.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import org.example.animations.LoadingAnimationPane;
import org.example.controllers.DashboardController;
import org.example.models.MonthlyFinance;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.Year;
import java.util.Objects;
import javafx.collections.FXCollections;

public class DashboardView {

    private Label topGoalNameLabel;
    private ProgressBar topGoalProgressBar;
    private String email;
    private LoadingAnimationPane loadingAnimationPane;
    private Label currentBalanceLabel, currentBalance;
    private Label totalIncomeLabel, totalIncome;
    private Label totalExpenseLabel, totalExpense;

    private Label budgetStatusLabel, budgetRemaining;
    private Label forecastLabel, forecastAmountLabel;

    private Label userNameLabel, userEmailLabel;
    private ToggleButton themeToggle;
    private javafx.scene.layout.HBox mainContent;

    private ComboBox<Integer> yearComboBox;
    private ComboBox<String> monthQuarterComboBox;

    public Button addTransactionButton, viewChartButton, scanReceiptButton;
    private VBox recentTransactionBox;

    private MenuItem createCategoryMenuItem, viewCategoriesMenuItem, logoutMenuItem;
    private MenuItem exportDataMenuItem;
    private MenuItem generatePdfReportMenuItem;
    private MenuItem setMonthlyBudgetsMenuItem;
    private MenuItem viewBudgetProgressMenuItem;
    private MenuItem addGoalMenuItem;
    private MenuItem viewGoalsMenuItem;
    private MenuItem aboutUsMenuItem;

    private MenuItem convertCurrencyMenuItem;
    public Button fintelAIButton;

    private TableView<MonthlyFinance> transactionTable;
    public Button aiAlertsButton;
    private TableColumn<MonthlyFinance, String> monthColumn;
    private TableColumn<MonthlyFinance, BigDecimal> incomeColumn;
    private TableColumn<MonthlyFinance, BigDecimal> expenseColumn;

    private ToggleButton listenBtn;
    private Label aiStatusLabel;
    private TextArea aiAdviceArea;
    private TextField chatInput;
    private Button sendBtn;

    private Canvas balanceSparkline, incomeSparkline, expenseSparkline;
    private Label welcomeGreetingLabel, welcomeSubLabel;

    public DashboardView(String email) {
        this.email = email;
        loadingAnimationPane = new LoadingAnimationPane(Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);

        currentBalanceLabel = new Label("Current Balance:");
        totalIncomeLabel = new Label("Total Income:");
        totalExpenseLabel = new Label("Total Expense:");

        budgetStatusLabel = new Label("Budget Remaining:");
        budgetRemaining = new Label("₹0.00");

        forecastLabel = new Label("🔮  AI Spending Forecast:");
        forecastAmountLabel = new Label("Calculating pace...");

        currentBalance = new Label("₹0.00");
        totalIncome = new Label("₹0.00");
        totalExpense = new Label("₹0.00");
        addTransactionButton = new Button("+");
        scanReceiptButton = new Button("📷 Scan");

        monthQuarterComboBox = new ComboBox<>();

        userNameLabel = new Label("");
        userEmailLabel = new Label("");
        userNameLabel.getStyleClass().addAll("user-name-label");
        userEmailLabel.getStyleClass().addAll("user-email-label");

        themeToggle = new ToggleButton();
        themeToggle.getStyleClass().add("theme-toggle");

        createCategoryMenuItem = new MenuItem("Add Category");
        viewCategoriesMenuItem = new MenuItem("View Categories");
        aiAlertsButton = new Button("🔔 AI Alerts (0)");
        aiAlertsButton.getStyleClass().add("btn-secondary");
        aiAlertsButton.setStyle("-fx-font-size: 13px; -fx-padding: 6px 12px;");
        exportDataMenuItem = new MenuItem("Export Data");
        generatePdfReportMenuItem = new MenuItem("Generate PDF Report");
        logoutMenuItem = new MenuItem("Logout");
        setMonthlyBudgetsMenuItem = new MenuItem("Set Monthly Budgets");
        viewBudgetProgressMenuItem = new MenuItem("View Budget Progress");
        addGoalMenuItem = new MenuItem("Add Goal");
        viewGoalsMenuItem = new MenuItem("View Goals");
        aboutUsMenuItem = new MenuItem("About Us");

        convertCurrencyMenuItem = new MenuItem("Convert Currency...");
        fintelAIButton = new Button("✨ Fintel AI");
        fintelAIButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-cursor: hand;");

        yearComboBox = new ComboBox<>();
        transactionTable = new TableView<>();
        recentTransactionBox = new VBox();
        topGoalNameLabel = new Label();
        topGoalProgressBar = new ProgressBar();
        viewChartButton = new Button("View Chart");

        welcomeGreetingLabel = new Label();
        welcomeSubLabel = new Label();
    }

    public void show() {
        Scene scene = createScene();

        ThemeManager.apply(scene);
        scene.widthProperty().addListener((observable, oldVal, newVal) -> {
            loadingAnimationPane.resizeWidth(newVal.doubleValue());
            resizeTableWidthColumns();
        });
        scene.heightProperty().addListener((observable, oldVal, newVal) ->
                loadingAnimationPane.resizeHeight(newVal.doubleValue()));

        ViewNavigator.switchViews(scene);
        new DashboardController(this);
    }

    private Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-background");

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        VBox centerContent = new VBox();
        centerContent.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(centerContent, Priority.ALWAYS);

        HBox topBar = createTopBar();
        centerContent.getChildren().add(topBar);

        mainContent = new HBox();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        mainContent.setStyle("-fx-background-color: transparent;");

        VBox mainContainerWrapper = new VBox();
        mainContainerWrapper.setPadding(new Insets(20, 24, 20, 24));
        mainContainerWrapper.setSpacing(16);
        VBox.setVgrow(mainContainerWrapper, Priority.ALWAYS);
        HBox.setHgrow(mainContainerWrapper, Priority.ALWAYS);

        HBox greetingBox = createGreetingBox();
        HBox balanceSummaryBox = createBalanceSummaryBox();
        HBox forecastBanner = createForecastBanner();
        GridPane contentGridPane = createContentGridPane();
        VBox.setVgrow(contentGridPane, Priority.ALWAYS);
        mainContainerWrapper.getChildren().addAll(greetingBox, balanceSummaryBox, forecastBanner, contentGridPane);

        ScrollPane scrollPane = new ScrollPane(mainContainerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().add(scrollPane);
        centerContent.getChildren().add(mainContent);
        root.setCenter(centerContent);

        root.getChildren().add(loadingAnimationPane);

        Scene scene = new Scene(root, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);

        themeToggle.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        themeToggle.setSelected(ThemeManager.isDarkMode());
        themeToggle.setOnAction(e -> {
            ThemeManager.toggleTheme(scene);
            themeToggle.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        });

        return scene;
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private HBox createGreetingBox() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 4, 0));

        welcomeGreetingLabel.setText(getGreeting());
        welcomeGreetingLabel.getStyleClass().add("welcome-greeting");

        welcomeSubLabel.setText("Here's your financial overview");
        welcomeSubLabel.getStyleClass().add("welcome-sub");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        aiAlertsButton.setOnAction(null);
        box.getChildren().addAll(welcomeGreetingLabel, welcomeSubLabel, spacer, fintelAIButton, aiAlertsButton, themeToggle);
        return box;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 12, 20, 12));

        Label logo = new Label("✨ Fintel");
        logo.getStyleClass().add("sidebar-logo");

        VBox userArea = new VBox(2);
        userArea.getStyleClass().add("sidebar-user-area");
        Label avatar = new Label();
        String initial = email != null && !email.isEmpty() ? String.valueOf(email.charAt(0)).toUpperCase() : "U";
        avatar.setText(initial);
        avatar.getStyleClass().addAll("chat-avatar", "chat-avatar-user");
        HBox avatarRow = new HBox(10, avatar);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userNameLabel, Priority.ALWAYS);
        userNameLabel.getStyleClass().add("sidebar-user-name");
        userEmailLabel.getStyleClass().add("sidebar-user-email");
        VBox nameBox = new VBox(0, userNameLabel, userEmailLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        avatarRow.getChildren().add(nameBox);
        userArea.getChildren().add(avatarRow);

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("sidebar-section-label");

        Button dashboardBtn = createSidebarButton("📊  Dashboard");
        Button categoriesBtn = createSidebarButton("🏷  Categories");
        Button goalsBtn = createSidebarButton("🎯  Savings Goals");
        Button budgetsBtn = createSidebarButton("💰  Budgets");
        Button whatIfBtn = createSidebarButton("🔮  What-If Scenario");
        Button healthScoreBtn = createSidebarButton("❤️  Health Score");
        Button exportBtn = createSidebarButton("📤  Export / Reports");
        Button currencyBtn = createSidebarButton("💱  Currency Convert");

        dashboardBtn.getStyleClass().add("active");

        categoriesBtn.setOnAction(e -> viewCategoriesMenuItem.fire());
        goalsBtn.setOnAction(e -> {
            addGoalMenuItem.fire();
        });
        budgetsBtn.setOnAction(e -> viewBudgetProgressMenuItem.fire());
        whatIfBtn.setOnAction(e -> {
            int uid = 0;
            try {
                org.example.models.User u = org.example.utils.ApiClient.getUserByEmail(email);
                if (u != null) uid = u.getId();
            } catch (Exception ignored) {}
            new ScenarioView(email, uid).show();
        });
        healthScoreBtn.setOnAction(e -> {
            int uid = 0;
            try {
                org.example.models.User u = org.example.utils.ApiClient.getUserByEmail(email);
                if (u != null) uid = u.getId();
            } catch (Exception ignored) {}
            new HealthScoreView(email, uid).show();
        });
        exportBtn.setOnAction(e -> exportDataMenuItem.fire());
        currencyBtn.setOnAction(e -> convertCurrencyMenuItem.fire());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label systemLabel = new Label("SYSTEM");
        systemLabel.getStyleClass().add("sidebar-section-label");

        Button logoutBtn = createSidebarButton("🚪  Logout");
        logoutBtn.setStyle("-fx-text-fill: #EF4444;");
        logoutBtn.setOnAction(e -> logoutMenuItem.fire());

        sidebar.getChildren().addAll(logo, userArea, navLabel, dashboardBtn, categoriesBtn, goalsBtn, budgetsBtn, whatIfBtn, healthScoreBtn, exportBtn, currencyBtn, spacer, systemLabel, logoutBtn);
        return sidebar;
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 0, 0));
        topBar.setStyle("-fx-background-color: transparent;");
        return topBar;
    }

    private HBox createBalanceSummaryBox() {
        HBox statBox = new HBox(14);
        statBox.setAlignment(Pos.CENTER);
        statBox.setPadding(new Insets(0, 0, 0, 0));

        VBox balanceCard = new VBox(4);
        balanceCard.getStyleClass().addAll("stat-card", "card-balance");
        currentBalanceLabel.setText("💳  Current Balance");
        currentBalanceLabel.getStyleClass().setAll("stat-label", "card-title-balance");
        currentBalance.getStyleClass().setAll("stat-amount", "card-amount-balance");
        balanceSparkline = new Canvas(120, 30);
        balanceSparkline.getStyleClass().add("sparkline-canvas");
        balanceCard.getChildren().addAll(currentBalanceLabel, currentBalance, balanceSparkline);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        VBox incomeCard = new VBox(4);
        incomeCard.getStyleClass().addAll("stat-card", "card-income");
        totalIncomeLabel.setText("↗  Total Income");
        totalIncomeLabel.getStyleClass().setAll("stat-label", "card-title-income");
        totalIncome.getStyleClass().setAll("stat-amount", "card-amount-income");
        incomeSparkline = new Canvas(120, 30);
        incomeSparkline.getStyleClass().add("sparkline-canvas");
        incomeCard.getChildren().addAll(totalIncomeLabel, totalIncome, incomeSparkline);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);

        VBox expenseCard = new VBox(4);
        expenseCard.getStyleClass().addAll("stat-card", "card-expense");
        totalExpenseLabel.setText("↘  Total Expense");
        totalExpenseLabel.getStyleClass().setAll("stat-label", "card-title-expense");
        totalExpense.getStyleClass().setAll("stat-amount", "card-amount-expense");
        expenseSparkline = new Canvas(120, 30);
        expenseSparkline.getStyleClass().add("sparkline-canvas");
        expenseCard.getChildren().addAll(totalExpenseLabel, totalExpense, expenseSparkline);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);

        VBox budgetCard = new VBox(4);
        budgetCard.getStyleClass().addAll("stat-card", "card-budget");
        budgetStatusLabel.setText("📅  Budget Remaining");
        budgetStatusLabel.getStyleClass().setAll("stat-label", "card-title-budget");
        budgetRemaining.getStyleClass().setAll("stat-amount", "card-amount-budget");
        budgetCard.getChildren().addAll(budgetStatusLabel, budgetRemaining);
        HBox.setHgrow(budgetCard, Priority.ALWAYS);

        VBox topGoalCard = new VBox(4);
        topGoalCard.getStyleClass().addAll("stat-card", "card-savings");
        Label topGoalLabel = new Label("🎯  Savings Goal");
        topGoalLabel.getStyleClass().setAll("stat-label", "card-title-savings");
        topGoalNameLabel.setText("Check Sidebar to Set Goal");
        topGoalNameLabel.getStyleClass().setAll("body-text", "card-goal-name");
        topGoalProgressBar = new ProgressBar(0.0);
        topGoalProgressBar.setPrefWidth(160);
        topGoalProgressBar.setMaxWidth(Double.MAX_VALUE);
        topGoalProgressBar.getStyleClass().add("progress-bar");
        topGoalCard.getChildren().addAll(topGoalLabel, topGoalNameLabel, topGoalProgressBar);
        HBox.setHgrow(topGoalCard, Priority.ALWAYS);

        statBox.getChildren().addAll(balanceCard, incomeCard, expenseCard, budgetCard, topGoalCard);
        return statBox;
    }

    public void drawSparkline(Canvas canvas, double[] data, String color) {
        if (canvas == null || data == null || data.length < 2) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double v : data) {
            if (v > max) max = v;
            if (v < min) min = v;
        }
        if (max == min) { max = min + 1; }

        gc.setStroke(javafx.scene.paint.Color.web(color));
        gc.setLineWidth(2.0);

        double stepX = w / (data.length - 1);
        gc.beginPath();
        for (int i = 0; i < data.length; i++) {
            double x = i * stepX;
            double y = h - 4 - ((data[i] - min) / (max - min)) * (h - 8);
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
    }

    private HBox createForecastBanner() {
        HBox banner = new HBox(14);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().addAll("forecast-card");

        forecastLabel.setText("🔮  AI Spending Forecast:");
        forecastLabel.getStyleClass().setAll("h4", "forecast-title");

        forecastAmountLabel.getStyleClass().setAll("body-text", "forecast-value");
        HBox.setHgrow(forecastAmountLabel, Priority.ALWAYS);

        banner.getChildren().addAll(forecastLabel, forecastAmountLabel);
        return banner;
    }

    private GridPane createContentGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setMinWidth(0);

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(56);
        leftCol.setMinWidth(0);

        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(44);
        rightCol.setMinWidth(0);

        gridPane.getColumnConstraints().addAll(leftCol, rightCol);
        VBox transactionsTableSummaryBox = new VBox(14);
        transactionsTableSummaryBox.setMinWidth(0);

        HBox filterAndChartButtonBox = createFilterAndChartButtonBox();

        VBox transactionTableContentBox = createTransactionsTableContentBox();
        VBox.setVgrow(transactionTableContentBox, Priority.ALWAYS);
        transactionTableContentBox.setMinWidth(0);

        transactionsTableSummaryBox.getChildren().addAll(filterAndChartButtonBox, transactionTableContentBox);

        VBox recentTransactionsVBox = createRecentTransactionsVBox();
        recentTransactionsVBox.getStyleClass().addAll("card", "card-elevated");
        GridPane.setVgrow(recentTransactionsVBox, Priority.ALWAYS);
        recentTransactionsVBox.setMinWidth(0);

        VBox rightColumn = new VBox(10, recentTransactionsVBox);
        GridPane.setVgrow(rightColumn, Priority.ALWAYS);
        rightColumn.setMinWidth(0);

        gridPane.add(transactionsTableSummaryBox, 0, 0);
        gridPane.add(rightColumn, 1, 0);
        return gridPane;
    }

    private HBox createFilterAndChartButtonBox() {
        HBox hbox = new HBox(12);
        hbox.setAlignment(Pos.CENTER_LEFT);

        yearComboBox = new ComboBox<>();
        yearComboBox.getStyleClass().add("combo-box-custom");
        yearComboBox.setValue(Year.now().getValue());
        yearComboBox.setPrefWidth(120);

        monthQuarterComboBox = new ComboBox<>();
        monthQuarterComboBox.getStyleClass().add("combo-box-custom");
        monthQuarterComboBox.setPromptText("Filter Month/Quarter");
        monthQuarterComboBox.setPrefWidth(180);

        viewChartButton = new Button("📊 View Charts");
        viewChartButton.getStyleClass().add("btn-secondary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hbox.getChildren().addAll(yearComboBox, monthQuarterComboBox, spacer, viewChartButton);

        setupComboBoxAnimationAndAutoClose(yearComboBox);
        setupComboBoxAnimationAndAutoClose(monthQuarterComboBox);

        return hbox;
    }

    private void setupComboBoxAnimationAndAutoClose(ComboBox<?> comboBox) {
        comboBox.showingProperty().addListener((obs, oldVal, isShowing) -> {
            Platform.runLater(() -> {
                javafx.scene.Node popupNode = comboBox.lookup(".combo-box-popup");
                if (popupNode != null) {
                    if (isShowing) {
                        popupNode.setOpacity(0);
                        popupNode.setTranslateY(-10);
                        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), popupNode);
                        ft.setToValue(1);
                        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(150), popupNode);
                        tt.setToY(0);
                        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
                        pt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                        pt.play();

                        popupNode.setOnMouseExited(e -> {
                            if (comboBox.isShowing()) {
                                javafx.animation.FadeTransition ftClose = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), popupNode);
                                ftClose.setToValue(0);
                                javafx.animation.TranslateTransition ttClose = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(150), popupNode);
                                ttClose.setToY(-10);
                                javafx.animation.ParallelTransition ptClose = new javafx.animation.ParallelTransition(ftClose, ttClose);
                                ptClose.setInterpolator(javafx.animation.Interpolator.EASE_IN);
                                ptClose.setOnFinished(ev -> comboBox.hide());
                                ptClose.play();
                            }
                        });
                    }
                }
            });
        });
    }

    private VBox createTransactionsTableContentBox() {
        VBox vbox = new VBox();
        transactionTable = new TableView<>();
        VBox.setVgrow(transactionTable, Priority.ALWAYS);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        monthColumn = new TableColumn<>("Month");
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        monthColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String formatted = item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
                    setText(formatted);
                    setStyle("-fx-font-weight: 700; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 16px;");
                }
            }
        });

        incomeColumn = new TableColumn<>("Total Income");
        incomeColumn.setCellValueFactory(new PropertyValueFactory<>("income"));
        incomeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%,.2f", item.doubleValue()));
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: 700; -fx-alignment: CENTER_RIGHT; -fx-padding: 0 18px 0 0;");
                }
            }
        });

        expenseColumn = new TableColumn<>("Total Expense");
        expenseColumn.setCellValueFactory(new PropertyValueFactory<>("expense"));
        expenseColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%,.2f", item.doubleValue()));
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 700; -fx-alignment: CENTER_RIGHT; -fx-padding: 0 22px 0 0;");
                }
            }
        });

        transactionTable.getColumns().addAll(monthColumn, incomeColumn, expenseColumn);
        vbox.getChildren().add(transactionTable);
        resizeTableWidthColumns();
        return vbox;
    }

    private VBox createRecentTransactionsVBox() {
        VBox recentTransactionsVBox = new VBox(14);
        HBox labelAndButtonBox = new HBox(10);
        labelAndButtonBox.setAlignment(Pos.CENTER_LEFT);

        Label recentTransactionsLabel = new Label("Recent Transactions");
        recentTransactionsLabel.getStyleClass().add("h3");
        recentTransactionsLabel.setStyle("-fx-font-weight: 800;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        scanReceiptButton.setText("📷 Scan");
        scanReceiptButton.getStyleClass().setAll("btn-secondary");
        scanReceiptButton.setStyle("-fx-padding: 8px 14px; -fx-font-size: 13px;");

        addTransactionButton.setText("+ Add");
        addTransactionButton.getStyleClass().setAll("btn-primary");
        addTransactionButton.setStyle("-fx-padding: 8px 16px; -fx-font-size: 13px;");

        labelAndButtonBox.getChildren().addAll(recentTransactionsLabel, spacer, scanReceiptButton, addTransactionButton);

        recentTransactionBox = new VBox(10);
        recentTransactionBox.setStyle("-fx-background-color: transparent;");

        ScrollPane recentTransactionsScrollPane = new ScrollPane(recentTransactionBox);
        recentTransactionsScrollPane.setFitToWidth(true);
        recentTransactionsScrollPane.setFitToHeight(true);
        recentTransactionsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(recentTransactionsScrollPane, Priority.ALWAYS);

        recentTransactionsVBox.getChildren().addAll(labelAndButtonBox, recentTransactionsScrollPane);
        return recentTransactionsVBox;
    }

    public static VBox createEmptyRecentTransactionsNode() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50, 20, 50, 20));

        Label icon = new Label("🧾");
        icon.setStyle("-fx-font-size: 42px;");

        Label title = new Label("No transactions yet");
        title.getStyleClass().add("h4");
        title.setStyle("-fx-font-weight: 700;");

        Label subtitle = new Label("Scan a receipt or click '+ Add' to record your first transaction.");
        subtitle.getStyleClass().add("caption-text");
        subtitle.setStyle("-fx-text-alignment: center;");
        subtitle.setWrapText(true);

        box.getChildren().addAll(icon, title, subtitle);
        return box;
    }

    private void resizeTableWidthColumns() {
        Platform.runLater(() -> {
            double width = transactionTable.getWidth();
            if (width > 60) {
                double colWidth = (width - 4) / 3.0;
                monthColumn.setPrefWidth(colWidth);
                incomeColumn.setPrefWidth(colWidth);
                expenseColumn.setPrefWidth(colWidth);
            }
        });
    }

    public Label getBudgetStatusLabel() { return budgetStatusLabel; }
    public Label getBudgetRemaining() { return budgetRemaining; }
    public MenuItem getCreateCategoryMenuItem() { return createCategoryMenuItem; }
    public MenuItem getViewCategoriesMenuItem() { return viewCategoriesMenuItem; }
    public MenuItem getExportDataMenuItem() { return exportDataMenuItem; }
    public MenuItem getGeneratePdfReportMenuItem() { return generatePdfReportMenuItem; }
    public MenuItem getLogoutMenuItem() { return logoutMenuItem; }
    public MenuItem getSetMonthlyBudgetsMenuItem() { return setMonthlyBudgetsMenuItem; }
    public MenuItem getViewBudgetProgressMenuItem() { return viewBudgetProgressMenuItem; }
    public MenuItem getAddGoalMenuItem() { return addGoalMenuItem; }
    public MenuItem getViewGoalsMenuItem() { return viewGoalsMenuItem; }
    public MenuItem getAboutUsMenuItem() { return aboutUsMenuItem; }
    public String getEmail() { return email; }
    public Button getAddTransactionButton() { return addTransactionButton; }
    public VBox getRecentTransactionBox() { return recentTransactionBox; }
    public LoadingAnimationPane getLoadingAnimationPane() { return loadingAnimationPane; }
    public TableView<MonthlyFinance> getTransactionTable() { return transactionTable; }
    public TableColumn<MonthlyFinance, String> getMonthColumn() { return monthColumn; }
    public TableColumn<MonthlyFinance, BigDecimal> getIncomeColumn() { return incomeColumn; }
    public TableColumn<MonthlyFinance, BigDecimal> getExpenseColumn() { return expenseColumn; }
    public ComboBox<Integer> getYearComboBox() { return yearComboBox; }
    public Label getCurrentBalanceLabel() { return currentBalanceLabel; }
    public Label getTotalIncomeLabel() { return totalIncomeLabel; }
    public Label getTotalExpenseLabel() { return totalExpenseLabel; }
    public Label getCurrentBalance() { return currentBalance; }
    public ToggleButton getVoiceBtn() { return null; }
    public Label getTotalIncome() { return totalIncome; }
    public Label getTotalExpense() { return totalExpense; }
    public Button getViewChartButton() { return viewChartButton; }
    public ComboBox<String> getMonthQuarterComboBox() { return monthQuarterComboBox; }

    public Label getUserNameLabel() { return userNameLabel; }
    public Label getUserEmailLabel() { return userEmailLabel; }

    public MenuItem getConvertCurrencyMenuItem() { return convertCurrencyMenuItem; }

    public Label getTopGoalNameLabel() { return topGoalNameLabel; }
    public ProgressBar getTopGoalProgressBar() { return topGoalProgressBar; }

    public ToggleButton getThemeToggle() { return themeToggle; }

    public Label getAiStatusLabel() { return aiStatusLabel; }
    public TextArea getAiAdviceArea() { return aiAdviceArea; }
    public TextField getChatInput() { return chatInput; }
    public Button getSendBtn() { return sendBtn; }
    public Label getForecastLabel() { return forecastLabel; }
    public Label getForecastAmountLabel() { return forecastAmountLabel; }

    public Canvas getBalanceSparkline() { return balanceSparkline; }
    public Canvas getIncomeSparkline() { return incomeSparkline; }
    public Canvas getExpenseSparkline() { return expenseSparkline; }
    public Label getWelcomeGreetingLabel() { return welcomeGreetingLabel; }

    public javafx.stage.Window getWindow() {
        if (addTransactionButton != null && addTransactionButton.getScene() != null) {
            return addTransactionButton.getScene().getWindow();
        }
        return null;
    }
}

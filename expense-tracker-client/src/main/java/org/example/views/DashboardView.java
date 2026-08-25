package org.example.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.animations.LoadingAnimationPane;
import org.example.controllers.DashboardController;
import org.example.models.MonthlyFinance;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;
import org.example.utils.ThemeManager; 
import org.example.services.AIVoiceService;

import java.math.BigDecimal;
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
    private ImageView userIcon;
    private ToggleButton themeToggle; 
    private javafx.scene.layout.HBox mainContent; 

    private ComboBox<Integer> yearComboBox;
    private ComboBox<String> monthQuarterComboBox;

    public Button addTransactionButton, viewChartButton, scanReceiptButton;
    private VBox recentTransactionBox;
    private MenuBar menuBar;

    private MenuItem createCategoryMenuItem, viewCategoriesMenuItem, logoutMenuItem;
    private MenuItem exportDataMenuItem;
    private MenuItem generatePdfReportMenuItem;
    private MenuItem setMonthlyBudgetsMenuItem;
    private MenuItem viewBudgetProgressMenuItem;
    private MenuItem addGoalMenuItem;
    private MenuItem viewGoalsMenuItem;
    private MenuItem aboutUsMenuItem; 

    private MenuItem convertCurrencyMenuItem;
    public Button finvoraAIButton;
    
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

    public DashboardView(String email) {
        this.email = email;
        loadingAnimationPane = new LoadingAnimationPane(Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);

        currentBalanceLabel = new Label("Current Balance:");
        totalIncomeLabel = new Label("Total Income:");
        totalExpenseLabel = new Label("Total Expense:");

        budgetStatusLabel = new Label("Budget Remaining:");
        budgetRemaining = new Label("₹0.00");

        forecastLabel = new Label("📈 Month-End Forecast:");
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

        userIcon = createUserIcon();
        
        themeToggle = new ToggleButton(); 
        themeToggle.getStyleClass().add("theme-toggle");
        
        createCategoryMenuItem = new MenuItem("Add Category");
        viewCategoriesMenuItem = new MenuItem("View Categories");
        exportDataMenuItem = new MenuItem("Export Data (CSV)");
        aiAlertsButton = new Button("🔔 AI Alerts (0)");
        aiAlertsButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #111; -fx-font-weight: bold; -fx-cursor: hand;");
        generatePdfReportMenuItem = new MenuItem("Generate PDF Report");
        logoutMenuItem = new MenuItem("Logout");
        setMonthlyBudgetsMenuItem = new MenuItem("Set Monthly Budgets");
        viewBudgetProgressMenuItem = new MenuItem("View Budget Progress");
        addGoalMenuItem = new MenuItem("Add Goal");
        viewGoalsMenuItem = new MenuItem("View Goals");
        aboutUsMenuItem = new MenuItem("About Us"); 
        
        convertCurrencyMenuItem = new MenuItem("Convert Currency...");
        finvoraAIButton = new Button("✨ Finvora AI");
        finvoraAIButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-cursor: hand;");

        yearComboBox = new ComboBox<>();
        transactionTable = new TableView<>();
        recentTransactionBox = new VBox();
        topGoalNameLabel = new Label();
        topGoalProgressBar = new ProgressBar();
        viewChartButton = new Button("View Chart");
    }

    private ImageView createUserIcon() {
        String iconPath = "/images/userlogo.png";
        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)));
            ImageView iv = new ImageView(iconImage);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            return iv;
        } catch (Exception e) {
            System.err.println("Could not load user icon at: " + iconPath + ". Using empty ImageView.");
            return new ImageView();
        }
    }

    private ToggleButton voiceBtn;

    public void show() {
        Scene scene = createScene();
        
        ThemeManager.apply(scene);       
        new DashboardController(this);
        scene.widthProperty().addListener((observable, oldVal, newVal) -> {
            loadingAnimationPane.resizeWidth(newVal.doubleValue());
            resizeTableWidthColumns();
        });
        scene.heightProperty().addListener((observable, oldVal, newVal) ->
                loadingAnimationPane.resizeHeight(newVal.doubleValue()));
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene() {
        StackPane rootStack = new StackPane();
        rootStack.getStyleClass().add("main-background");
        
        VBox rootVBox = new VBox();
        rootVBox.getStyleClass().add("main-background");
        
        javafx.scene.layout.HBox topMenuBar = createTopMenuBar();

        mainContent = new javafx.scene.layout.HBox();
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        mainContent.setStyle("-fx-background-color: transparent;");

        VBox mainContainerWrapper = new VBox();
        mainContainerWrapper.getStyleClass().add("dashboard-padding");
        VBox.setVgrow(mainContainerWrapper, Priority.ALWAYS);
        HBox.setHgrow(mainContainerWrapper, Priority.ALWAYS);
        
        HBox balanceSummaryBox = createBalanceSummaryBox();
        HBox forecastBanner = createForecastBanner();
        GridPane contentGridPane = createContentGridPane();
        VBox.setVgrow(contentGridPane, Priority.ALWAYS);
        mainContainerWrapper.getChildren().addAll(balanceSummaryBox, forecastBanner, contentGridPane);

        ScrollPane scrollPane = new ScrollPane(mainContainerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background-color: transparent;");

        mainContent.getChildren().add(scrollPane);
        rootVBox.getChildren().addAll(topMenuBar, mainContent);
        
        rootStack.getChildren().addAll(rootVBox, loadingAnimationPane);

        Scene scene = new Scene(rootStack, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
        
        themeToggle.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        themeToggle.setSelected(ThemeManager.isDarkMode());
        themeToggle.setOnAction(e -> {
            ThemeManager.toggleTheme(scene);
            themeToggle.setText(ThemeManager.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        });

        return scene;
    }

    private javafx.scene.layout.HBox createTopMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: transparent; -fx-padding: 5px;");

        Menu categoryMenu = new Menu("Categories");
        categoryMenu.getItems().addAll(createCategoryMenuItem, viewCategoriesMenuItem);

        Menu savingsMenu = new Menu("Savings Goals");
        savingsMenu.getItems().addAll(addGoalMenuItem, viewGoalsMenuItem);

        Menu budgetMenu = new Menu("Budgets");
        budgetMenu.getItems().addAll(setMonthlyBudgetsMenuItem, viewBudgetProgressMenuItem);

        Menu exportMenu = new Menu("Export / Reports");
        exportMenu.getItems().addAll(exportDataMenuItem, generatePdfReportMenuItem);

        Menu currencyMenu = new Menu("Currency Convert");
        currencyMenu.getItems().add(convertCurrencyMenuItem);
        
        Menu systemMenu = new Menu("System");
        systemMenu.getItems().addAll(aboutUsMenuItem, logoutMenuItem);

        menuBar.getMenus().addAll(categoryMenu, savingsMenu, budgetMenu, exportMenu, currencyMenu, systemMenu);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, Priority.ALWAYS);
        
        themeToggle.getStyleClass().add("toggle-button");
        finvoraAIButton.getStyleClass().addAll("btn-primary");
        finvoraAIButton.setStyle("-fx-font-size: 13px; -fx-padding: 6px 14px;");
        
        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(10, menuBar, spacer, finvoraAIButton, aiAlertsButton, themeToggle);
        topBar.getStyleClass().add("top-bar-background");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new javafx.geometry.Insets(8, 18, 8, 10));
        
        return topBar;
    }

    private HBox createBalanceSummaryBox() {
        HBox statBox = new HBox(14);
        statBox.setAlignment(Pos.CENTER);
        statBox.setPadding(new Insets(10, 0, 18, 0));

        // Balance Card
        VBox balanceCard = new VBox(6);
        balanceCard.getStyleClass().addAll("stat-card", "card-balance");
        currentBalanceLabel.setText("💳  Current Balance");
        currentBalanceLabel.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 800; -fx-font-size: 13px;");
        currentBalance.getStyleClass().setAll("stat-amount");
        currentBalance.setStyle("-fx-text-fill: #4338CA; -fx-font-size: 26px; -fx-font-weight: 800;");
        balanceCard.getChildren().addAll(currentBalanceLabel, currentBalance);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        // Income Card
        VBox incomeCard = new VBox(6);
        incomeCard.getStyleClass().addAll("stat-card", "card-income");
        totalIncomeLabel.setText("↗  Total Income");
        totalIncomeLabel.setStyle("-fx-text-fill: #065F46; -fx-font-weight: 800; -fx-font-size: 13px;");
        totalIncome.getStyleClass().setAll("stat-amount");
        totalIncome.setStyle("-fx-text-fill: #059669; -fx-font-size: 26px; -fx-font-weight: 800;");
        incomeCard.getChildren().addAll(totalIncomeLabel, totalIncome);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);

        // Expense Card
        VBox expenseCard = new VBox(6);
        expenseCard.getStyleClass().addAll("stat-card", "card-expense");
        totalExpenseLabel.setText("↘  Total Expense");
        totalExpenseLabel.setStyle("-fx-text-fill: #991B1B; -fx-font-weight: 800; -fx-font-size: 13px;");
        totalExpense.getStyleClass().setAll("stat-amount");
        totalExpense.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 26px; -fx-font-weight: 800;");
        expenseCard.getChildren().addAll(totalExpenseLabel, totalExpense);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);

        // Budget Card
        VBox budgetCard = new VBox(6);
        budgetCard.getStyleClass().addAll("stat-card", "card-budget");
        budgetStatusLabel.setText("📅  Budget Status");
        budgetStatusLabel.setStyle("-fx-text-fill: #0E7490; -fx-font-weight: 800; -fx-font-size: 13px;");
        budgetRemaining.getStyleClass().setAll("stat-amount");
        budgetRemaining.setStyle("-fx-text-fill: #0891B2; -fx-font-size: 26px; -fx-font-weight: 800;");
        budgetCard.getChildren().addAll(budgetStatusLabel, budgetRemaining);
        HBox.setHgrow(budgetCard, Priority.ALWAYS);

        // Savings Card
        VBox topGoalCard = new VBox(6);
        topGoalCard.getStyleClass().addAll("stat-card", "card-savings");
        Label topGoalLabel = new Label("🎯  Savings Goal");
        topGoalLabel.setStyle("-fx-text-fill: #581C87; -fx-font-weight: 800; -fx-font-size: 13px;");
        topGoalNameLabel.setText("Check Menu to Set Goal");
        topGoalNameLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-weight: 800; -fx-font-size: 14px;");
        topGoalProgressBar = new ProgressBar(0.0);
        topGoalProgressBar.setPrefWidth(160);
        topGoalProgressBar.setMaxWidth(Double.MAX_VALUE);
        topGoalProgressBar.getStyleClass().add("progress-bar");
        topGoalCard.getChildren().addAll(topGoalLabel, topGoalNameLabel, topGoalProgressBar);
        HBox.setHgrow(topGoalCard, Priority.ALWAYS);

        statBox.getChildren().addAll(balanceCard, incomeCard, expenseCard, budgetCard, topGoalCard);
        return statBox;
    }

    private HBox createForecastBanner() {
        HBox banner = new HBox(14);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().addAll("forecast-card");

        forecastLabel.setText("🔮  AI Spending Forecast:");
        forecastLabel.getStyleClass().setAll("h4");
        forecastLabel.setStyle("-fx-text-fill: #3730A3; -fx-font-weight: 800;");

        forecastAmountLabel.getStyleClass().setAll("body-text");
        forecastAmountLabel.setStyle("-fx-text-fill: #4338CA; -fx-font-weight: 800; -fx-font-size: 14px;");
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
        return hbox;
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
                    setStyle("-fx-font-weight: 700; -fx-text-fill: #1E293B; -fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 16px;");
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
                    setStyle("-fx-text-fill: #059669; -fx-font-weight: 700; -fx-alignment: CENTER_RIGHT; -fx-padding: 0 18px 0 0;");
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
                    setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-alignment: CENTER_RIGHT; -fx-padding: 0 22px 0 0;");
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
    public MenuBar getMenuBar() { return this.menuBar; }
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
    public Label getCurrentBalance() { return currentBalance; }
    public ToggleButton getVoiceBtn() { return voiceBtn; }
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
}

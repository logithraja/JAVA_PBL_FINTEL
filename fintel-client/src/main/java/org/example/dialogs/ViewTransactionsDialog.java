package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.components.TransactionComponent;
import org.example.controllers.DashboardController;
import org.example.models.Transaction;
import org.example.utils.ApiClient;
import org.example.utils.ThemeManager;

import java.time.Month;
import java.util.List;

public class ViewTransactionsDialog extends CustomDialog {

    private final DashboardController dashboardController;
    private final String monthName;
    private final int monthValue;

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private int totalPages = 1;
    private long totalElements = 0;

    private final VBox transactionListBox = new VBox(12);
    private final Button prevButton = new Button("Previous");
    private final Button nextButton = new Button("Next");
    private final Label pageInfoLabel = new Label("Page 1 of 1");
    private final Label headerLabel = new Label();

    public ViewTransactionsDialog(DashboardController dashboardController, String monthName) {
        super(dashboardController.getUser());

        this.dashboardController = dashboardController;
        this.monthName = monthName;
        this.monthValue = Month.valueOf(monthName).getValue();

        setTitle("View Transactions - " + monthName + " " + dashboardController.getCurrentYear());
        setWidth(1000);
        setHeight(700);

        VBox contentLayout = buildLayout();
        getDialogPane().setContent(contentLayout);

        loadPage(0);
    }

    private VBox buildLayout() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(16));

        headerLabel.setText("Transactions for " + monthName + " " + dashboardController.getCurrentYear());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        transactionListBox.setPadding(new Insets(8));

        ScrollPane scrollPane = new ScrollPane(transactionListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(520);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                loadPage(currentPage - 1);
            }
        });
        prevButton.setDisable(true);

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                loadPage(currentPage + 1);
            }
        });
        nextButton.setDisable(true);

        HBox paginationBar = new HBox(16, prevButton, pageInfoLabel, nextButton);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(8, 0, 0, 0));

        root.getChildren().addAll(headerLabel, scrollPane, paginationBar);
        return root;
    }

    private void loadPage(int page) {
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        pageInfoLabel.setText("Loading page " + (page + 1) + "...");

        new Thread(() -> {
            ApiClient.PageResult<Transaction> result = ApiClient.getPagedTransactions(
                    dashboardController.getUser().getId(),
                    dashboardController.getCurrentYear(),
                    monthValue,
                    page,
                    PAGE_SIZE
            );

            Platform.runLater(() -> {
                currentPage = result.getPageNumber();
                totalPages = Math.max(1, result.getTotalPages());
                totalElements = result.getTotalElements();

                pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " total)");
                prevButton.setDisable(result.isFirst() || currentPage == 0);
                nextButton.setDisable(result.isLast() || currentPage >= totalPages - 1);

                transactionListBox.getChildren().clear();
                List<Transaction> transactions = result.getContent();
                if (transactions != null && !transactions.isEmpty()) {
                    for (Transaction t : transactions) {
                        TransactionComponent comp = new TransactionComponent(dashboardController, t);
                        transactionListBox.getChildren().add(comp);
                    }
                } else {
                    Label emptyLabel = new Label("No transactions found for this period.");
                    emptyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-padding: 24px; -fx-alignment: center;");
                    transactionListBox.getChildren().add(emptyLabel);
                }
            });
        }).start();
    }
}
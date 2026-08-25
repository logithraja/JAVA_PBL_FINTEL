package org.example.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.example.controllers.DashboardController;
import org.example.dialogs.CreateOrEditTransactionDialog;
import org.example.models.Transaction;
import org.example.utils.ApiClient;

public class TransactionComponent extends HBox {
    private Label transactionCategoryLabel, transactionNameLabel, transactionDateLabel, transactionAmountLabel;
    private Button editButton, delButton;

    private DashboardController dashboardController;
    private Transaction transaction;

    public TransactionComponent(DashboardController dashboardController, Transaction transaction){
        this.dashboardController = dashboardController;
        this.transaction = transaction;

        setSpacing(12);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("card");
        setStyle("-fx-padding: 12px 16px; -fx-background-radius: 14px; -fx-border-radius: 14px;");

        VBox categoryNameDateSection = createCategoryNameDateSection();

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        transactionAmountLabel = new Label("₹" + transaction.getTransactionAmount());
        transactionAmountLabel.getStyleClass().add("h3");
        transactionAmountLabel.setMinWidth(Region.USE_PREF_SIZE);
        
        if(transaction.getTransactionType().equalsIgnoreCase("expense")){
            transactionAmountLabel.getStyleClass().add("text-error");
        }else{
            transactionAmountLabel.getStyleClass().add("text-success");
        }

        HBox actionButtonSection = createActionButtons();
        actionButtonSection.setMinWidth(Region.USE_PREF_SIZE);

        HBox.setHgrow(categoryNameDateSection, Priority.ALWAYS);
        categoryNameDateSection.setMinWidth(0);

        getChildren().addAll(categoryNameDateSection, region, transactionAmountLabel, actionButtonSection);
    }

    private VBox createCategoryNameDateSection(){
        VBox categoryNameDateSection = new VBox(2);

        if(transaction.getTransactionCategory() == null){
            transactionCategoryLabel = new Label("Undefined");
            transactionCategoryLabel.getStyleClass().add("caption-text");
        }else{
            transactionCategoryLabel = new Label(transaction.getTransactionCategory().getCategoryName());
            transactionCategoryLabel.getStyleClass().add("caption-text");
            String catColor = transaction.getTransactionCategory().getCategoryColor();
            if (catColor != null && !catColor.startsWith("#")) {
                catColor = "#" + catColor;
            }
            transactionCategoryLabel.setTextFill(Paint.valueOf(catColor));
            transactionCategoryLabel.setStyle("-fx-font-weight: bold;");
        }

        transactionNameLabel = new Label(transaction.getTransactionName());
        transactionNameLabel.getStyleClass().add("body-text");
        transactionNameLabel.setStyle("-fx-font-weight: bold;");
        transactionNameLabel.setMinWidth(0);

        String dateStr = transaction.getTransactionDate().toString();
        if (transaction.getTransactionTime() != null && !transaction.getTransactionTime().isEmpty()) {
            dateStr += " at " + transaction.getTransactionTime();
        }
        transactionDateLabel = new Label(dateStr);
        transactionDateLabel.getStyleClass().add("caption-text");

        categoryNameDateSection.getChildren().addAll(transactionCategoryLabel, transactionNameLabel, transactionDateLabel);
        return categoryNameDateSection;
    }

    private HBox createActionButtons(){
        HBox actionButtonSection = new HBox(8);
        actionButtonSection.setAlignment(Pos.CENTER);

        editButton = new Button("Edit");
        editButton.getStyleClass().add("btn-secondary");
        editButton.setStyle("-fx-padding: 6px 12px; -fx-font-size: 12px;");
        editButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new CreateOrEditTransactionDialog(dashboardController, TransactionComponent.this,
                        true).showAndWait();
            }
        });

        delButton = new Button("Del");
        delButton.getStyleClass().add("btn-danger");
        delButton.setStyle("-fx-padding: 6px 12px; -fx-font-size: 12px;");
        delButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!ApiClient.deleteTransactionById(transaction.getId())){
                    return;
                }

                setVisible(false);
                setManaged(false);
                if(getParent() instanceof VBox){
                    ((VBox) getParent()).getChildren().remove(TransactionComponent.this);
                }

                dashboardController.fetchUserData();
            }
        });

        actionButtonSection.getChildren().addAll(editButton, delButton);
        return actionButtonSection;
    }

    public Transaction getTransaction(){return transaction;}

    public Label getTransactionCategoryLabel() {
        return transactionCategoryLabel;
    }

    public Label getTransactionNameLabel() {
        return transactionNameLabel;
    }

    public Label getTransactionDateLabel() {
        return transactionDateLabel;
    }

    public Label getTransactionAmountLabel() {
        return transactionAmountLabel;
    }
}

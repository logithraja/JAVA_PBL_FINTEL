package org.example.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.controllers.DashboardController;
import org.example.models.TransactionCategory;
import org.example.utils.ApiClient;
import org.example.utils.Utilitie;

public class CategoryComponent extends HBox {
    private DashboardController dashboardController;
    private TransactionCategory transactionCategory;

    private TextField categoryTextField;
    private ColorPicker colorPicker;
    private Button editButton, saveButton, deleteButton;

    private boolean isEditing;

    public CategoryComponent(DashboardController dashboardController, TransactionCategory transactionCategory){
        this.dashboardController = dashboardController;
        this.transactionCategory = transactionCategory;

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("card");
        setStyle("-fx-padding: 10px 14px; -fx-background-radius: 12px;");

        categoryTextField = new TextField();
        categoryTextField.setText(transactionCategory.getCategoryName());
        categoryTextField.setMaxWidth(Double.MAX_VALUE);
        categoryTextField.setEditable(false);
        HBox.setHgrow(categoryTextField, Priority.ALWAYS);
        categoryTextField.getStyleClass().add("input-field");

        colorPicker = new ColorPicker();
        colorPicker.setDisable(true);
        colorPicker.setValue(Color.valueOf(transactionCategory.getCategoryColor()));
        colorPicker.getStyleClass().add("combo-box-custom");

        editButton = new Button("Edit");
        editButton.setMinWidth(60);
        editButton.getStyleClass().add("btn-secondary");
        editButton.setStyle("-fx-padding: 6px 12px; -fx-font-size: 12px;");
        editButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleToggle();
            }
        });

        saveButton = new Button("Save");
        saveButton.setMinWidth(60);
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setStyle("-fx-padding: 6px 12px; -fx-font-size: 12px;");
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleToggle();

                String newCatgoryName = categoryTextField.getText();
                String newCategoryColor = Utilitie.getHexColorValue(colorPicker);

                ApiClient.putTransactionCategory(transactionCategory.getId(), newCatgoryName, newCategoryColor);

                dashboardController.fetchUserData();
            }
        });

        deleteButton = new Button("Del");
        deleteButton.setMinWidth(60);
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setStyle("-fx-padding: 6px 12px; -fx-font-size: 12px;");
        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!ApiClient.deleteTransactionCategoryById(transactionCategory.getId())){
                    return;
                }

                setVisible(false);
                setManaged(false);

                if(getParent() instanceof VBox){
                    ((VBox) getParent()).getChildren().remove(CategoryComponent.this);
                }
            }
        });

        getChildren().addAll(categoryTextField, colorPicker, editButton, saveButton, deleteButton);
    }

    private void handleToggle(){
        if(!isEditing){
            isEditing = true;

            categoryTextField.setEditable(true);
            categoryTextField.setStyle("-fx-background-color: #fff; -fx-text-fill: #000");

            colorPicker.setDisable(false);

            editButton.setVisible(false);
            editButton.setManaged(false);

            saveButton.setVisible(true);
            saveButton.setManaged(true);
        }else{
            isEditing = false;

            categoryTextField.setEditable(false);
            categoryTextField.setStyle("-fx-background-color: #515050; -fx-text-fill: #BEB9B9");

            colorPicker.setDisable(true);

            editButton.setVisible(true);
            editButton.setManaged(true);

            saveButton.setVisible(false);
            saveButton.setManaged(false);
        }
    }
}

package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.example.models.SavingsGoal;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateGoalDialog extends Dialog<SavingsGoal> {
    public CreateGoalDialog() {
        setTitle("Create New Goal");
        setHeaderText("Enter your savings goal details:");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        getDialogPane().getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        getDialogPane().setPrefWidth(420);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Goal name (e.g. Vacation, Car)");
        nameField.getStyleClass().add("input-field");

        TextField targetAmountField = new TextField();
        targetAmountField.setPromptText("Target amount (e.g. 50000)");
        targetAmountField.getStyleClass().add("input-field");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Target Deadline");
        deadlinePicker.getStyleClass().add("input-field");
        deadlinePicker.setMaxWidth(Double.MAX_VALUE);

        Label nameLbl = new Label("Goal Name:");
        nameLbl.getStyleClass().add("body-text");
        Label amtLbl = new Label("Target Amount (₹):");
        amtLbl.getStyleClass().add("body-text");
        Label dateLbl = new Label("Target Date:");
        dateLbl.getStyleClass().add("body-text");

        grid.add(nameLbl, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(amtLbl, 0, 1);
        grid.add(targetAmountField, 1, 1);
        grid.add(dateLbl, 0, 2);
        grid.add(deadlinePicker, 1, 2);

        getDialogPane().setContent(grid);

        setOnShowing(e -> {
            if (getDialogPane().getScene() != null) {
                ThemeManager.apply(getDialogPane().getScene());
                javafx.stage.Window w = getDialogPane().getScene().getWindow();
                if (w instanceof javafx.stage.Stage stage) {
                    stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/fintel_app_icon.png")));
                }
            }
        });

        setResultConverter(new Callback<ButtonType, SavingsGoal>() {
            @Override
            public SavingsGoal call(ButtonType buttonType) {
                if (buttonType == createButtonType) {
                    try {
                        String name = nameField.getText();
                        BigDecimal target = new BigDecimal(targetAmountField.getText());
                        LocalDate date = deadlinePicker.getValue();

                        return new SavingsGoal(name, target, BigDecimal.ZERO, date);
                    } catch (Exception e) {
                        FintelAlert.showError("Please enter valid goal details and target amount.");
                        return null;
                    }
                }
                return null;
            }
        });
    }
}
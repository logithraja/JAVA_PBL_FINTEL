package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.models.User;
import org.example.utils.ThemeManager;

import java.time.LocalDate;

public class ExportDataDialog extends Dialog<ExportDataDialog.ExportOptions> {

    private final CheckBox cbTransactions = new CheckBox("Transactions");
    private final CheckBox cbCategories = new CheckBox("Categories");
    private final CheckBox cbBudgets = new CheckBox("Budgets");

    private final DatePicker startDate = new DatePicker();
    private final DatePicker endDate = new DatePicker();

    public ExportDataDialog(User user) {
        setTitle("Export Data (CSV)");
        setHeaderText("Choose what to export and an optional date range");

        ButtonType okBtn = new ButtonType("Export", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        getDialogPane().getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());

        cbTransactions.setSelected(true);
        startDate.getStyleClass().add("input-field");
        endDate.getStyleClass().add("input-field");
        startDate.setPromptText("Start date");
        endDate.setPromptText("End date");

        GridPane gp = new GridPane();
        gp.setHgap(14);
        gp.setVgap(12);
        gp.setPadding(new Insets(18));

        Label incLbl = new Label("Include:");
        incLbl.getStyleClass().add("body-text");
        Label startLbl = new Label("Start date:");
        startLbl.getStyleClass().add("body-text");
        Label endLbl = new Label("End date:");
        endLbl.getStyleClass().add("body-text");

        gp.add(incLbl, 0, 0);
        gp.add(cbTransactions, 1, 0);
        gp.add(cbCategories, 1, 1);
        gp.add(cbBudgets, 1, 2);

        gp.add(startLbl, 0, 3);
        gp.add(startDate, 1, 3);

        gp.add(endLbl, 0, 4);
        gp.add(endDate, 1, 4);

        getDialogPane().setContent(gp);

        setOnShowing(e -> {
            if (getDialogPane().getScene() != null) {
                ThemeManager.apply(getDialogPane().getScene());
            }
        });

        final Button okButton = (Button) getDialogPane().lookupButton(okBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!cbTransactions.isSelected() && !cbCategories.isSelected() && !cbBudgets.isSelected()) {
                FinvoraAlert.showWarning("Select at least one item to export.");
                evt.consume();
                return;
            }

            LocalDate s = startDate.getValue();
            LocalDate e = endDate.getValue();
            if (s != null && e != null && e.isBefore(s)) {
                FinvoraAlert.showWarning("End date must be on or after start date.");
                evt.consume();
            }
        });

        setResultConverter(bt -> {
            if (bt != okBtn) return null;

            return new ExportOptions(
                    cbTransactions.isSelected(),
                    cbCategories.isSelected(),
                    cbBudgets.isSelected(),
                    startDate.getValue(),
                    endDate.getValue()
            );
        });
    }

    public static class ExportOptions {
        public final boolean transactions;
        public final boolean categories;
        public final boolean budgets;
        public final LocalDate start;
        public final LocalDate end;

        public ExportOptions(boolean transactions, boolean categories, boolean budgets,
                             LocalDate start, LocalDate end) {
            this.transactions = transactions;
            this.categories = categories;
            this.budgets = budgets;
            this.start = start;
            this.end = end;
        }
    }
}
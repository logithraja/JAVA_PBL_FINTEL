package org.example.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.models.TransactionCategory;
import org.example.models.User;
import org.example.utils.ApiClient;
import org.example.utils.CsvImportUtil;

import java.io.File;
import java.util.List;

public class ImportDataDialog extends Dialog<Boolean> {

    private final User user;
    private final Runnable onSuccessCallback;
    private File selectedFile;
    private CsvImportUtil.ImportResult preparedResult;

    private final Label fileLabel = new Label("No file selected");
    private final Label statusLabel = new Label("Select a CSV file to import transactions");
    private final Button chooseFileBtn = new Button("Browse CSV...");
    private final Button importBtn = new Button("Import Transactions");

    public ImportDataDialog(User user, Runnable onSuccessCallback) {
        this.user = user;
        this.onSuccessCallback = onSuccessCallback;

        setTitle("Import Transactions (CSV)");
        setHeaderText("Import financial records from a bank or spreadsheet CSV");

        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        if (getClass().getResource("/theme.css") != null) {
            getDialogPane().getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        }

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setPrefWidth(550);

        HBox fileRow = new HBox(12, chooseFileBtn, fileLabel);
        fileRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fileLabel, Priority.ALWAYS);

        statusLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-wrap-text: true;");
        statusLabel.setPrefHeight(60);

        importBtn.setDisable(true);
        importBtn.setStyle("-fx-font-weight: bold;");

        chooseFileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select CSV File to Import");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
            File file = chooser.showOpenDialog(getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile = file;
                fileLabel.setText(file.getName());
                parseFile();
            }
        });

        importBtn.setOnAction(e -> executeImport());

        root.getChildren().addAll(fileRow, statusLabel, importBtn);
        getDialogPane().setContent(root);
    }

    private void parseFile() {
        if (selectedFile == null) return;
        statusLabel.setText("Analyzing CSV and checking duplicates...");

        new Thread(() -> {
            List<TransactionCategory> categories = ApiClient.getAllTransactionCategoriesByUser(user);
            preparedResult = CsvImportUtil.parseAndPrepare(selectedFile, user, categories);

            javafx.application.Platform.runLater(() -> {
                if (preparedResult.errors.isEmpty() || preparedResult.importedCount > 0) {
                    statusLabel.setText(String.format(
                            "Found %d rows.\n%d new transactions ready to import.\n%d duplicate(s) skipped.",
                            preparedResult.totalRows,
                            preparedResult.importedCount,
                            preparedResult.duplicatesSkipped
                    ));
                    importBtn.setDisable(preparedResult.importedCount == 0);
                } else {
                    statusLabel.setText("Failed to parse CSV: " + String.join("\n", preparedResult.errors));
                    importBtn.setDisable(true);
                }
            });
        }).start();
    }

    private void executeImport() {
        if (preparedResult == null || preparedResult.jsonPayload.isEmpty()) return;

        importBtn.setDisable(true);
        statusLabel.setText("Importing " + preparedResult.importedCount + " transactions...");

        new Thread(() -> {
            boolean success = ApiClient.postTransactionsBulk(preparedResult.jsonPayload);

            javafx.application.Platform.runLater(() -> {
                if (success) {
                    FintelAlert.showSuccess(String.format("Successfully imported %d transactions!", preparedResult.importedCount));
                    if (onSuccessCallback != null) onSuccessCallback.run();
                    close();
                } else {
                    FintelAlert.showError("Failed to import transactions. Please check your network and try again.");
                    importBtn.setDisable(false);
                }
            });
        }).start();
    }
}

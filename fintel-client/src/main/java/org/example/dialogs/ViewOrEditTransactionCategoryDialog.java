package org.example.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.example.components.CategoryComponent;
import org.example.controllers.DashboardController;
import org.example.models.TransactionCategory;
import org.example.models.User;
import org.example.utils.ApiClient;

import java.util.List;

public class ViewOrEditTransactionCategoryDialog extends CustomDialog {
    private DashboardController dashboardController;
    private VBox dialogVBox;

    public ViewOrEditTransactionCategoryDialog(User user, DashboardController dashboardController) {
        super(user);
        this.dashboardController = dashboardController;

        setTitle("View Categories");
        setWidth(1000);
        setHeight(700);

        ScrollPane mainContainer = createMainContainerContent();
        getDialogPane().setContent(mainContainer);

        loadCategoriesAsync();
    }

    private ScrollPane createMainContainerContent() {
        dialogVBox = new VBox(12);
        dialogVBox.setPadding(new Insets(16));

        Label loadingLabel = new Label("Loading categories...");
        loadingLabel.getStyleClass().add("body-text");
        loadingLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
        loadingLabel.setPadding(new Insets(40, 0, 0, 0));
        loadingLabel.setAlignment(Pos.CENTER);
        dialogVBox.getChildren().add(loadingLabel);

        ScrollPane scrollPane = new ScrollPane(dialogVBox);
        scrollPane.setMinHeight(getHeight() - 40);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    private void loadCategoriesAsync() {
        new Thread(() -> {
            try {
                List<TransactionCategory> categories = ApiClient.getAllTransactionCategoriesByUser(user);
                javafx.application.Platform.runLater(() -> {
                    dialogVBox.getChildren().clear();
                    if (categories == null || categories.isEmpty()) {
                        showEmptyState();
                    } else {
                        for (TransactionCategory tc : categories) {
                            CategoryComponent component = new CategoryComponent(dashboardController, tc);
                            dialogVBox.getChildren().add(component);
                        }
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    dialogVBox.getChildren().clear();
                    showEmptyState();
                });
            }
        }).start();
    }

    private void showEmptyState() {
        Label icon = new Label("🏷");
        icon.setStyle("-fx-font-size: 36px;");
        Label title = new Label("No Categories Yet");
        title.getStyleClass().add("h3");
        title.setStyle("-fx-font-weight: 700;");
        Label desc = new Label("Create your first category to start organizing your transactions.");
        desc.getStyleClass().add("body-text");
        desc.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
        desc.setWrapText(true);
        Label hint = new Label("Go to sidebar → Categories to add one.");
        hint.getStyleClass().add("caption-text");
        hint.setStyle("-fx-text-fill: #64748B;");

        VBox emptyBox = new VBox(8, icon, title, desc, hint);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60, 0, 60, 0));
        dialogVBox.getChildren().add(emptyBox);
    }
}
